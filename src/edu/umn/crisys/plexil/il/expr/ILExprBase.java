package edu.umn.crisys.plexil.il.expr;

import java.util.Optional;
import java.util.function.Function;

import edu.umn.crisys.plexil.runtime.values.PValue;

/**
 * Base class for Expressions. Extend this if possible, because it helps to
 * enforce some basic rules for expressions. 
 * 
 * If the expression you are implementing is constant, or could be constant 
 * depending on its arguments, override the eval() method to evaluate your
 * expression. 
 * 
 * @author jbiatek
 *
 */
public abstract class ILExprBase implements ILExpr {

	private ILType type;
	
	public ILExprBase(ILType type) {
		this.type = type;
	}
	
	@Override
	public final ILType getType() {
		return type;
	}

	@Override
	public final String toString() {
		return asString();
	}

	@Override
	public final Optional<PValue> eval() {
		// This method never needs to be overridden, so make it final.
		return ILExpr.super.eval();
	}

	/**
	 * Evaluate this expression, using the provided Function to resolve 
	 * expressions that are not constants. 
	 * 
	 * Override this method if your method is either constant or may be 
	 * constant depending on its arguments.  By default, this method assumes
	 * that this expression is never constant, and so uses the result of
	 * the function directly.
	 */
	@Override
	public Optional<PValue> eval(Function<ILExpr, Optional<PValue>> mapper) {
		return mapper.apply(this);
	}
	

}
