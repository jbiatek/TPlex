package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.expr.var.ASTExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;


public enum NodeState implements PValue {

	INACTIVE,
	WAITING,
	EXECUTING,
	FINISHING,
	ITERATION_ENDED,
	FAILING,
	FINISHED;
	
	public PBoolean equalTo(PValue other) {
		return PValue.Util.enumEqualTo(this, other);
	}

	public static NodeState fromOrd(int ord_value) {
		switch (ord_value) {
			case 0: return INACTIVE;
			case 1: return WAITING; 
			case 2: return EXECUTING; 
			case 3: return FINISHING;
			case 4: return ITERATION_ENDED;
			case 5: return FAILING; 
			case 6: return FINISHED; 
			default: throw new RuntimeException("Out-of-range value for NodeState in NodeState::fromOrd");
		}
	}
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
	public PValue castTo(PlexilType type) {
		return PValue.Util.defaultCastTo(this, type);
	}

	@Override
	public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
		return visitor.visitNodeState(this, param);
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
		return accept((CommonExprVisitor<P, R>) visitor, param);
	}

	@Override
	public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
		return accept((CommonExprVisitor<P, R>) visitor, param);
	}

	@Override
	public String asString() {
		return toString();
	}

	@Override
	public boolean isAssignable() {
		return false;
	}
}