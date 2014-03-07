package edu.umn.crisys.plexil.il.action;

import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.translator.il.vars.CommandHandleReference;

public class CommandAction implements PlexilAction {

    private CommandHandleReference handle;
    private ILExpression name;
    private List<ILExpression> args;
    
    public CommandAction(CommandHandleReference handle, ILExpression name, 
            List<ILExpression> args) {
        this.handle = handle;
        this.name = name;
        PlexilType.STRING.typeCheck(name.getType());
        this.args = args;
    }
    
    public List<ILExpression> getArgs() {
        return args;
    }
    
    public ILExpression getName() {
    	return name;
    }
    
    public CommandHandleReference getHandle() {
    	return handle;
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
