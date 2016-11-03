package edu.umn.crisys.plexil.il.action;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;

public class CommandAction implements PlexilAction {

    private SimpleVar handle;
    private SimpleVar ackFlag;
    private ILExpr name;
    private List<ILExpr> args;
    private Optional<ILExpr> assignResultTo;
    
    public CommandAction(SimpleVar handle, SimpleVar ackFlag, ILExpr name, 
            List<ILExpr> args, Optional<ILExpr> assignResultTo) {
        ILType.COMMAND_HANDLE.typeCheck(handle.getType());
        ILType.STRING.typeCheck(name.getType());
        ILType.NATIVE_BOOL.typeCheck(ackFlag.getType());
    	this.handle = handle;
    	this.ackFlag = ackFlag;
        this.name = name;
        this.args = args;
        this.assignResultTo = assignResultTo;
    }
    
    public CommandAction(SimpleVar handle, SimpleVar ackFlag, ILExpr name, 
            List<ILExpr> args, ILExpr assignResultTo) {
    	this(handle, ackFlag, name, args, Optional.of(assignResultTo));
    }
    
    public CommandAction(SimpleVar handle, SimpleVar ackFlag, ILExpr name, 
            List<ILExpr> args) {
    	this(handle, ackFlag, name, args, Optional.empty());
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
    
    public SimpleVar getAckFlag() {
    	return ackFlag;
    }
    
    public Optional<ILExpr> getPossibleLeftHandSide() {
    	return assignResultTo;
    }
    
    @Override
    public String toString() {
        String ret = "Issue command: " + name.toString();
        ret += args.stream().map(Object::toString)
        		.collect(Collectors.joining(", ", "(", ")"));
        return ret;
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitCommand(this, param);
	}

}
