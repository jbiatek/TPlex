package edu.umn.crisys.plexil.expr.il;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;

public class RootAncestorInvariantExpr implements Expression {

    @Override
    public PlexilType getType() {
        return PlexilType.BOOLEAN;
    }

    @Override
    public String toString() {
        return "<root node's ancestor invariant condition>";
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
