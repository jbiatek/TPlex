package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILType;

public enum NodeRefExpr implements ILExpr {

	PARENT,
	SIBLING,
	CHILD,
	SELF;
	
	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public ILType getType() {
		return ILType.NODEREF;
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
