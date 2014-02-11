package edu.umn.crisys.plexil.java.plx;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.StandardValue;
import edu.umn.crisys.plexil.java.values.PValue.Util;


public class PlexilArray<T extends PValue> extends StandardValue {

	private List<Variable<T>> array;
	private PlexilType type; 
	private String name;
	
	/**
	 * Create a new PlexilArray with space for a set number of elements. You
	 * must also provide a type for the array.
	 * 
	 * @param elements Number of elements this array will hold
	 * @param type The type of value that this array will hold.
	 */
	public PlexilArray(String name, int elements, PlexilType type, 
			T... initial) {
	    this.name = name;
	    if (type.isArrayType()) {
	        this.type = type;
	    } else {
	        this.type = type.toArrayType();
	    }
		array = new ArrayList<Variable<T>>(elements);
		int i = 0;
		for (; i<initial.length; i++) {
		    array.add(null);
			array.set(i, new Variable<T>(name+"["+i+"]", initial[i]));
		}
		for (; i<elements; i++) {
		    array.add(null);
			array.set(i, new Variable<T>(name+"["+i+"]", type));
		}
	}
	
	public Variable<T> get(PNumeric n) {
	    if (n.isUnknown()) {
	        throw new RuntimeException("Tried to access index UNKNOWN of array "
	                +name+"");
	    }
	    if (n.isReal()) {
	        throw new RuntimeException("Tried to pass a real index "+n+" to array");
	    }
	    int nn = n.getIntValue();
	    if (nn < 0 || nn >= array.size()) {
	        throw new ArrayIndexOutOfBoundsException(nn);
	    }
	    
		return array.get(n.getIntValue());
	}
	
	public String getName() {
		return name;
	}
	
	public int size() {
	    return array.size();
	}
	
	@Override
	public String toString() {
	    if (array.size() == 0) {
	        return "#()";
	    }
	    String s = "#(";
	    for (int i=0; i<array.size()-1; i++) {
	        s += array.get(i).getValue() + " ";
	    }
	    return s + array.get(array.size()-1).getValue() + ")";
	}
	
	public void assignArray(PValue o, int priority) {
	    if (o instanceof PlexilArray) {
	        @SuppressWarnings("unchecked")
            PlexilArray<? extends StandardValue> other = 
                (PlexilArray<? extends StandardValue>) o;
	        
	        
	        if (other.array.size() > this.array.size()) {
	            throw new RuntimeException("Can't assign a larger array ("+
	                    other.array.size()+") to a smaller one ("+
	                    this.array.size()+").");
	        }
	        int i=0;
	        for (; i<other.array.size(); i++) {
	            array.get(i).setValue(other.array.get(i).getValue(), priority);
	        }
	        for (; i<this.array.size(); i++) {
	            array.get(i).setValue(type.elementType().getUnknown(), priority);
	        }
	    } else {
	        throw new RuntimeException("Cannot assign "+o+" to an array.");
	    }
	}

	public void assignArray(PValue other) {
		this.assignArray(other, Integer.MIN_VALUE);
	}
	
	@Override
	public boolean isKnown() {
		return true;
	}

	@Override
	public boolean isUnknown() {
		return false;
	}

	@Override
	public PBoolean equalTo(PValue o) {
		// Is array equality even a thing?
		return BooleanValue.get(equals(o));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlexilArray) {
			@SuppressWarnings("rawtypes")
			PlexilArray other = (PlexilArray) obj;
			return this.array.equals(other.array);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (Variable<T> value : array) {
			hash += value.hashCode();
		}
		return hash;
	}

	@Override
	public PlexilType getType() {
		return type;
	}

	@Override
	public PValue castTo(PlexilType type) {
		return PValue.Util.defaultCastTo(this, type);
	}
	
	public void reset() {
		for (Variable<T> var : array) {
			var.reset();
		}
	}
	public void commit() {
		for (Variable<T> var : array) {
			var.commit();
		}
	}

    public boolean hasAssignmentsPending() {
        for (Variable<T> v : array) {
            if (v.hasAssignmentsPending()) {
                return true;
            }
        }
        return false;
    }
	
}
