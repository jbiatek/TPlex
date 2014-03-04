package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class RootAncestorEndExpr extends ILExprBase {

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
    public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
        return visitor.visitRootParentEnd(this, param);
    }

}
