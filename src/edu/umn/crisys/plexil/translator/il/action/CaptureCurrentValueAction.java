package edu.umn.crisys.plexil.translator.il.action;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;

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
    
    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        // Capture the old value for possibly reverting later
        block.assign(prev.directReference(cm), var.rhs(cm));
    }

}
