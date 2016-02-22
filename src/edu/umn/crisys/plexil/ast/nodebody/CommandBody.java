package edu.umn.crisys.plexil.ast.nodebody;

import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILType;

public class CommandBody extends NodeBody {

    // TODO: Someday... resources.
    private Optional<ILExpr> varToAssign;
    private ILExpr cmdName;
    private List<ILExpr> args;

    public CommandBody(ILExpr cmdName, List<ILExpr> args) {
        this(Optional.empty(), cmdName, args);
    }
    
    public CommandBody(ILExpr varToAssign, ILExpr cmdName, List<ILExpr> args) {
    	this(Optional.of(varToAssign), cmdName, args);
    }

    
    public CommandBody(Optional<ILExpr> varToAssign, 
    		ILExpr cmdName, List<ILExpr> args) {
    	varToAssign.ifPresent((ILExpr var) -> {
    		if ( ! var.isAssignable()) throw new RuntimeException(varToAssign + " is not assignable.");
    	});
        ILType.STRING.typeCheck(cmdName.getType());
    	
        this.varToAssign = varToAssign;
        this.cmdName = cmdName;
        this.args = args;
    }
    
    /**
     * @return the variable to store the command's result to (optional)
     */
    public Optional<ILExpr> getVarToAssign() {
        return varToAssign;
    }
    
    public ILExpr getCommandName() {
        return cmdName;
    }
    
    public List<ILExpr> getCommandArguments() {
        return args;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitCommand(this, param);
    }
}
