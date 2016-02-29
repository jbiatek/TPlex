package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.il.expr.ILType;

public interface PInteger extends PNumeric {
	
	@Override
	default public PValue castTo(ILType type) {
		if (type == ILType.REAL) return castToReal();
		else return PNumeric.super.castTo(type);
	}

}
