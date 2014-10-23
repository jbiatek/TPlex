package edu.umn.crisys.plexil.runtime.values;

public interface PInteger extends PNumeric {
	
	@Override
	default public PValue castTo(PlexilType type) {
		if (type == PlexilType.REAL) return castToReal();
		else return PNumeric.super.castTo(type);
	}

}
