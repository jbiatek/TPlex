package edu.umn.crisys.plexil.ast.expr;

import java.util.List;

/**
 * A CompositeExpression is an Expression that contains other expressions. 
 * All of them happen to be both IL and AST expressions too, which is nice.
 *
 * @author jbiatek
 *
 */
public abstract class CompositeExpr implements Expression {

    
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

}
