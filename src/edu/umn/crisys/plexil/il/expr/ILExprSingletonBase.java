package edu.umn.crisys.plexil.il.expr;

import java.util.Optional;
import java.util.function.Function;

import edu.umn.crisys.plexil.runtime.values.PValue;

/**
 * Fulfills the same purpose as ILExprBase, but for singleton objects. This is
 * necessary because singletons are usually initialized statically, which 
 * can create a race condition if the static ILType is trying to refer to the
 * static singleton, while the static singleton is trying to refer to its
 * ILType. ILExprBase wants to just take the type as a constructor argument, 
 * which is usually desirable, but for singletons this can cause weird things
 * like the type ending up null at runtime even though it is clearly 
 * initialized with something. Using this class breaks the cycle, allowing
 * you to create singleton objects with no dependencies on other static objects
 * until after the static initialization phase of the JVM is done. 
 * 
 * <p>If you are implementing a singleton ILExpr, extend this class if possible.
 * It will helpfully force you to follow important conventions. 
 * 
 * <p>If the expression you are implementing is constant, or could be constant 
 * depending on its arguments, and it does not also implement PValue, 
 * override the eval() method to evaluate your expression. 
 * 
 * @author jbiatek
 *
 */
public abstract class ILExprSingletonBase implements ILExpr {


	@Override
	public final String toString() {
		return asString();
	}

	@Override
	public final Optional<PValue> eval() {
		// This method never needs to be overridden, so make it final.
		return ILExpr.super.eval();
	}
	
	@Override
	public final boolean equals(Object o) {
		if (o instanceof ILExpr) {  
			return equals((ILExpr) o);
		} else {
			return false;
		}
	}
	
	public abstract boolean equals(ILExpr e);
	
	@Override
	public abstract int hashCode();

	/**
	 * Evaluate this expression, using the provided Function to resolve 
	 * expressions that are not constants. 
	 * 
	 * Override this method if your method is either constant or may be 
	 * constant depending on its arguments, and if it does not also implement
	 * PValue.  The default behavior is to return PValues as-is, and otherwise
	 * return the value that the function gives for this expression. 
	 */
	@Override
	public Optional<PValue> eval(Function<ILExpr, Optional<PValue>> mapper) {
		if (this instanceof PValue) {
			return Optional.of((PValue) this);
		}
		return mapper.apply(this);
	}

}
