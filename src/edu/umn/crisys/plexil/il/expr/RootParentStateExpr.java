package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class RootParentStateExpr implements Expression {

    @Override
    public PlexilType getType() {
        return PlexilType.STATE;
    }
    
    @Override
    public String toString() {
        return "<root node's parent state>";
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitRootParentState(this, param);
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
