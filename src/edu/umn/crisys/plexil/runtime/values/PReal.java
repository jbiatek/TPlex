package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.il.ILType;

public interface PReal extends PNumeric {

	@Override
	default public PValue castTo(ILType type) {
		if (type == ILType.INTEGER) return castToInteger();
		else return PNumeric.super.castTo(type);
	}

}
