package edu.umn.crisys.plexil.jkind.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lustre.LustreTrace;

public class IncrementalTrace {

	private LustreTrace partialTrace;
	private Optional<IncrementalTrace> prefix;
	private Set<TraceProperty> properties = new HashSet<>();
	private Set<TraceProperty> triedAndFailed = new HashSet<>();
	private Map<TraceProperty,IncrementalTrace> triedAndSucceeded = new HashMap<>();
	
	public IncrementalTrace(LustreTrace theTrace, Set<TraceProperty> properties) {
		this(theTrace, Optional.empty(), properties);
	}
	
	public IncrementalTrace(LustreTrace partialTrace, 
			Optional<IncrementalTrace> prefix,
			Set<TraceProperty> properties) {
		this.partialTrace = partialTrace;
		this.prefix = prefix;
		this.properties = Collections.unmodifiableSet(properties);
	}

	public boolean filterAlreadyDone(TraceProperty p) {
		return ! triedAndFailed.contains(p) 
				&& ! triedAndSucceeded.keySet().contains(p);
	}
	
	public void addAsFailure(TraceProperty p) {
		triedAndFailed.add(p);
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
	
	public static LustreTrace concatenate(LustreTrace first, LustreTrace second) {
		LustreTrace newTrace = new LustreTrace(0);
		newTrace.addLustreTrace(first);
		newTrace.addLustreTrace(second);
		return newTrace;
	}
	
	public LustreTrace getPartialTrace() {
		return partialTrace;
	}
	
	public LustreTrace getFullTrace() {
		return prefix.map(p -> concatenate(p.getFullTrace(), partialTrace))
				.orElse(partialTrace);
	}
	
	@Override
	public String toString() {
		return "<trace hash "+hashCode()
				+", fragment length "+partialTrace.getLength()+">";
	}
	
}
