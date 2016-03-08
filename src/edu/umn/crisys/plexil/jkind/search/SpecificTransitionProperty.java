package edu.umn.crisys.plexil.jkind.search;

import java.util.Collections;
import java.util.Set;

import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import edu.umn.crisys.util.NameUtils;
import edu.umn.crisys.util.Util;
import jkind.lustre.Equation;
import jkind.lustre.IdExpr;
import jkind.lustre.NamedType;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;

public class SpecificTransitionProperty extends TraceProperty {

	private Transition myTransition;
	private NodeStateMachine myStateMachine;
	
	public SpecificTransitionProperty(Transition t, NodeStateMachine nsm) {
		this.myTransition = t;
		this.myStateMachine = nsm;
	}
	
	@Override
	public void addProperty(NodeBuilder node, PlanToLustre translator) {
		// Our property is actually something that the translator supports 
		// directly. 
		node.addLocal(new VarDecl(getPropertyId(), NamedType.BOOL));
		node.addEquation(new Equation(new IdExpr(getPropertyId()), 
				translator.getGuardForThisSpecificTransition(
						myTransition, myStateMachine)));
		node.addProperty(getPropertyId());
	}

	@Override
	public String getPropertyId() {
		return NameUtils.clean(
				"ExecuteTransition"+myTransition.description+myTransition.hashCode());
	}

	@Override
	public Set<TraceProperty> createInitialGoals() {
		return Util.asHashSet(this);
	}

	@Override
	public Set<TraceProperty> createIntermediateGoalsFrom(IncrementalTrace trace, PlanToLustre translator) {
		return Collections.emptySet();
	}

	@Override
	public boolean traceLooksReachable(IncrementalTrace trace) {
		return true;
	}

	@Override
	public String toString() {
		return getPropertyId();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myStateMachine == null) ? 0 : myStateMachine.hashCode());
		result = prime * result + ((myTransition == null) ? 0 : myTransition.hashCode());
		return result;
	}

	@Override
	public boolean equals(TraceProperty p) {
		if (this == p)
			return true;
		if (getClass() != p.getClass())
			return false;
		SpecificTransitionProperty other = (SpecificTransitionProperty) p;
		if (myStateMachine == null) {
			if (other.myStateMachine != null)
				return false;
		} else if (!myStateMachine.equals(other.myStateMachine))
			return false;
		if (myTransition == null) {
			if (other.myTransition != null)
				return false;
		} else if (!myTransition.equals(other.myTransition))
			return false;
		return true;
	}


}
