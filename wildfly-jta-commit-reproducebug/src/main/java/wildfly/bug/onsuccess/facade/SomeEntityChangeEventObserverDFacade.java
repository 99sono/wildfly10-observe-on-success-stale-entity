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
import wildfly.bug.onsuccess.event.SomeEntityChangeDEvent;

/**
 * Same as observer for event B, but in this case the event D will be sent after the application modifies the entity and
 * explicitly done by the page bean as opposed to the business logic EJB.
 *
 *
 *
 */
@Singleton
@Interceptors(FacadeInterceptor.class)
public class SomeEntityChangeEventObserverDFacade
        extends AbstractSomeEntityChangeEventObserverFacade<SomeEntityChangeDEvent> {

    /**
     * We will not use this. instead we add the requires new annotation to the singleton. WHat we will see is that the
     * requires new annotation is in this case usless and the app server will essentially be killed.
     *
     */
    @Inject
    ExecutorFacadeLocal executorFacadeLocal;

    /**
     * It is the page bean that fires the event, not business logic that modifies the entity.
     *
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void observeEvent(
            @Observes(during = TransactionPhase.AFTER_SUCCESS) SomeEntityChangeDEvent someEntityChangeEvent) {
        analyseStateOfEntityDuringEventHandling(someEntityChangeEvent);
    }

}
