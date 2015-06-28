package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.expr.ExprType;

/**
 * Represents a Condition that wasn't specified in the XML file. This matters
 * most for EndConditions, which are very different depending on which type
 * the Node is. We want to tell the difference between the programmer leaving
 * it blank versus actually specifying "true". 
 * @author jbiatek
 *
 */
public class DefaultEndExpr extends ExpressionBase {

    private static DefaultEndExpr singleton;
    
    public static DefaultEndExpr get() {
        if (singleton == null) {
            singleton = new DefaultEndExpr();
        }
        return singleton;
    }

    private DefaultEndExpr() {
    	super(ExprType.BOOLEAN);
    }

    @Override
    public String asString() {
        return "<PLEXIL default>";
    }
    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

}
