package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.PlexilType;

public enum NodeState implements PValue {

	INACTIVE,
	WAITING,
	EXECUTING,
	FINISHING,
	ITERATION_ENDED,
	FAILING,
	FINISHED;
	
	@Override
	public boolean isKnown() {
		return true;
	}

	@Override
	public boolean isUnknown() {
		return false;
	}

	@Override
	public PlexilType getType() {
		return PlexilType.STATE;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public String asString() {
		return toString();
	}
}