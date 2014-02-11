package edu.umn.crisys.plexil.translator.il.action;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;

import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.translator.il.NodeUID;
import edu.umn.crisys.plexil.translator.il.vars.VariableReference;

public class SetOutcomeAction implements PlexilAction {

    private VariableReference outcomeVar;
    private VariableReference failureVar;
    private String nodeName;
    private NodeOutcome theOutcome;
    private NodeFailureType theFailure = NodeFailureType.UNKNOWN; 
    
    public SetOutcomeAction(NodeUID uid,
                            VariableReference outcomeVar, NodeOutcome outcome, 
                            VariableReference failureVar, NodeFailureType failure) {
        this.theOutcome = outcome;
        this.theFailure = failure;
        this.nodeName = uid.getShortName();
        this.outcomeVar = outcomeVar;
        this.failureVar = failureVar;
    }

    @Override
    public String toString() {
        String ret = "Set outcome of "+nodeName+" to "+theOutcome;
        if (theFailure != NodeFailureType.UNKNOWN) {
            ret += ", failure type "+theFailure;
        }
        return ret;
    }
    
    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        outcomeVar.addAssignment(cm.ref(NodeOutcome.class).staticRef(theOutcome.toString()), JExpr.lit(1), block, cm);
        block.invoke("commitAfterMicroStep").arg(outcomeVar.directReference(cm));
        if (theFailure != NodeFailureType.UNKNOWN) {
            failureVar.addAssignment(cm.ref(NodeFailureType.class).staticRef(theFailure.toString()), JExpr.lit(1), block, cm);
            block.invoke("commitAfterMicroStep").arg(failureVar.directReference(cm));
        }
    }
    
}
