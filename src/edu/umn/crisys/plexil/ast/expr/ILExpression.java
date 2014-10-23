package edu.umn.crisys.plexil.ast.expr;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;


/**
 * IL Expressions and AST expressions are practically the same thing. The only 
 * difference is that in the AST, variables are unresolved names, whereas
 * in the IL they have to be actual variable objects. The XML parser returns
 * Expressions, and when you "translate" them to ILExpressions, that 
 * really just means resolving the variable names and replacing them with the
 * correct IL variable. 
 */
public interface ILExpression extends Expression {

    public <P,R> R accept(ILExprVisitor<P,R> visitor, P param);

    @Override
    default public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        if (visitor instanceof ILExprVisitor<?,?>) {
            return this.accept((ILExprVisitor<P,R>) visitor, param);
        }
        
        throw new RuntimeException("I am an IL expression, but "+visitor+
                " can't visit ILExpressions.");
    }

}
