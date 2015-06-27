package edu.umn.crisys.plexil.ast.expr.var;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

/**
 * An expression representing a variable reference. This is basically just the
 * name of a variable, it isn't linked to a specific node yet. You'll have to
 * resolve the variable from the node that is referring to it. 
 */
public class UnresolvedVariableExpr implements ASTExpression {

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
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitVariable(this, param);
    }

	@Override
	public boolean isAssignable() {
		return true;
	}
}
