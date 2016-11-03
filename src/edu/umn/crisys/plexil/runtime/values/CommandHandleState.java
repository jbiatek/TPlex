package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILType;

public enum CommandHandleState implements PValue {
	UNKNOWN,
	COMMAND_SENT_TO_SYSTEM,
	COMMAND_ACCEPTED,
	COMMAND_RCVD_BY_SYSTEM,
	COMMAND_FAILED,
	COMMAND_DENIED,
	COMMAND_SUCCESS,
	COMMAND_INTERFACE_ERROR;
	
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
