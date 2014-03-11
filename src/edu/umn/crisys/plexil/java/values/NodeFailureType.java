package edu.umn.crisys.plexil.java.values;

import edu.umn.crisys.plexil.ast.core.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.core.expr.var.ASTExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;


public enum NodeFailureType implements PValue {
	PRE_CONDITION_FAILED,
	POST_CONDITION_FAILED,
	INVARIANT_CONDITION_FAILED,
	PARENT_FAILED,
	EXITED,
	PARENT_EXITED,
	UNKNOWN;

	public PBoolean equalTo(PValue other) {
		return PValue.Util.enumEqualTo(this, other);
	}
	
	@Override
	public boolean isKnown() {
		return !this.equals(UNKNOWN);
	}

	@Override
	public boolean isUnknown() {
		return this.equals(UNKNOWN);
	}

	@Override
	public PlexilType getType() {
		return PlexilType.FAILURE;
	}

	@Override
	public PValue castTo(PlexilType type) {
		return PValue.Util.defaultCastTo(this, type);
	}

	@Override
	public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
		return visitor.visitNodeFailure(this, param);
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
