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
import wildfly.bug.onsuccess.event.SomeEntityChangeBEvent;

/**
 * This observer class is similar to the {@link SomeEntityChangeEventObserverAFacade} with the difference that this
 * class tries to use the requires new annotation to observe the event to open a transaction context. This is not
 * effective in our application. In our application we have been forced to use something like the executor facade here
 * to get the needed transaction context. In our application, we go into an infinite cycle of observing exception
 * without transaction context.
 *
 * In this sample application the same phenomena is not reproducible.
 * 
 *
 *
 */
@Singleton
@Interceptors(FacadeInterceptor.class)
public class SomeEntityChangeEventObserverBFacade
        extends AbstractSomeEntityChangeEventObserverFacade<SomeEntityChangeBEvent> {

    /**
     * We will not use this. instead we add the requires new annotation to the singleton. WHat we will see is that the
     * requires new annotation is in this case usless and the app server will essentially be killed.
     *
     */
    @Inject
    ExecutorFacadeLocal executorFacadeLocal;

    /**
     * In this test we check if the requires new annotation in the observer is effective. in our application this is not
     * the case, and we were forced into using the executor facade to be able to open a new transaction context.
     *
     * Here it seems to be effective.
     *
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void observeEvent(
            @Observes(during = TransactionPhase.AFTER_SUCCESS) SomeEntityChangeBEvent someEntityChangeEvent) {
        analyseStateOfEntityDuringEventHandling(someEntityChangeEvent);
    }

}
