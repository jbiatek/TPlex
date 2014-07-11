package edu.umn.crisys.plexil.runtime.values;

public interface PBoolean extends PValue {

	public abstract boolean isTrue();

	public abstract boolean isFalse();
	
	public abstract boolean isNotFalse();
	
	public abstract boolean isNotTrue();

	/**
	 * Perform an OR operation.
	 * @param o
	 * @return the result
	 */
	public abstract PBoolean or(PBoolean o);

	/**
	 * Perform an XOR operation.
	 * @param o
	 * @return the result
	 */
	public abstract PBoolean xor(PBoolean o);

	/**
	 * Perform an AND operation.
	 * @param o
	 * @return the result
	 */
	public abstract PBoolean and(PBoolean o);

	/**
	 * Perform a NOT operation.
	 * @param o
	 * @return the result
	 */
	public abstract PBoolean not();

}