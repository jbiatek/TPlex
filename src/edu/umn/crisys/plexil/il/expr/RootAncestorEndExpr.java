package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class RootAncestorEndExpr implements ILExpression {

    @Override
    public PlexilType getType() {
        return PlexilType.BOOLEAN;
    }
    
    @Override
    public String toString() {
        return "<root node's ancestor end condition>";
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitRootParentEnd(this, param);
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
