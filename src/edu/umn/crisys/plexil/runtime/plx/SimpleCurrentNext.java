package edu.umn.crisys.plexil.runtime.plx;

/**
 * Simple class implementing synchronous changes. Setting the next value won't
 * affect getCurrent() until a commit() occurs. Doesn't support reset, doesn't
 * have priority, etc.
 * @author jbiatek
 *
 * @param <T>
 */
public class SimpleCurrentNext<T> {

    private T current;
    
    private T next = null;
    
    public SimpleCurrentNext(T initial) {
        current = initial;
    }
    
    public T getCurrent() {
        return current;
    }
    
    public T getNext() {
        if (next == null) {
            // We are staying the same next step.
            return current;
        }
        return next;
    }
    
    public void setNext(T next) {
        this.next = next;
    }
    
    public void commit() {
        if (next == null) {
            // Stay the same.
            return;
        }
            
        current = next;
        next = null;
    }
}
