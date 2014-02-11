package edu.umn.crisys.plexil.ast.core.expr.var;

import edu.umn.crisys.plexil.ast.core.visitor.ASTExprVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;

/**
 * An expression representing a variable reference. This is basically just the
 * name of a variable, it isn't linked to a specific node yet. You'll have to
 * resolve the variable from the node that is referring to it. 
 */
public class UnresolvedVariableExpr extends ASTExprBase {

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
    public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
        return visitor.visitVariable(this, param);
    }
}
