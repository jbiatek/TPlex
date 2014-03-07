package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.CommonExprVisitor;

public abstract class ILExprBase implements ILExpression {

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        if (visitor instanceof ILExprVisitor<?,?>) {
            return this.accept((ILExprVisitor<P,R>) visitor, param);
        }
        
        throw new RuntimeException("I am an IL expression, but "+visitor+
                " can't visit ILExpressions.");
    }

}
