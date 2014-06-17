package edu.umn.crisys.plexil.java.values;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;

/**
 * The interface defining what all PlexilValues can do.
 * @author jbiatek
 *
 */
public interface PValue extends ASTExpression, ILExpression {

	public abstract boolean isKnown();

	public abstract boolean isUnknown();

	public abstract PBoolean equalTo(PValue o);
	
	public abstract PlexilType getType();
	
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