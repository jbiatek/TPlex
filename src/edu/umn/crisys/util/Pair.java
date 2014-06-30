/**
 * 
 */
package edu.umn.crisys.util;

/**
 * @author Whalen
 *
 */
public class Pair<T1, T2> {
	public T1 first;
	public T2 second; 
	
	public Pair(T1 t1, T2 t2) {
		first = t1; 
		second = t2;
	}
	
	@Override 
	public String toString() {
	    return "("+first+", "+second+")";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof Pair) {
			return ((Pair<?,?>) o).first.equals(this.first)
					&& ((Pair<?,?>) o).second.equals(this.second);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return first.hashCode() + second.hashCode();
	}
}
