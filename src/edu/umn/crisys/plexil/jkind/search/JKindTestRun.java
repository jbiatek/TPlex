package edu.umn.crisys.plexil.jkind.search;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.util.Util;

public class JKindTestRun {
	
	private Set<TraceProperty> fromScratch = new HashSet<>();
	private Map<IncrementalTrace,Set<TraceProperty>> testsToGet = new HashMap<>();
	
	public void add(IncrementalTrace prefix, TraceProperty prop) {
		testsToGet.merge(prefix, Util.asHashSet(prop), 
				Util::merge);
	}
	
	public void add(TraceProperty prop) {
		fromScratch.add(prop);
	}
	
	public boolean removeWithoutPrefix(TraceProperty prop) {
		return fromScratch.remove(prop);
	}
	
	public boolean removeAllFromWithoutPrefix(Collection<? extends TraceProperty> c) {
		return fromScratch.removeAll(c);
	}
	
	public boolean isEmpty() {
		return fromScratch.isEmpty() && testsToGet.isEmpty();
	}
	
	public Set<IncrementalTrace> getAllPrefixes() {
		return Collections.unmodifiableSet(testsToGet.keySet());
	}
	
	public Set<TraceProperty> getPropertiesForPrefix(IncrementalTrace trace) {
		return Collections.unmodifiableSet(testsToGet.get(trace));
	}
	
	public Set<TraceProperty> getPropertiesWithoutPrefix() {
		return Collections.unmodifiableSet(fromScratch);
	}
}
