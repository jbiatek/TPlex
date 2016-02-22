package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILExprBase;
import edu.umn.crisys.plexil.expr.il.ILType;

/**
 * An expression representing a variable reference. This is basically just the
 * name of a variable, it isn't linked to a specific node yet. You'll have to
 * resolve the variable from the node that is referring to it. 
 */
public class UnresolvedVariableExpr extends ILExprBase {

    private String varName;
    
    public UnresolvedVariableExpr(String varName, ILType type) {
    	super(type);
        this.varName= varName;
    }
    
    @Override
    public String asString() {
        return varName;
    }
    
    
    public String getName() {
        return varName;
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
