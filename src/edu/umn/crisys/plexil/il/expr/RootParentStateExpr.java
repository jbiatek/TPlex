package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.java.values.PlexilType;

public class RootParentStateExpr extends ILExprBase {

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
    public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
        return visitor.visitRootParentState(this, param);
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
