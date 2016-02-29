package edu.umn.crisys.plexil.il.action;

import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;

public class CommandAction implements PlexilAction {

    private SimpleVar handle;
    private ILExpr name;
    private List<ILExpr> args;
    private Optional<ILExpr> assignResultTo;
    
    public CommandAction(SimpleVar handle, ILExpr name, 
            List<ILExpr> args, Optional<ILExpr> assignResultTo) {
        ILType.COMMAND_HANDLE.typeCheck(handle.getType());
        ILType.STRING.typeCheck(name.getType());
    	this.handle = handle;
        this.name = name;
        this.args = args;
        this.assignResultTo = assignResultTo;
    }
    
    public CommandAction(SimpleVar handle, ILExpr name, 
            List<ILExpr> args, ILExpr assignResultTo) {
    	this(handle, name, args, Optional.of(assignResultTo));
    }
    
    public CommandAction(SimpleVar handle, ILExpr name, 
            List<ILExpr> args) {
    	this(handle, name, args, Optional.empty());
    }
    
    public List<ILExpr> getArgs() {
        return args;
    }
    
    public ILExpr getName() {
    	return name;
    }
    
    public String getNameAsConstantString() {
    	Optional<PValue> eval = name.eval();
    	if (! eval.isPresent()) {
    		throw new RuntimeException("Expression "+name+" is not constant.");
    	}
    	PValue actual = eval.get();
    	if (actual instanceof StringValue) {
    		return ((StringValue)actual).getString();
    	} else {
    		throw new RuntimeException(actual+" is not a (known) string");
    	}
    }
    
    public SimpleVar getHandle() {
    	return handle;
    }
    
    public Optional<ILExpr> getPossibleLeftHandSide() {
    	return assignResultTo;
    }
    
    @Override
    public String toString() {
        String ret = "Issue command: ";
        ret += name + "(";
        for (ILExpr arg : args) {
            ret += arg + ", ";
        }
        return ret.substring(0, ret.length() - 2) +")";
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitCommand(this, param);
	}

}
