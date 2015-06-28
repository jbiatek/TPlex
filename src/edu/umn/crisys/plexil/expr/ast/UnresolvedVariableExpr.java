package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;

/**
 * An expression representing a variable reference. This is basically just the
 * name of a variable, it isn't linked to a specific node yet. You'll have to
 * resolve the variable from the node that is referring to it. 
 */
public class UnresolvedVariableExpr implements Expression {

    private String varName;
    private PlexilType type = PlexilType.UNKNOWN;
    
    private UnresolvedVariableExpr(String varName) {
        this.varName = varName;
    }
    
    public UnresolvedVariableExpr(String varName, PlexilType type) {
        this(varName);
        this.type = type;
    }
    
    public String toString() {
        return varName;
    }
    
    @Override
    public String asString() { return this.toString(); }
    
    public String getName() {
        return varName;
    }
    
    @Override
    public PlexilType getType() {
        return type;
    }

    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

	@Override
	public boolean isAssignable() {
		return true;
	}
}
