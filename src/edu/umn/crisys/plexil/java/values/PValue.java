package edu.umn.crisys.plexil.java.values;

/**
 * The interface defining what all PlexilValues can do.
 * @author jbiatek
 *
 */
public interface PValue {

	public abstract boolean isKnown();

	public abstract boolean isUnknown();

	public abstract PBoolean equalTo(PValue o);
	
	public abstract PlexilType getType();
	
	public abstract PValue castTo(PlexilType type);

	public static class Util {
		
		/** Performs, essentially, Java's == operation in Plexil's logic. That is,
		 * if either isUnknown(), the result is unknown, otherwise, they must be
		 * == to each other. Used for the enum types.
		 * 
		 * @param one
		 * @param two
		 * @return the equality check result
		 */
		public static PBoolean enumEqualTo(PValue one, PValue two) {
			if (one.isUnknown() || two.isUnknown()) {
				return UnknownValue.get();
			} else {
				return BooleanValue.get(one == two);
			}
		}
		
		/**
		 * Attempt a standard Java cast of the given value to the given type.
		 * Intended for use in places where UNKNOWN-aware smarter casting won't
		 * mean much, such as for the enum types.
		 * @param val
		 * @param cls
		 * @return a PlexilValue that can be safely cast to the given type, or
		 * an exception will be thrown.
		 */
		public static PValue defaultCastTo(
				PValue val, PlexilType type) {
			if (val.getType() == type) {
				return val;
			} else if (type == PlexilType.NUMERIC 
					&& val instanceof PNumeric) { 
				return val;
			}
			else if (val.isUnknown()) {
				return type.getUnknown();
			}
			throw new RuntimeException(
					"Cannot cast a "+val.getType()+" to "+type);
		}
		
	}
}