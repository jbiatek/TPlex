package edu.umn.crisys.plexil.translator.il.vars;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;

public abstract class RHSVariable implements ILExpression, IntermediateVariable {

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        if (visitor instanceof ILExprVisitor<?,?>) {
            return this.accept((ILExprVisitor<P,R>) visitor, param);
        }
        throw new RuntimeException("This is an IL variable, cannot be visited as an Expression.");
    }

    @Override
    public PlexilType getType() {
        return PlexilType.UNKNOWN;
    }

}
