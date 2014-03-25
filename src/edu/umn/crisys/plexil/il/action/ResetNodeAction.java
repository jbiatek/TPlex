package edu.umn.crisys.plexil.il.action;

import java.util.LinkedList;
import java.util.List;

import edu.umn.crisys.plexil.il.vars.ILVariable;

public class ResetNodeAction implements PlexilAction {
    
    private List<ILVariable> varsToReset = new LinkedList<ILVariable>();
    
    public void addVariableToReset(ILVariable var) {
        varsToReset.add(var);
    }
    
    @Override
    public String toString() {
        String ret = "Reset variables: ";
        for (ILVariable v : varsToReset) {
            ret += v + ", ";
        }
        return ret.substring(0, ret.length() - 1);
    }

	public List<ILVariable> getVars() {
		return varsToReset;
	}

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitResetNode(this, param);
	}
}
