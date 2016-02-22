package edu.umn.crisys.plexil.expr.ast;

/**
 * An expression representing a variable reference. This is basically just the
 * name of a variable, it isn't linked to a specific node yet. You'll have to
 * resolve the variable from the node that is referring to it. 
 */
public class UnresolvedVariableExpr extends PlexilExprBase {

    private String varName;
    
    public UnresolvedVariableExpr(String varName, PlexilType type) {
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
    public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

}
