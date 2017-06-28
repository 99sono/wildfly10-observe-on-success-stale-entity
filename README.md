# wildfly10-observe-on-success-stale-entity

## Summary
The issue is reported in jboss JIRA:
https://issues.jboss.org/browse/WFLY-8954


In wildfly 10, using eclipselink 2.6.4 as JPA implementation layer,   an application does some business logic, fires and event and handles the event with an on success observer, will suffer from the problem that the entities it handles are stale, for it seems that at this point in time, eclipselink has not yet been instructed to merge into the server session cache the modified entities, although the commit has already been published to the database.

An application is forced to resort to dirty work-arounds such as: (a) refresh entities, using the entity manager refresh api. (b) post pone the execution of the @onsuccess to a later point in time have the on success put a message on a queue, and do the on success business logic in some independent mdb. It is not always possible that we will know what entities have changed so that we can do a refresh.

## The Sample Application Module
The sample application is comprised by three modules. 

wildfly-jta-commit-entities:
Contains a basic domain model entity called SomeEntity. We will populate the database with one or more entities and then use one of these entities to change its state, fire an event reporting that the entity has been changed, and obverse the event to see what we get out of the JPA cache when we access the entity and to proove that we are getting stale data out of the cache.


wildfly-jta-commit-createdb:
Containst a baic startup @Singleton EJB. This application must be deployed once. It will create the database tables need to run the sample application.


wildfly-jta-commit-reproducebug:
This is the main application. When deployed an index.xhtml page can be visit at application context /app (see jboss-web.xml).
When this application is deployed, one must visist the index page.
There, we will have several buttons corresponding to different tests.
We will explain these tests later.


## Configuration
### Widlfly XML stand alone config
In this project there is a folder that contains a valid wildfly 10 configuration that should be used to run the applicaiton.
The configuration can be found at:
wildfly-jta-commit-reproducebug\src\test\resources\wildfly_stand_alone_config\standalone-orcl-config.xml .

The configuration comprises ORCL database configuration, for an oracle database.
It comprises some queues, since the original sample application that was modified to build this application was using queues to build work on the back-end. 

### Required Module for wildfly (eclipselink)
The wildfly installation needs to be given the following modules:

org.eclipse.persistence.main
```xml
<module xmlns="urn:jboss:module:1.1" name="org.eclipse.persistence">
    <resources>
        <resource-root path="jipijapa-eclipselink-10.0.0.Final.jar"/>
        <resource-root path="eclipselink-2.6.4.jar">
           <filter>
              <exclude path="javax/**" />
           </filter>
        </resource-root>
    </resources>
 
    <dependencies>
        <module name="asm.asm"/>
        <module name="javax.api"/>
        <module name="javax.annotation.api"/>
        <module name="javax.enterprise.api"/>
        <module name="javax.persistence.api"/>
        <module name="javax.transaction.api"/>
        <module name="javax.validation.api"/>
        <module name="javax.xml.bind.api"/>
        <module name="javax.ws.rs.api"/>
        <module name="org.antlr"/>
        <module name="org.apache.commons.collections"/>
        <module name="org.dom4j"/>
        <module name="org.jboss.as.jpa.spi"/>
        <module name="org.jboss.logging"/>
        <module name="org.jboss.vfs"/>
    </dependencies>
</module>
```


## Reproduce ISSUE

### Deploy the create db war
Start by starting up wildfly with the standalone-orcl-config.xml.
Once the server is running, deploy the create DB war file.
This should create a table 
SELECT * FROM SOMEENTITY;

You can now undeploy the create db war, it is no longer needed once the DB schema is set in place.

### Deploy the reproduce bug war

#### Populate the database
Visit the index page a context localhost:8080/app, and press the top most button. (InvokeTestA - create some database entities)
The database SOMENTITY should now have some data stored in.

#### Reproduce the bug by pressing button fireSomeEntityChangeA/BEvent
Both buttos, for event A and even B lead to the same problem.
What happens is that the application modifieis a text field of a entity between true/false/true/false ... and when fires an event stating that it did this chaange.
The obsever of the envent shall be class SomeEntityChangeEventObserverA/BFacade.
The observer will take information about the primary key of the modified entity and will check if the value of the field that is supposed to be modified is the same or not before/after an enitty refresh.
What we see is that the value of the field that wasmodified changes with the refresh and that we have essentially gotten stale data out of the cache.

In the server log an error message shall be logged reporting this.

Now after doing this test using EVENT D.
When we fire the event D, we do first the data modification transaction without firing any event. Then the page bean after getting the transaction commited will invoke a second API that just fires the event D.
When the event D is observed, we do not have the problem of stale entities.


### Conclusion
This application shows that when the onsuccess observer is invoked, the JPA server session cache has not been yet updated with the modification done in the business transaction.
The same behavior does not happen when using weblogic with eclipselink, this is an issue that is specific to wildfly.








