package edu.umn.crisys.plexil.il.expr;

/**
 * <p>Base class for Expressions. Extend this if possible, because it helps to
 * enforce some basic rules for expressions. If you are creating a singleton, 
 * consider extending the ILExprSingletonBase class instead (see those docs
 * for why). 
 * 
 * <p>As stated in ILExpr, make sure to override both getArguments() and
 * getCloneWithArgs() if this expression contains other expressions. 
 * 
 * <p>If the expression you are implementing is constant, or could be constant 
 * depending on its arguments, and it doesn't implement PValue, override the 
 * eval() method to properly evaluate your expression. 
 * 
 * @author jbiatek
 *
 */
public abstract class ILExprBase extends ILExprSingletonBase {
	
	private ILType type;
	
	public ILExprBase(ILType type) {
		if (type == null) {
			throw new RuntimeException("Cannot pass in null type. This can also "
					+ "happen if a static variable is being initialized before ILType is.");
		}
		this.type = type;
	}
	
	@Override
	public final ILType getType() {
		return type;
	}

}
