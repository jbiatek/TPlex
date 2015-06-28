package edu.umn.crisys.plexil.expr.il;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.expr.ExprType;

public class RootAncestorEndExpr extends ExpressionBase {

	public RootAncestorEndExpr() {
		super(ExprType.BOOLEAN);
	}
	
    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

	@Override
	public String asString() {
	    return "<root node's ancestor end condition>";
	}

}
