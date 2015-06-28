package edu.umn.crisys.plexil.il.action;

import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;

public class CommandAction implements PlexilAction {

    private SimpleVar handle;
    private Expression name;
    private List<Expression> args;
    private Optional<Expression> assignResultTo;
    
    public CommandAction(SimpleVar handle, Expression name, 
            List<Expression> args, Optional<Expression> assignResultTo) {
        PlexilType.COMMAND_HANDLE.typeCheck(handle.getType());
        PlexilType.STRING.typeCheck(name.getType());
    	this.handle = handle;
        this.name = name;
        this.args = args;
        this.assignResultTo = assignResultTo;
    }
    
    public CommandAction(SimpleVar handle, Expression name, 
            List<Expression> args, Expression assignResultTo) {
    	this(handle, name, args, Optional.of(assignResultTo));
    }
    
    public CommandAction(SimpleVar handle, Expression name, 
            List<Expression> args) {
    	this(handle, name, args, Optional.empty());
    }
    
    public List<Expression> getArgs() {
        return args;
    }
    
    public Expression getName() {
    	return name;
    }
    
    public SimpleVar getHandle() {
    	return handle;
    }
    
    public Optional<Expression> getPossibleLeftHandSide() {
    	return assignResultTo;
    }
    
    @Override
    public String toString() {
        String ret = "Issue command: ";
        ret += name + "(";
        for (Expression arg : args) {
            ret += arg + ", ";
        }
        return ret.substring(0, ret.length() - 2) +")";
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitCommand(this, param);
	}

}
