package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;

public enum NodeRefExpr implements Expression {

	PARENT,
	SIBLING,
	CHILD,
	SELF;
	
	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visitNodeReference(this, param);
	}

	@Override
	public PlexilType getType() {
		return PlexilType.NODEREF;
	}

	@Override
	public String asString() {
		return toString().toLowerCase();
	}

	@Override
	public boolean isAssignable() {
		return false;
	}

}
