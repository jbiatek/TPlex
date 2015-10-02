package edu.umn.crisys.plexil.jkind.search;

import java.util.Set;

import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import jkind.lustre.builders.NodeBuilder;

public abstract class TraceProperty {
	public abstract void addProperty(NodeBuilder node, PlanToLustre translator);
	public abstract String getPropertyId();
	public abstract Set<TraceProperty> createInitialGoals();
	public abstract Set<TraceProperty> 
		createIntermediateGoalsFrom(IncrementalTrace trace, PlanToLustre translator);
	public abstract boolean 
		traceLooksReachable(IncrementalTrace trace);

	@Override
	public abstract int hashCode();
	@Override
	public final boolean equals(Object o) {
		if (o instanceof TraceProperty) {
			return equals((TraceProperty)o);
		}
		return false;
	}
	public abstract boolean equals(TraceProperty p);
}
