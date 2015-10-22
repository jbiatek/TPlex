package edu.umn.crisys.plexil.jkind.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.umn.crisys.plexil.jkind.results.JKindResultUtils;
import lustre.LustreTrace;

public class IncrementalTrace {

	private LustreTrace partialTrace;
	private Optional<IncrementalTrace> prefix;
	private Set<TraceProperty> properties = new HashSet<>();
	private Set<TraceProperty> triedAndFailed = new HashSet<>();
	private Map<TraceProperty,IncrementalTrace> triedAndSucceeded = new HashMap<>();
	
	public IncrementalTrace(LustreTrace partialTrace, 
			Optional<IncrementalTrace> prefix,
			Set<TraceProperty> properties) {
		this.partialTrace = partialTrace;
		this.prefix = prefix;
		this.properties = Collections.unmodifiableSet(properties);
	}

	public boolean propertyHasntBeenTriedYet(TraceProperty p) {
		return ! triedAndFailed.contains(p) 
				&& ! triedAndSucceeded.keySet().contains(p);
	}
	
	public void addAsFailure(TraceProperty p) {
		triedAndFailed.add(p);
	}
	
	public void addAsSuccess(IncrementalTrace child) {
		child.getProperties().forEach(prop -> triedAndSucceeded.put(prop, child));
	}
	
	public void addAsSuccess(TraceProperty p, IncrementalTrace child) {
		triedAndSucceeded.put(p, child);
	}
	
	public Optional<IncrementalTrace> getPrefix() {
		return prefix;
	}
	
	public Set<TraceProperty> getProperties() {
		return properties;
	}
	
	public LustreTrace getPartialTrace() {
		return partialTrace;
	}
	
	public LustreTrace getFullTrace() {
		return prefix.map(p -> JKindResultUtils.concatenate(p.getFullTrace(), partialTrace))
				.orElse(partialTrace);
	}
	
	@Override
	public String toString() {
		return "<trace hash "+hashCode()
				+", fragment length "+partialTrace.getLength()+">";
	}
	
}
