package edu.umn.crisys.plexil.runtime.values;

public interface PReal extends PValue {

	
	public abstract double getRealValue();

	public abstract PBoolean gt(PReal o);
	public abstract PBoolean ge(PReal o);
	public abstract PBoolean lt(PReal o);
	public abstract PBoolean le(PReal o);
	public abstract PReal add(PReal o);
	public abstract PReal sub(PReal o);
	public abstract PReal mul(PReal o);
	public abstract PReal div(PReal o);
	public abstract PReal mod(PReal o);
	public abstract PReal max(PReal o);
	public abstract PReal min(PReal o);
	public abstract PReal sqrt();
	public abstract PReal abs();
	/**
	 * "returns least positive integer greater than or equal to [this]"
	 * 
	 * "In each conversion function, if the supplied Real is out of range for 
	 * an Integer, UNKNOWN is returned"
	 */
	public abstract PInteger ceil();
	/** 
	 * "returns most positive integer less than or equal to [this]"
	 * "In each conversion function, if the supplied Real is out of range for 
	 * an Integer, UNKNOWN is returned"
	 */
	public abstract PInteger floor();
	/**
	 * "as defined in the C language standard"
	 * "In each conversion function, if the supplied Real is out of range for 
	 * an Integer, UNKNOWN is returned"
	 */
	public abstract PInteger round();
	/**
	 * "rounds toward 0"
	 * "In each conversion function, if the supplied Real is out of range for 
	 * an Integer, UNKNOWN is returned"
	 */
	public abstract PInteger trunc();
	/**
	 * "For converting a Real that is known to be exactly integer-valued...
	 * will return UNKNOWN if the supplied Real is not exactly an integer value."
	 * "In each conversion function, if the supplied Real is out of range for 
	 * an Integer, UNKNOWN is returned"
	 */
	public abstract PInteger real_to_int();

}
