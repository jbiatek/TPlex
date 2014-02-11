package edu.umn.crisys.plexil.ast.core.node;

import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.core.visitor.NodeBodyVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class CommandBody extends NodeBody {

    // TODO: Someday... resources.
    private ASTExpression varToAssign;
    private ASTExpression cmdName;
    private List<ASTExpression> args;

    public CommandBody(ASTExpression cmdName, List<ASTExpression> args) {
        PlexilType.STRING.typeCheck(cmdName.getType());
        this.cmdName = cmdName;
        this.args = args;
    }
    
    public CommandBody(UnresolvedVariableExpr varToAssign, ASTExpression cmdName, List<ASTExpression> args) {
        this.varToAssign = varToAssign;
        PlexilType.STRING.typeCheck(cmdName.getType());
        this.cmdName = cmdName;
        this.args = args;
    }
    
    public CommandBody(ArrayIndexExpr varToAssign, ASTExpression cmdName, List<ASTExpression> args) {
        this.varToAssign = varToAssign;
        PlexilType.STRING.typeCheck(cmdName.getType());
        this.cmdName = cmdName;
        this.args = args;
    }
    
    /**
     * @return the variable to store the command's result to, or null if it 
     * isn't saved.
     */
    public ASTExpression getVarToAssign() {
        return varToAssign;
    }
    
    public ASTExpression getCommandName() {
        return cmdName;
    }
    
    public List<ASTExpression> getCommandArguments() {
        return args;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitCommand(this, param);
    }
}
