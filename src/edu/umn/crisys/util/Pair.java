/**
 * 
 */
package edu.umn.crisys.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Whalen
 *
 */
public class Pair<T1, T2> {
	
	
	@SafeVarargs
	public static <K, V> Map<K,V> mapify(Pair<K,V>... pairs) {
		Map<K, V> ret = new HashMap<K, V>();
		for (Pair<K,V> pair : pairs) {
			ret.put(pair.first, pair.second);
		}
		return ret;
	}
	
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
	
	public T1 getFirst() {
		return first;
	}
	
	public T2 getSecond() {
		return second;
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
