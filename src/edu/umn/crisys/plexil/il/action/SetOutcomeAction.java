package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
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


	public VariableReference getOutcomeVar() {
		return outcomeVar;
	}

	public VariableReference getFailureVar() {
		return failureVar;
	}
	
	public String getNodeName() {
		return nodeName;
	}

	public NodeOutcome getOutcome() {
		return theOutcome;
	}

	public NodeFailureType getFailure() {
		return theFailure;
	}


	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitSetOutcome(this, param);
	}
    
}
