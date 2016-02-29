package edu.umn.crisys.plexil.runtime.values;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import edu.umn.crisys.plexil.expr.ast.ASTExprVisitor;
import edu.umn.crisys.plexil.expr.ast.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExprBase;
import edu.umn.crisys.plexil.il.expr.ILType;

/**
 * <p>A List that is also a PValue. It implements the PLEXIL array semantics -- 
 * it has a fixed size, accepts PIntegers as indexes, and when you initialize 
 * it, it follows the PLEXIL array assignment rules (my_array[10] = #(1,2,3) 
 * produces an array containting 1, 2, 3, and 7 UNKNOWNs). Unlike a PLEXIL
 * array, though, it's immutable, just like the rest of the PValue wrappers. 
 * 
 * <p>It's a PValue itself, which means that Lookups and Commands can return it.
 * 
 * <p>It's also a Java List, but since it's immutable don't try to change it 
 * or you'll get an exception thrown at you. That means no adding, removing, 
 * retaining, clearing, or setting. 
 * 
 * @author jbiatek
 *
 */
public class PValueList<T extends PValue> extends ILExprBase implements PValue, List<T>{

	private final T[] myValues;
	private final T unknown;
	
	@SafeVarargs
	public PValueList(ILType type, int maxSize, T...values) {
		this(type, maxSize, Arrays.asList(values));
	}
	
	@SafeVarargs
	public PValueList(ILType type, T...values) {
		this(type, values.length, Arrays.asList(values));
	}
	
	public PValueList(ILType type, List<T> values) {
		this(type, values.size(), values);
	}
	
	public PValueList(ILType type, int maxSize, List<T> values) {
		super(type);
		if ( ! type.isArrayType()) {
			throw new RuntimeException("Array needs to be an array type, not "+type);
		}
		
		this.unknown = uncheckedGetUnknown(type);
		this.myValues = uncheckedGetGenericArray(maxSize);
		// Initialize with the given values, if any
		if (values.size() > maxSize) {
			throw new ArrayIndexOutOfBoundsException(
					"Initialized an array that is too large: Size is "+
							values.size()+", but my max size is "+getMaxSize());
		}
		int index = 0;
		for (; index < values.size(); index++) {
			myValues[index] = values.get(index);
		}
		// Fill in the rest with UNKNOWN
		for (; index < myValues.length; index++) {
			myValues[index] = unknown;
		}
	}
	
	@Override
	public PlexilType getPlexilType() {
		// Get the AST type from the IL type.
		switch(getType()) {
		case ARRAY: return PlexilType.ARRAY;
		case BOOLEAN_ARRAY: return PlexilType.BOOLEAN_ARRAY;
		case INTEGER_ARRAY: return PlexilType.INTEGER_ARRAY;
		case REAL_ARRAY: return PlexilType.REAL_ARRAY;
		case STRING_ARRAY: return PlexilType.STRING_ARRAY;
		default: throw new RuntimeException("Missing case: "+getType());
		}
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}

	@SuppressWarnings("unchecked")
	private T uncheckedGetUnknown(ILType arrayType) {
		return (T) arrayType.elementType().getUnknown();
	}
	
	@SuppressWarnings("unchecked")
	private T[] uncheckedGetGenericArray(int size) {
		return (T[]) new PValue[size];
	}
	
	public int getMaxSize() {
		return myValues.length;
	}
	
	public T get(PInteger index) {
		return get(index.getIntValue());
	}
	
	/*
	 * PValue methods:
	 */
	
	@Override
	public boolean isKnown() {
		// Arrays are always known.
		return true;
	}

	@Override
	public boolean isUnknown() {
		// Arrays are always known.
		return false;
	}

	@Override
	public PBoolean equalTo(PValue o) {
		if (o instanceof PValueList) {
			return BooleanValue.get(this.equals(o));
		}
		return BooleanValue.get(false);
	}

	@Override
	public PValue castTo(ILType type) {
		this.getType().typeCheck(type);
		return this;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public String asString() {
		// Arrays in PLEXIL look like this for some reason
		String ret = "#(";
		for (T element : myValues) {
			ret += element.toString() + ", ";
		}
		return ret.replaceFirst(", $", "")+")";
	}

	/*
	 * List methods:
	 */
	
	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
	

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean contains(Object o) {
		for (T element : myValues) {
			if (o==null ? element==null : element.equals(o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if ( ! contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public T get(int index) {
		return myValues[index];
	}

	@Override
	public int indexOf(Object o) {
		for (int i=0; i<myValues.length; i++) {
			if ((o==null ? get(i)==null : o.equals(get(i)))) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return myValues.length == 0;
	}

	@Override
	public Iterator<T> iterator() {
		return Collections.unmodifiableList(Arrays.asList(myValues)).iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		for (int i=myValues.length-1; i >= 0; i--) {
			if ((o==null ? get(i)==null : o.equals(get(i)))) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		return Collections.unmodifiableList(Arrays.asList(myValues)).listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return Collections.unmodifiableList(Arrays.asList(myValues)).listIterator(index);
	}


	@Override
	public int size() {
		return myValues.length;
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return Collections.unmodifiableList(Arrays.asList(myValues)).subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		Object[] newArray = new Object[myValues.length];
		for (int i=0; i < myValues.length; i++) {
			newArray[i] = myValues[i];
		}
		return newArray;
	}

	@Override
	public <O> O[] toArray(O[] a) {
		return Arrays.asList(myValues).toArray(a);
	}
}
