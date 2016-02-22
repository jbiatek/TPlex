package edu.umn.crisys.plexil.expr.il;

public enum RootAncestorExpr implements ILExpr {

	END,
	EXIT,
	INVARIANT,
	STATE;

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public ILType getType() {
		return this == STATE ? ILType.STATE : ILType.BOOLEAN;
	}

	@Override
	public String asString() {
		return "<root ancestor's "+this.toString().toLowerCase()+">";
	}
	
}
