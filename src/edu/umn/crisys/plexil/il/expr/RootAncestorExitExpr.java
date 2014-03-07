package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.java.values.PlexilType;

public class RootAncestorExitExpr extends ILExprBase {

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
    public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
        return visitor.visitRootParentExit(this, param);
    }

}
