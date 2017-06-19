/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wildfly.bug.onsuccess.facade;

import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import facade.ExecutorFacadeLocal;
import inteceptor.FacadeInterceptor;
import wildfly.bug.onsuccess.event.SomeEntityChangeAEvent;

/**
 * This singleton object observes a CDI event.
 *
 * And what ends up happening is that we get a stale entity.
 *
 * We consider this the master example, a we will enumerate the problems of this example. Problems in this scenarion:
 * <br>
 *
 * <ul>
 * <li>The Transaction attribute in the SINGLETON EJB is virtually irrelevant. If observe an event with transaction
 * attribute If our transaction attribute was requirest new an we were not using the executor facade collaborator in
 * here, we would have problems. That no transaction context is active. We are forced to use the exeuctor facade to
 * work-around this.
 * <li>The entity we read from the cache is stale. We need to an entity manager refresh to get the entity.
 *
 * </ul>
 *
 */
@Singleton
@Interceptors(FacadeInterceptor.class)
public class SomeEntityChangeEventObserverAFacade
        extends AbstractSomeEntityChangeEventObserverFacade<SomeEntityChangeAEvent> {

    @Inject
    ExecutorFacadeLocal executorFacadeLocal;

    /**
     * See description at the top of the class to see what is happening in this test. In short, we are forced into using
     * the executor facade and refreshing the java entity. Both things are quite dirty work-arounds.
     *
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void observeEvent(
            @Observes(during = TransactionPhase.AFTER_SUCCESS) SomeEntityChangeAEvent someEntityChangeEvent) {
        // FIXME
        // In wildfly we are forced to this
        executorFacadeLocal.executoreSynchronousNewJta(new Runnable() {
            @Override
            public void run() {
                analyseStateOfEntityDuringEventHandling(someEntityChangeEvent);
            }
        });
    }

}
