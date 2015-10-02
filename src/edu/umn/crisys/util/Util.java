package edu.umn.crisys.util;

import java.util.HashSet;
import java.util.Set;

public class Util {

	@SafeVarargs
	public static <T> Set<T> asHashSet(T... items) {
		HashSet<T> ret = new HashSet<>();
		for (T item : items) {
			ret.add(item);
		}
		return ret;
	}
	
	public static <T> Set<T> merge(Set<T> s1, Set<T> s2) {
		HashSet<T> newSet = new HashSet<>();
		newSet.addAll(s1);
		newSet.addAll(s2);
		return newSet;
	}
	
}
