package edu.umn.crisys.plexil.ast.expr;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.expr.common.Operation;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public interface Expression {
    
    public <P,R> R accept(CommonExprVisitor<P,R> visitor, P param);
    
    public PlexilType getType();
    
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
    public boolean isAssignable();
    
    public default Expression ensureType(PlexilType type) {
    	if (type == this.getType()) return this;
    	
    	if (type == PlexilType.BOOLEAN) {
    		return Operation.castToBoolean(this);
    	} else if (type == PlexilType.INTEGER) {
    		return Operation.castToInteger(this);
    	} else if (type == PlexilType.REAL) {
    		return Operation.castToReal(this);
    	} else if (type == PlexilType.NUMERIC) {
    		return Operation.castToNumeric(this);
    	} else if (type == PlexilType.STRING) {
    		return Operation.castToString(this);
    	} else {
    		throw new RuntimeException("No casting operator for "+type);
    	}
    }
}
