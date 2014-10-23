package edu.umn.crisys.plexil.ast.expr;

import java.util.List;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.expr.var.ASTExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;

/**
 * A CompositeExpression is an Expression that contains other expressions. 
 * All of them happen to be both IL and AST expressions too, which is nice.
 *
 * @author jbiatek
 *
 */
public abstract class CompositeExpr implements ASTExpression, ILExpression {

    
    /**
     * Get all arguments to this expression. 
     * @return
     */
    public abstract List<Expression> getArguments();
    
    /**
     * Get a new Expression that's the same as this one but with the given
     * arguments. Use this to make changes to this Expression while keeping 
     * them immutable.
     * 
     * @param args The new arguments to the expression
     * @return An expression of the same type but with these arguments instead.
     */
    public abstract CompositeExpr getCloneWithArgs(List<Expression> args);

    @Override // un-default this expression from AST and IL versions
    public abstract <P, R> R accept(CommonExprVisitor<P, R> visitor, P param);
    
    @Override
    public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
        return this.accept((CommonExprVisitor<P,R>)visitor, param);
    }

    @Override
    public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
        return this.accept((CommonExprVisitor<P,R>)visitor, param);
    }
    
}
