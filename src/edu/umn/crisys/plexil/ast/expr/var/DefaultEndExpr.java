package edu.umn.crisys.plexil.ast.expr.var;

import edu.umn.crisys.plexil.runtime.values.PlexilType;

/**
 * Represents a Condition that wasn't specified in the XML file. This matters
 * most for EndConditions, which are very different depending on which type
 * the Node is. We want to tell the difference between the programmer leaving
 * it blank versus actually specifying "true". 
 * @author jbiatek
 *
 */
public class DefaultEndExpr extends ASTExprBase {

    private static DefaultEndExpr singleton;
    
    public static DefaultEndExpr get() {
        if (singleton == null) {
            singleton = new DefaultEndExpr();
        }
        return singleton;
    }

    private DefaultEndExpr() {}
    
    @Override
    public PlexilType getType() {
        return PlexilType.BOOLEAN;
    }

    @Override
    public String asString() {
        return "<PLEXIL default>";
    }
    @Override
    public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
        return visitor.visitDefaultEnd(this, param);
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
