/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wildfly.bug.onsuccess.facade;

import java.util.List;
import java.util.TreeSet;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.crud.SomeEntityRepository;
import db.model.SomeEntity;
import inteceptor.FacadeInterceptor;
import wildfly.bug.onsuccess.event.SomeEntityChangeAEvent;
import wildfly.bug.onsuccess.event.SomeEntityChangeBEvent;
import wildfly.bug.onsuccess.event.SomeEntityChangeCEvent;
import wildfly.bug.onsuccess.event.SomeEntityChangeDEvent;

/**
 * Run a given command with different flavors of JTA transaction context underneath it. Avoids mushrooming facade API
 * classes
 */
@LocalBean
@Stateless
@Interceptors(FacadeInterceptor.class)
public class ModifyEntityAndFireEventFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyEntityAndFireEventFacade.class);

    @Inject
    SomeEntityRepository someEntityRepository;

    /**
     * observer: On success, not supported, executor facade oopens new transaction.
     */
    @Inject
    Event<SomeEntityChangeAEvent> someEntityChangeEvent;

    /**
     * observer: On success and transaction requires new.
     */
    @Inject
    Event<SomeEntityChangeBEvent> someEntityChangeEventB;

    /**
     * observer: after completion and transaction requires new.
     */
    @Inject
    Event<SomeEntityChangeCEvent> someEntityChangeEventC;

    /**
     * Fire event D but not part of the update transaction. What expect is that the merge of clones to server session
     * chache step has already been done by eclipselink in this situation.
     */
    @Inject
    Event<SomeEntityChangeDEvent> someEntityChangeEventD;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void modifyEntityAndFireEventA() {
        executeExperimentA();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void modifyEntityAndFireEventB() {
        executeExperimentB();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void modifyEntityAndFireEventC() {
        executeExperimentC();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SomeEntityChangeDEvent modifyEntityAndFireEventDPart1ModifyEntity() {
        return executeExperimentDPart1();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void modifyEntityAndFireEventDPart2FireEvent(SomeEntityChangeDEvent eventToFire) {
        someEntityChangeEventD.fire(eventToFire);
    }

    /**
     * Load the entity with the smallest id, change the value and fire an event.
     */
    protected void executeExperimentA() {
        // (a) discover the ID of the lowest entity
        List<Integer> allEntityIds = someEntityRepository.fetchAlIds();
        if (allEntityIds.isEmpty()) {
            LOGGER.warn(" There is currently no entity created.");
        }

        // (b) We have entities that we can manipulate
        Integer smallestId = new TreeSet<>(allEntityIds).first();
        SomeEntity entity = someEntityRepository.fetchById(smallestId);
        LOGGER.info("We will now modify the entity: {}  we will toogle the text to either true or false ", entity);
        Boolean newValue = Boolean.TRUE.toString().equals(entity.getText()) ? Boolean.FALSE : Boolean.TRUE;
        String oldValue = entity.getText();

        // (c) Do the update
        entity.setText(newValue.toString());

        // (d) Build the CDI event and fire it up
        SomeEntityChangeAEvent someEntityChangeEventValue = new SomeEntityChangeAEvent(oldValue, newValue.toString(),
                smallestId);
        someEntityChangeEvent.fire(someEntityChangeEventValue);
    }

    /**
     * Load the entity with the smallest id, change the value and fire an event.
     */
    protected void executeExperimentB() {
        // (a) discover the ID of the lowest entity
        List<Integer> allEntityIds = someEntityRepository.fetchAlIds();
        if (allEntityIds.isEmpty()) {
            LOGGER.warn(" There is currently no entity created.");
        }

        // (b) We have entities that we can manipulate
        Integer smallestId = new TreeSet<>(allEntityIds).first();
        SomeEntity entity = someEntityRepository.fetchById(smallestId);
        LOGGER.info("We will now modify the entity: {}  we will toogle the text to either true or false ", entity);
        Boolean newValue = Boolean.TRUE.toString().equals(entity.getText()) ? Boolean.FALSE : Boolean.TRUE;
        String oldValue = entity.getText();

        // (c) Do the update
        entity.setText(newValue.toString());

        // (d) Build the CDI event and fire it up
        SomeEntityChangeBEvent someEntityChangeEventValue = new SomeEntityChangeBEvent(oldValue, newValue.toString(),
                smallestId);
        someEntityChangeEventB.fire(someEntityChangeEventValue);
    }

    protected void executeExperimentC() {
        // (a) discover the ID of the lowest entity
        List<Integer> allEntityIds = someEntityRepository.fetchAlIds();
        if (allEntityIds.isEmpty()) {
            LOGGER.warn(" There is currently no entity created.");
        }

        // (b) We have entities that we can manipulate
        Integer smallestId = new TreeSet<>(allEntityIds).first();
        SomeEntity entity = someEntityRepository.fetchById(smallestId);
        LOGGER.info("We will now modify the entity: {}  we will toogle the text to either true or false ", entity);
        Boolean newValue = Boolean.TRUE.toString().equals(entity.getText()) ? Boolean.FALSE : Boolean.TRUE;
        String oldValue = entity.getText();

        // (c) Do the update
        entity.setText(newValue.toString());

        // (d) Build the CDI event and fire it up
        SomeEntityChangeCEvent someEntityChangeEventValue = new SomeEntityChangeCEvent(oldValue, newValue.toString(),
                smallestId);
        someEntityChangeEventC.fire(someEntityChangeEventValue);
    }

    /**
     * Modify an entity, build an event object, but do not fir an event.
     *
     * @return
     */
    protected SomeEntityChangeDEvent executeExperimentDPart1() {
        // (a) discover the ID of the lowest entity
        List<Integer> allEntityIds = someEntityRepository.fetchAlIds();
        if (allEntityIds.isEmpty()) {
            LOGGER.warn(" There is currently no entity created.");
        }

        // (b) We have entities that we can manipulate
        Integer smallestId = new TreeSet<>(allEntityIds).first();
        SomeEntity entity = someEntityRepository.fetchById(smallestId);
        LOGGER.info("We will now modify the entity: {}  we will toogle the text to either true or false ", entity);
        Boolean newValue = Boolean.TRUE.toString().equals(entity.getText()) ? Boolean.FALSE : Boolean.TRUE;
        String oldValue = entity.getText();

        // (c) Do the update
        entity.setText(newValue.toString());

        // (d) Build the CDI event and fire it up
        SomeEntityChangeDEvent someEntityChangeEventValue = new SomeEntityChangeDEvent(oldValue, newValue.toString(),
                smallestId);
        return someEntityChangeEventValue;
    }

}
