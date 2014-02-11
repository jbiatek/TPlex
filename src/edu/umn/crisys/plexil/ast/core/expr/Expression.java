package edu.umn.crisys.plexil.ast.core.expr;

import edu.umn.crisys.plexil.ast.core.visitor.CommonExprVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;

public interface Expression {
    
    public <P,R> R accept(CommonExprVisitor<P,R> visitor, P param);
    
    public PlexilType getType();
    
    /**
     * @return a human readable string. It's not toString() so that no one 
     * forgets to do it. You should implement a good toString() and have this
     * return it. 
     */
    public String asString();
}
