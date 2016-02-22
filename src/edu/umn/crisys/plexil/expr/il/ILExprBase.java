package edu.umn.crisys.plexil.expr.il;

/**
 * Base class for Expressions. Extend this if possible, because it helps to
 * enforce some basic rules for expressions. 
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

}
