package edu.umn.crisys.plexil.expr.il;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.expr.PlexilType;

public class RootAncestorExitExpr extends ExpressionBase {

	public RootAncestorExitExpr() {
		super(PlexilType.BOOLEAN);
	}
	
    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

	@Override
	public String asString() {
	    return "<root node's ancestor exit condition>";
	}

}
