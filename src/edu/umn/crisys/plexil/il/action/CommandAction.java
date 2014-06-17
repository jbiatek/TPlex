package edu.umn.crisys.plexil.il.action;

import java.util.List;

import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class CommandAction implements PlexilAction {

    private SimpleVar handle;
    private ILExpression name;
    private List<ILExpression> args;
    private ILExpression assignResultTo;
    
    public CommandAction(SimpleVar handle, ILExpression name, 
            List<ILExpression> args, ILExpression assignResultTo) {
        PlexilType.COMMAND_HANDLE.typeCheck(handle.getType());
        PlexilType.STRING.typeCheck(name.getType());
    	this.handle = handle;
        this.name = name;
        this.args = args;
        this.assignResultTo = assignResultTo;
    }
    
    public List<ILExpression> getArgs() {
        return args;
    }
    
    public ILExpression getName() {
    	return name;
    }
    
    public SimpleVar getHandle() {
    	return handle;
    }
    
    public ILExpression getPossibleLeftHandSide() {
    	return assignResultTo;
    }
    
    @Override
    public String toString() {
        String ret = "Issue command: ";
        ret += name + "(";
        for (ILExpression arg : args) {
            ret += arg + ", ";
        }
        return ret.substring(0, ret.length() - 2) +")";
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitCommand(this, param);
	}

}
