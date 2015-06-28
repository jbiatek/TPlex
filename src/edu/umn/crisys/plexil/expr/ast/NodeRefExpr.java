package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExprType;

public enum NodeRefExpr implements Expression {

	PARENT,
	SIBLING,
	CHILD,
	SELF;
	
	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public ExprType getType() {
		return ExprType.NODEREF;
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
