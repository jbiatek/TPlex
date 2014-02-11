package edu.umn.crisys.plexil.java.plx;

import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class Variable<T extends PValue> {

	private final PlexilType type;
	private final T originalValue;
	private final String name;
	
    private T currentValue;
	private T nextValue = null;
	private int priority = Integer.MIN_VALUE;
	
	public Variable(String name, T initialValue) {
		this.name = name;
		originalValue = initialValue;
		currentValue = initialValue;
		type = initialValue.getType();
	}
	
	@SuppressWarnings("unchecked")
    public Variable(String name, PlexilType type) {
		this.name = name;
		originalValue = (T) type.getUnknown();
		currentValue = (T) type.getUnknown();
		this.type = type;
	}
	
	public T getValue() {
		return currentValue;
	}
	
	public PlexilType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	public void setValue(PValue newVal, int priority) {
	    if (JavaPlan.DEBUG) {
	        System.out.println(name + ": Assignment received "+newVal);
	    }
	    if (priority < this.priority) {
	        return;
	    }
		nextValue = (T) newVal.castTo(type);
		this.priority = priority;
	}
	
	/*public void setValue(PValue newVal) {
		setValue(newVal, Integer.MAX_VALUE);
	}*/
	
	public void commit() {
		if (nextValue == null) return;
		
		if (JavaPlan.DEBUG) {
		    System.out.println(name+" committed value to "+nextValue);
		}
		
	    currentValue = nextValue;
		nextValue = null;
		priority = Integer.MIN_VALUE;
	}
	
	public boolean hasAssignmentsPending() {
		return (nextValue != null);
	}

	public void reset() {
		// TODO: Make sure this is the right thing to do on reset()
		setValue(originalValue, Integer.MIN_VALUE);
	}
	
}
