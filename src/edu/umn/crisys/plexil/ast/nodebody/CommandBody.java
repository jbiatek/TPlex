package edu.umn.crisys.plexil.ast.nodebody;

import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;

public class CommandBody extends NodeBody {

    // TODO: Someday... resources.
    private Optional<Expression> varToAssign;
    private Expression cmdName;
    private List<Expression> args;

    public CommandBody(Expression cmdName, List<Expression> args) {
        this(Optional.empty(), cmdName, args);
    }
    
    public CommandBody(Expression varToAssign, Expression cmdName, List<Expression> args) {
    	this(Optional.of(varToAssign), cmdName, args);
    }

    
    public CommandBody(Optional<Expression> varToAssign, 
    		Expression cmdName, List<Expression> args) {
    	varToAssign.ifPresent((Expression var) -> {
    		if ( ! var.isAssignable()) throw new RuntimeException(varToAssign + " is not assignable.");
    	});
        PlexilType.STRING.typeCheck(cmdName.getType());
    	
        this.varToAssign = varToAssign;
        this.cmdName = cmdName;
        this.args = args;
    }
    
    /**
     * @return the variable to store the command's result to (optional)
     */
    public Optional<Expression> getVarToAssign() {
        return varToAssign;
    }
    
    public Expression getCommandName() {
        return cmdName;
    }
    
    public List<Expression> getCommandArguments() {
        return args;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitCommand(this, param);
    }
}
