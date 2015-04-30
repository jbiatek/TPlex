package edu.umn.crisys.plexil.runtime.plx;

import java.util.Optional;

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
    
    private Optional<T> next = Optional.empty();
    
    public SimpleCurrentNext(T initial) {
        current = initial;
    }
    
    public T getCurrent() {
        return current;
    }
    
    public T getNext() {
    	return next.orElse(current);
    }
    
    public void setNext(T next) {
        this.next = Optional.of(next);
    }
    
    public void commit() {
    	next.ifPresent((value) -> current = value);
    	next = Optional.empty();
    }
}
