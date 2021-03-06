package edu.umn.crisys.plexil.il.expr;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import edu.umn.crisys.plexil.runtime.values.PValue;

/**
 * <p>Common interface for IL expressions. If possible, extend the ILExprBase 
 * class rather than implementing this interface directly. The base class
 * is able to more strictly enforce the required conventions.
 * 
 * <p>If this expression contains other expressions, you must override both the
 * getArguments() and getCloneWithArgs() methods. See their individual documentation
 * for more details. 
 * 
 * <p>If this expression can appear on the left hand side of an assignment,
 * override the isAssignable() method. 
 * 
 * <p>The other default
 * methods should not need to be overridden, they are convenience methods that 
 * should apply universally to any ILExpr.  
 * 
 * <p>If you cannot extend ILExprBase for your purposes (writing an enum, a class
 * that absolutely must extend something else, etc.) please do the following:
 * <ul>
 *   <li>Implement a toString() method that returns the expression as it might 
 *   appear in source code. Your "asString()" method should just return toString().</li>
 *   <li>Implement hashCode() and equals() following the normal Java rules, 
 *   making sure that objects are equal if and only if they represent the same
 *   abstract syntax tree. </li>
 * </ul>
 * @author jbiatek
 *
 */
public interface ILExpr {
    
    public <P,R> R accept(ExprVisitor<P,R> visitor, P param);
    
    public default <R> R accept(ExprVisitor<Void,R> visitor) {
    	return this.accept(visitor, null);
    }
    
    public ILType getType();
    
    /**
     * Get all arguments to this expression. 
     * @return
     */
    public default List<ILExpr> getArguments() {
    	return Collections.emptyList();
    }
    
    /**
     * If you're sure that this expression has exactly one argument, use
     * this method to get it. An exception will be thrown if you are wrong.
     * 
     * @return
     */
    public default ILExpr getUnaryArg() {
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
    public default ILExpr getBinaryFirst() {
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
    public default ILExpr getBinarySecond() {
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
    public default ILExpr getCloneWithArgs(List<ILExpr> args) {
    	if (args.size() != 0) {
    		throw new RuntimeException("Tried to clone "+this+" with args");
    	}
    	return this;
    }

    /**
     * Evaluate this expression statically. (This method almost certainly does
     * not need to be overridden.)
     * 
     * @return The static value of this expression, or empty if it depends
     * on values that are not constant.
     */
    public default Optional<PValue> eval() {
    	// Provide an empty context for resolving variables.
    	return eval((e) -> Optional.empty());
    }
    
    /**
     * Evaluate this expression, using the given function to map variables
     * and other dynamic expressions to concrete values. Empty will be returned
     * if the expression depends on a non-constant expression that is not
     * mapped to a value. 
     * 
     * @param mapper A function that maps dynamic IL expressions to 
     * concrete values. 
     * @return The value of this expression under the provided mappings of
     * variable expressions. 
     */
    public Optional<PValue> eval(Function<ILExpr, Optional<PValue>> mapper);
    
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
