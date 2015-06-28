package edu.umn.crisys.plexil.expr;

import java.util.Collections;
import java.util.List;

import edu.umn.crisys.plexil.expr.common.Operation;

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
    
    public default Expression ensureType(ExprType type) {
    	if (type == this.getType()) return this;
    	
    	if (type == ExprType.BOOLEAN) {
    		return Operation.castToBoolean(this);
    	} else if (type == ExprType.INTEGER) {
    		return Operation.castToInteger(this);
    	} else if (type == ExprType.REAL) {
    		return Operation.castToReal(this);
    	} else if (type == ExprType.NUMERIC) {
    		return Operation.castToNumeric(this);
    	} else if (type == ExprType.STRING) {
    		return Operation.castToString(this);
    	} else {
    		throw new RuntimeException("No casting operator for "+type);
    	}
    }
}
