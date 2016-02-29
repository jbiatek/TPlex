package edu.umn.crisys.plexil.runtime.plx;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.runtime.values.PInteger;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;

public class SimplePArray<T extends PValue> {

	/**
	 * The current value of this array variable.
	 */
	private PValueList<T> current;
	/**
	 * Mutable storage for what values to commit next. 
	 */
	private List<T> next;
	/**
	 * Flags to see which ones have been modified -- it's not allowed to 
	 * have two assignments to the same variable in PLEXIL.
	 */
	private boolean[] modified;
	
	/**
	 * The declared maximum size.
	 */
	private int maxSize;
	/**
	 * This array's type.
	 */
	private ILType type;

	@SafeVarargs
	public SimplePArray(ILType t, int size, T...init) {
		this.current = new PValueList<T>(t, size, init);
		this.type = t;
		this.maxSize = size;
		this.modified = new boolean[maxSize];
		
		next = new ArrayList<T>(current);
	}
	
	public SimplePArray(int size, PValueList<T> init) {
		this.current = init;
		this.type = init.getType();
		this.maxSize = size;
		this.modified = new boolean[maxSize];
		
		next = new ArrayList<T>(current);
	}
	
	public PValueList<T> getCurrent() {
		return current;
	}
	
	public void arrayAssign(Object v) {
		if (v instanceof List) {
			List<?> uncheckedList = (List<?>) v;
			List<T> passedItems = new ArrayList<T>();
			for (Object element : uncheckedList) {
				if (element instanceof PValue) {
					PValue pvalue = (PValue) element;
					type.elementType().typeCheck(pvalue.getType());
					@SuppressWarnings("unchecked")
					T castValue = (T) pvalue.castTo(type.elementType());
					passedItems.add(castValue);
				} else {
					throw new ClassCastException("Array contains non-PValue "+element);
				}
			}
			// Everything seems okay. Call the real one.
			arrayAssign(passedItems);
		} else {
			throw new ClassCastException(v+" is not a list");
		}
	}
	
	public void arrayAssign(List<T> ts) {
		for (int i=0; i<maxSize; i++) {
			if (modified[i]) {
				throw new RuntimeException(
						"Tried to assign an entire array, but index "
								+i+" has already been assigned to!");
			} else {
				// Well, we're going to modify it now, so since we're here...
				modified[i] = true;
			}
		}
		// The new list is going to contain these values. The constructor for
		// PValueList handles the PLEXIL array assignment semantics.
		next = new ArrayList<T>(ts);
	}
	
	public void indexAssign(PInteger plexilIndex, T value) {
		indexAssign(plexilIndex.getIntValue(), value);
	}
	
	public void indexAssign(int index, T value) {
		if (index < 0 || index >= maxSize) {
			throw new IndexOutOfBoundsException(
					"Max size is "+maxSize+", but index was "+index);
		} else if (modified[index]) {
			throw new RuntimeException("Tried to assign twice to index "+index);
		}
		modified[index] = true;
		next.set(index, value);
	}
	
	public void commit() {
		// Create the new current value
		current = new PValueList<T>(type, maxSize, next);
		// Reset the next and modified storage spaces
		next = new ArrayList<T>(current);
		modified = new boolean[maxSize];
	}
}
