package edu.umn.crisys.plexil.java.values;

public abstract class StandardValue implements PValue {
	
	@Override
	public boolean isKnown() {
		return true;
	}
	
	@Override
	public boolean isUnknown() {
		return false;
	}
	
	public Object asNativeJava() {
	    throw new RuntimeException("No native Java exists for "+getClass());
	}
	
	/**
	 * Attempt to change this value to the given value type. If nothing bad
	 * happens, you can safely cast it to the given type. If this is unknown,
	 * a new unknown of the given type will be created.
	 * 
	 * Subclasses can override this to implement casting to another type
	 * (independent of Java's actual casting behavior). Just check to see
	 * if the given class is one you'd like to cast to, return the new value,
	 * and otherwise return super.castTo(c).
	 * 
	 * @param type The type that you'd like to cast to
	 * @return an object that can be safely cast, or an exception if it can't
	 * be done.
	 */
	@Override
	public PValue castTo(PlexilType type) {
		return PValue.Util.defaultCastTo(this, type);
	}
}
