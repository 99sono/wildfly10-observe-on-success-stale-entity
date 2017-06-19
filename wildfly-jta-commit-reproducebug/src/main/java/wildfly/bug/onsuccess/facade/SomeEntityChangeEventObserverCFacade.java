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
import wildfly.bug.onsuccess.event.SomeEntityChangeCEvent;

/**
 * Same as test B, but in this case we observe after completion.
 *
 *
 *
 */
@Singleton
@Interceptors(FacadeInterceptor.class)
public class SomeEntityChangeEventObserverCFacade
        extends AbstractSomeEntityChangeEventObserverFacade<SomeEntityChangeCEvent> {

    /**
     * We will not use this. instead we add the requires new annotation to the singleton. WHat we will see is that the
     * requires new annotation is in this case usless and the app server will essentially be killed.
     *
     */
    @Inject
    ExecutorFacadeLocal executorFacadeLocal;

    /**
     * Instead of doing after success we observe after completion.
     *
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void observeEvent(
            @Observes(during = TransactionPhase.AFTER_COMPLETION) SomeEntityChangeCEvent someEntityChangeEvent) {
        analyseStateOfEntityDuringEventHandling(someEntityChangeEvent);
    }

}
