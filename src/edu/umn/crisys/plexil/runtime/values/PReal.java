package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.PlexilType;

public interface PReal extends PNumeric {

	@Override
	default public PValue castTo(PlexilType type) {
		if (type == PlexilType.INTEGER) return castToInteger();
		else return PNumeric.super.castTo(type);
	}

}
