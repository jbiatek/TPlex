package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.ExprType;

public interface PInteger extends PNumeric {
	
	@Override
	default public PValue castTo(ExprType type) {
		if (type == ExprType.REAL) return castToReal();
		else return PNumeric.super.castTo(type);
	}

}
