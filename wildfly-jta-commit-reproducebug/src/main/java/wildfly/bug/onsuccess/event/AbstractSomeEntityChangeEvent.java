/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wildfly.bug.onsuccess.event;

/**
 * Base class for CDI event that shall be fire the application to study the wildfly behavior of handling events on
 * succes. We will have as many subclasses as experiments we do.
 *
 */
public abstract class AbstractSomeEntityChangeEvent {

    final String oldValue;
    final String newValue;
    final Integer someEntityId;

    /**
     * Create a new SomeEntityChangeEvent.
     *
     * @param oldValue
     * @param newValue
     * @param someEntityId
     */
    public AbstractSomeEntityChangeEvent(String oldValue, String newValue, Integer someEntityId) {
        super();
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.someEntityId = someEntityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public Integer getSomeEntityId() {
        return someEntityId;
    }

}
