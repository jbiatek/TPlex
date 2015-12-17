package edu.umn.crisys.plexil.expr.il;

import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;

public enum RootAncestorExpr implements Expression {

	END,
	EXIT,
	INVARIANT,
	STATE;

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public ExprType getType() {
		return this == STATE ? ExprType.STATE : ExprType.BOOLEAN;
	}

	@Override
	public String asString() {
		return "<root ancestor's "+this.toString().toLowerCase()+">";
	}
	
}
