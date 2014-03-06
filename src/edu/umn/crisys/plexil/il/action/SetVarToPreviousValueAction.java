package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;
import edu.umn.crisys.plexil.translator.il.vars.PreviousValueReference;

public class SetVarToPreviousValueAction implements PlexilAction {

    private int priority;
    private IntermediateVariable var;
    private PreviousValueReference prev;
    
    public SetVarToPreviousValueAction(IntermediateVariable var, PreviousValueReference prev, int priority) {
	    this.var = var;
	    this.priority = priority;
	    this.prev = prev;
	}

	public int getPriority() {
		return priority;
	}

	public IntermediateVariable getVar() {
		return var;
	}

	public PreviousValueReference getPrev() {
		return prev;
	}

	@Override
    public String toString() {
        return "Revert "+var+" to previous value";
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitSetVarToPreviousValue(this, param);
	}
    
    
}
