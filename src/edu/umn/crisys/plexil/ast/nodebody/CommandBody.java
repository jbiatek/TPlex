package edu.umn.crisys.plexil.ast.nodebody;

import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class CommandBody extends NodeBody {

    // TODO: Someday... resources.
    private Optional<ASTExpression> varToAssign;
    private ASTExpression cmdName;
    private List<ASTExpression> args;

    public CommandBody(ASTExpression cmdName, List<ASTExpression> args) {
        this(Optional.empty(), cmdName, args);
    }
    
    public CommandBody(ASTExpression varToAssign, ASTExpression cmdName, List<ASTExpression> args) {
    	this(Optional.of(varToAssign), cmdName, args);
    }

    
    public CommandBody(Optional<ASTExpression> varToAssign, 
    		ASTExpression cmdName, List<ASTExpression> args) {
    	varToAssign.ifPresent((ASTExpression var) -> {
    		if ( ! var.isAssignable()) throw new RuntimeException(varToAssign + " is not assignable.");
    	});
        PlexilType.STRING.typeCheck(cmdName.getType());
    	
        this.varToAssign = varToAssign;
        this.cmdName = cmdName;
        this.args = args;
    }
    
    /**
     * @return the variable to store the command's result to, or null if it 
     * isn't saved.
     */
    public Optional<ASTExpression> getVarToAssign() {
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
