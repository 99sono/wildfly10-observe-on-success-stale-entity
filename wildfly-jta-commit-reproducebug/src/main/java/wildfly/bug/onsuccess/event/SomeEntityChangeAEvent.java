/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wildfly.bug.onsuccess.event;

/**
 * Event associated to master test. We are forced to refresh the stale entity we read from the db.
 */
public class SomeEntityChangeAEvent extends AbstractSomeEntityChangeEvent {

    /**
     * Create a new SomeEntityChangeEvent.
     *
     * @param oldValue
     * @param newValue
     * @param someEntityId
     */
    public SomeEntityChangeAEvent(String oldValue, String newValue, Integer someEntityId) {
        super(oldValue, newValue, someEntityId);
        // TODO Auto-generated constructor stub
    }

}
