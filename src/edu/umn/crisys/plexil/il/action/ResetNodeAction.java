package edu.umn.crisys.plexil.il.action;

import java.util.LinkedList;
import java.util.List;

import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;

public class ResetNodeAction implements PlexilAction {
    
    private List<IntermediateVariable> varsToReset = new LinkedList<IntermediateVariable>();
    
    public void addVariableToReset(IntermediateVariable var) {
        varsToReset.add(var);
    }
    
    @Override
    public String toString() {
        String ret = "Reset variables: ";
        for (IntermediateVariable v : varsToReset) {
            ret += v + ", ";
        }
        return ret.substring(0, ret.length() - 1);
    }

	public List<IntermediateVariable> getVars() {
		return varsToReset;
	}

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitResetNode(this, param);
	}
}
