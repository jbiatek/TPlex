package edu.umn.crisys.plexil.expr;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.expr.common.ASTOperation;
import edu.umn.crisys.plexil.runtime.values.PValue;

public interface Expression {
    
    public <P,R> R accept(ExprVisitor<P,R> visitor, P param);
    
    public default <R> R accept(ExprVisitor<Void,R> visitor) {
    	return this.accept(visitor, null);
    }
    
    public ExprType getType();
    
    /**
     * Get all arguments to this expression. 
     * @return
     */
    public default List<Expression> getArguments() {
    	return Collections.emptyList();
    }
    
    /**
     * If you're sure that this expression has exactly one argument, use
     * this method to get it. An exception will be thrown if you are wrong.
     * 
     * @return
     */
    public default Expression getUnaryArg() {
    	if (getArguments().size() != 1) {
    		throw new RuntimeException("Expression is not unary: "+this);
    	}
    	return getArguments().get(0);
    }
    
    /**
     * If you're sure that this expression has exactly two arguments,
     * use this method to get the first one. An exception will be thrown if
     * you are wrong. 
     * @return
     */
    public default Expression getBinaryFirst() {
    	if (getArguments().size() != 2) {
    		throw new RuntimeException("Expression is not binary: "+this);
    	}
    	return getArguments().get(0);
    }
    
    /**
     * If you're sure that this expression has exactly two arguments,
     * use this method to get the second one. An exception will be thrown if
     * you are wrong. 
     * @return
     */
    public default Expression getBinarySecond() {
    	if (getArguments().size() != 2) {
    		throw new RuntimeException("Expression is not binary: "+this);
    	}
    	return getArguments().get(1);
    }
    
    /**
     * Get a new Expression that's the same as this one but with the given
     * arguments. Use this to make changes to this Expression while keeping 
     * them immutable.
     * 
     * @param args The new arguments to the expression
     * @return An expression of the same type but with these arguments instead.
     */
    public default Expression getCloneWithArgs(List<Expression> args) {
    	if (args.size() != 0) {
    		throw new RuntimeException("Tried to clone "+this+" with args");
    	}
    	return this;
    }

    public default Optional<PValue> eval() {
    	return Optional.empty();
    }
    
    /**
     * @return a human readable string. It's not toString() so that no one 
     * forgets to do it. You should implement a good toString() and have this
     * return it. 
     */
    public String asString();
    
    /**
     * @return whether this expression is a valid left-hand side of 
     * an assignment.
     */
    public default boolean isAssignable() {
    	return false;
    }
    
}
