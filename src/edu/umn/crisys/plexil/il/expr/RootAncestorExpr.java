package edu.umn.crisys.plexil.il.expr;

import java.util.Optional;
import java.util.function.Function;

import edu.umn.crisys.plexil.runtime.values.PValue;

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

	@Override
	public Optional<PValue> eval(Function<ILExpr, Optional<PValue>> mapper) {
		return mapper.apply(this);
	}
	
}
