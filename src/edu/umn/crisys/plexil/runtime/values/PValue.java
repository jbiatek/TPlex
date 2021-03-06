package edu.umn.crisys.plexil.runtime.values;

import java.util.Optional;
import java.util.function.Function;

import edu.umn.crisys.plexil.ast.expr.PlexilExpr;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILType;

/**
 * The interface defining what all PlexilValues can do.
 * @author jbiatek
 *
 */
public interface PValue extends ILExpr, PlexilExpr {

	public abstract boolean isKnown();

	public abstract boolean isUnknown();

	public abstract ILType getType();

    public default Optional<PValue> eval(Function<ILExpr, Optional<PValue>> mapper) {
    	// We don't need a mapper, these all are static.
    	return Optional.of(this);
    }

	
	/**
	 * Performs, essentially, Java's equals() operator but with PLEXIL logic. 
	 * That is, if either one is unknown, return UNKNOWN, but otherwise return the
	 * result of the equals() operator. 
	 * 
	 * @param o
	 * @return a PBoolean answer
	 */
	default public PBoolean equalTo(PValue o) {
		if (this.isUnknown() || o.isUnknown()) return UnknownBool.get();
		else return BooleanValue.get(this.equals(o));
	}
	
	/**
	 * Attempt to change this value to the given value type. If nothing bad
	 * happens, you can safely Java cast it to the given type. This is the 
	 * baseline behavior, which handles these scenarios:
	 * 
	 * <ul>
	 * <li> If we're already the right type, return this
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
	default public PValue castTo(ILType type)  {
		if (this.getType() == type) {
			return this;
		} else if (this.isUnknown()) {
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