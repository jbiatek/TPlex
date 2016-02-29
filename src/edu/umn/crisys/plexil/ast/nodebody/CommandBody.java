package edu.umn.crisys.plexil.ast.nodebody;

import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.ast.expr.PlexilExpr;
import edu.umn.crisys.plexil.ast.expr.PlexilType;

public class CommandBody extends NodeBody {

    // TODO: Someday... resources.
    private Optional<PlexilExpr> varToAssign;
    private PlexilExpr cmdName;
    private List<PlexilExpr> args;

    public CommandBody(PlexilExpr cmdName, List<PlexilExpr> args) {
        this(Optional.empty(), cmdName, args);
    }
    
    public CommandBody(PlexilExpr varToAssign, PlexilExpr cmdName, List<PlexilExpr> args) {
    	this(Optional.of(varToAssign), cmdName, args);
    }

    
    public CommandBody(Optional<PlexilExpr> varToAssign, 
    		PlexilExpr cmdName, List<PlexilExpr> args) {
        PlexilType.STRING.typeCheck(cmdName.getPlexilType());
    	
        this.varToAssign = varToAssign;
        this.cmdName = cmdName;
        this.args = args;
    }
    
    /**
     * @return the variable to store the command's result to (optional)
     */
    public Optional<PlexilExpr> getVarToAssign() {
        return varToAssign;
    }
    
    public PlexilExpr getCommandName() {
        return cmdName;
    }
    
    public List<PlexilExpr> getCommandArguments() {
        return args;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitCommand(this, param);
    }
}
