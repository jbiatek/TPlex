package edu.umn.crisys.plexil.translator.il.action;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;

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

    @Override
    public String toString() {
        return "Revert "+var+" to previous value";
    }
    
    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        var.addAssignment(prev.directReference(cm), JExpr.lit(priority), block, cm);
        if (var.directReference(cm) != null) {
            block.invoke("commitAfterMacroStep").arg(var.directReference(cm));
            block.invoke("endMacroStep");
        }
    }
    
}
