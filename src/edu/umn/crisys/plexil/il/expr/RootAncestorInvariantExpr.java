package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class RootAncestorInvariantExpr extends ILExprBase {

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
    public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
        return visitor.visitRootParentInvariant(this, param);
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
