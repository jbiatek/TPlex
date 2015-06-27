package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class RootAncestorExitExpr implements Expression {

    @Override
    public PlexilType getType() {
        return PlexilType.BOOLEAN;
    }

    @Override
    public String toString() {
        return "<root node's ancestor exit condition>";
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitRootParentExit(this, param);
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
