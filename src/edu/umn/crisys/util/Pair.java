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
}
