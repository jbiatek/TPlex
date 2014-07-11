package edu.umn.crisys.plexil.runtime.values;


public abstract interface PNumeric extends PValue {
	
	public abstract boolean isReal();
	
	public abstract PInteger castToInteger();
	public abstract PReal castToReal();
	public abstract int getIntValue();
	public abstract double getRealValue();

	public abstract PBoolean gt(PNumeric o);
	public abstract PBoolean ge(PNumeric o);
	public abstract PBoolean lt(PNumeric o);
	public abstract PBoolean le(PNumeric o);
	public abstract PNumeric add(PNumeric o);
	public abstract PNumeric sub(PNumeric o);
	public abstract PNumeric mul(PNumeric o);
	public abstract PNumeric div(PNumeric o);
	public abstract PNumeric mod(PNumeric o);
	public abstract PNumeric max(PNumeric o);
	public abstract PNumeric min(PNumeric o);
	public abstract PNumeric sqrt();
	public abstract PNumeric abs();

}