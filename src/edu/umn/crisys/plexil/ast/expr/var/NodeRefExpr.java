package edu.umn.crisys.plexil.ast.expr.var;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public enum NodeRefExpr implements ASTExpression {

	PARENT,
	SIBLING,
	CHILD,
	SELF;
	
	@Override
	public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
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
