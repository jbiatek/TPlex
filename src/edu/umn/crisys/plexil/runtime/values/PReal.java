package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.ExprType;

public interface PReal extends PNumeric {

	@Override
	default public PValue castTo(ExprType type) {
		if (type == ExprType.INTEGER) return castToInteger();
		else return PNumeric.super.castTo(type);
	}

}
