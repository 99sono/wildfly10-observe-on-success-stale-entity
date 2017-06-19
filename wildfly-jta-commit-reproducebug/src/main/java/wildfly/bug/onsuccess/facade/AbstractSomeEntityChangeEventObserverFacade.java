/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wildfly.bug.onsuccess.facade;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.crud.SomeEntityRepository;
import db.model.SomeEntity;
import facade.ExecutorFacadeLocal;
import wildfly.bug.onsuccess.event.AbstractSomeEntityChangeEvent;

/**
 * Base class with the drivial business logic to check if we are getting stale data.
 *
 * The user interface offers multiple different apis that fire different event types that we observe. Each event
 * corresponds to a different test. What matters to the test is how the event is observed (e.g. transaction requires
 * new, transaction requress no, no transaction demarcatio).. I the entity stale, etc..
 *
 * @param <T>
 *            The concrete type of event that the test is observing.
 *
 */
public class AbstractSomeEntityChangeEventObserverFacade<T extends AbstractSomeEntityChangeEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSomeEntityChangeEventObserverFacade.class);

    @Inject
    SomeEntityRepository someEntityRepository;

    @Inject
    ExecutorFacadeLocal executorFacadeLocal;

    /**
     * Check the state of the entity during event handling. Do an entity refresh to see if the state changes, meaining
     * we have gottend a stale entity.
     *
     * @param someEntityChangeEvent
     *            the event that was fired by the application.
     */
    protected void analyseStateOfEntityDuringEventHandling(T someEntityChangeEvent) {
        LOGGER.info("START: TEST VALIDATION - FOR FIRED EVENT: {} ",
                someEntityChangeEvent.getClass().getCanonicalName());
        try {
            executeAnalysis(someEntityChangeEvent);
        } finally {
            LOGGER.info("ENDED: TEST VALIDATION - FOR FIRED EVENT: {} ",
                    someEntityChangeEvent.getClass().getCanonicalName());
        }

    }

    /**
     * See
     * {@link AbstractSomeEntityChangeEventObserverFacade#analyseStateOfEntityDuringEventHandling(AbstractSomeEntityChangeEvent)}
     *
     * @param someEntityChangeEvent
     *            the event to be analyzed.
     */
    protected void executeAnalysis(T someEntityChangeEvent) {
        // FIXME See comments bellow
        // (a) In wildfly we have the problem that during ON_SUCESS we are getting stale entities
        // as if the completed transaction that was successful had never run.
        // As a work around we need to refresh our entity to get the data that was persisted in the DB
        Integer transportEquipmentId = someEntityChangeEvent.getSomeEntityId();
        SomeEntity entity = someEntityRepository.fetchById(transportEquipmentId);
        String equimentModeBeforeRefresh = entity.getText();
        someEntityRepository.getEm().refresh(entity);
        String equimentModeAfterRefresh = entity.getText();
        LOGGER.info(
                "Before Refresh value was: {}, After Refresh: {}. Value on entity passed by event object as new value was: {} ",
                equimentModeBeforeRefresh, equimentModeAfterRefresh, someEntityChangeEvent.getNewValue());
        if (!equimentModeAfterRefresh.equals(equimentModeBeforeRefresh)) {
            LOGGER.error(
                    "Wildfly ON_SUCCESS handling observing stale entity that does not match what transaction persisted."
                            + " EntityMode before refresh: {} And After Refresh: {} For Equipment: {} ",
                    equimentModeBeforeRefresh, equimentModeAfterRefresh, transportEquipmentId);
        } else {
            LOGGER.info(
                    "The entity remeained unchanged before and after refresh. Before Refresh value was: {}, After Refresh: {} ",
                    equimentModeBeforeRefresh, equimentModeAfterRefresh);
        }
    }

}
