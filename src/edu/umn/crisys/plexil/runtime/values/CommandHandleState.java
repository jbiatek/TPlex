package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.ast.ASTExprVisitor;
import edu.umn.crisys.plexil.expr.ast.PlexilType;
import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILType;

public enum CommandHandleState implements PValue {
	UNKNOWN,
	COMMAND_ACCEPTED,
	COMMAND_SUCCESS,
	COMMAND_RCVD_BY_SYSTEM,
	COMMAND_SENT_TO_SYSTEM,
	COMMAND_FAILED,
	COMMAND_DENIED,
	COMMAND_ABORTED,
	COMMAND_ABORT_FAILED;
	
	public boolean isKnown() {
		return this != UNKNOWN;
	}

	@Override
	public boolean isUnknown() {
		return this == UNKNOWN;
	}

	@Override
	public ILType getType() {
		return ILType.COMMAND_HANDLE;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}
	
	@Override
	public String asString() {
		return toString();
	}

	@Override
	public PlexilType getPlexilType() {
		return PlexilType.COMMAND_HANDLE;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}
}
