package edu.umn.crisys.plexil.expr;

/**
 * Base class for Expressions. Extend this if possible, because it helps to
 * enforce some basic rules for expressions. 
 * 
 * @author jbiatek
 *
 */
public abstract class ExpressionBase implements Expression {

	private PlexilType type;
	
	public ExpressionBase(PlexilType type) {
		this.type = type;
	}
	
	@Override
	public final PlexilType getType() {
		return type;
	}

	@Override
	public final String toString() {
		return asString();
	}

}
