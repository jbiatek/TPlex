package edu.umn.crisys.plexil.jkind.search;

import java.util.Set;

import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import jkind.lustre.builders.NodeBuilder;

public abstract class TraceProperty {
	
	/**
	 * Add this property to the Node. The name that will be used can be gotten
	 * from getPropertyId().   
	 * 
	 * @param node The node that the property will be added to.
	 * @param translator The Plan translator, used to translate any IL expressions
	 * that the property may have.
	 */
	public abstract void addProperty(NodeBuilder node, PlanToLustre translator);
	
	/**
	 * @return the Lustre ID that will be used when inserting this property
	 * into a Node. 
	 */
	public abstract String getPropertyId();
	
	/**
	 * Get goals that this property would like to try for in the initial,
	 * non-prefixed run. 
	 * @return
	 */
	public abstract Set<TraceProperty> createInitialGoals();
	
	/**
	 * Get goals that this property thinks could be a stepping stone from
	 * the given trace to achieving this property. 
	 * @param trace
	 * @param translator
	 * @return
	 */
	public abstract Set<TraceProperty> 
		createIntermediateGoalsFrom(IncrementalTrace trace, PlanToLustre translator);
	
	/**
	 * Ask this trace if it thinks that it can be reached directly from this
	 * prefix. 
	 * @param trace
	 * @return
	 */
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
