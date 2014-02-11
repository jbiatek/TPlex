package edu.umn.crisys.plexil.translator.il.action;

import java.util.LinkedList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;

import edu.umn.crisys.plexil.translator.il.vars.ArrayReference;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;
import edu.umn.crisys.plexil.translator.il.vars.VariableReference;

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
    
    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        for (IntermediateVariable var : varsToReset) {
            var.reset(block, cm);
            if (var instanceof VariableReference 
                    || var instanceof ArrayReference) {
                block.invoke("commitAfterMicroStep").arg(var.directReference(cm));
            }
        }
    }
}
