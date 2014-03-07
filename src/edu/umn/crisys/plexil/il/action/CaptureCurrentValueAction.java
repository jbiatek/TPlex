package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;
import edu.umn.crisys.plexil.translator.il.vars.PreviousValueReference;

public class CaptureCurrentValueAction implements PlexilAction {

    private IntermediateVariable var;
    private PreviousValueReference prev;
    
    public CaptureCurrentValueAction(IntermediateVariable v, PreviousValueReference prev) {
        this.var = v;
        this.prev = prev;
    }
    
    @Override
    public String toString() {
        return "Capture current value of "+var;
    }
    
    public IntermediateVariable getVar() {
    	return var;
    }
    
    public PreviousValueReference getPreviousRef() {
    	return prev;
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitCaptureCurrentValue(this, param);
	}
    
}
