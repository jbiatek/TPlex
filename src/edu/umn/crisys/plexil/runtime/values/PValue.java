package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;

/**
 * The interface defining what all PlexilValues can do.
 * @author jbiatek
 *
 */
public interface PValue extends ASTExpression, ILExpression {

	public abstract boolean isKnown();

	public abstract boolean isUnknown();

	public abstract PlexilType getType();

	/**
	 * Performs, essentially, Java's == operator but with PLEXIL logic. That is,
	 * if either one is unknown, return UNKNOWN, but otherwise they
	 * must be the exact same object. Just as in Java, sometimes this is okay
	 * for checking equality but sometimes not (e.g. 1 == 1 but "hello" doesn't
	 * always == "hello" in Java). 
	 * 
	 * @param o
	 * @return a PBoolean answer
	 */
	default public PBoolean equalTo(PValue o) {
		if (this.isUnknown() || o.isUnknown()) return UnknownValue.get();
		else return BooleanValue.get(this == o);
	}
	
	/**
	 * Attempt to change this value to the given value type. If nothing bad
	 * happens, you can safely Java cast it to the given type. This is the 
	 * baseline behavior, which handles these scenarios:
	 * 
	 * <ul>
	 * <li> If we're already the right type, return this
	 * <li> If the desired type is NUMERIC and we're already numeric, return this
	 * <li> If we're UNKNOWN, return an appropriate UNKNOWN
	 * </ul> 
	 * 
	 * Subclasses can override this to implement casting to another type
	 * (independent of Java's actual casting behavior). For example, 
	 * PIntegers add an additional case when the desired type is REAL, and
	 * then defer to PValue.super.castTo() for the remaining cases.  
	 * 
	 * @param type The type that you'd like to cast to
	 * @return an object that can be safely cast, or an exception if it can't
	 * be done.
	 */
	default public PValue castTo(PlexilType type)  {
		if (this.getType() == type) {
			return this;
		} else if (type == PlexilType.NUMERIC 
				&& this instanceof PNumeric) { 
			return this;
		}
		else if (this.isUnknown()) {
			return type.getUnknown();
		}
		throw new RuntimeException(
				"Cannot cast a "+this.getType()+" to "+type);
	}
	
	@Override
	default public boolean isAssignable() {
		return false;
	}
	
}