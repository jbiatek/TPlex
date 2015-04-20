/**
 * @author Whalen
 *
 */

package edu.umn.crisys.plexil.il;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.vars.ILVariable;

public class Plan {
    
	private List<NodeStateMachine> stateMachines = new LinkedList<NodeStateMachine>(); 
	private Set<ILVariable> variables = new HashSet<ILVariable>();
	
	private NodeStateMachine root;
	private ILVariable rootOutcome;
	private GetNodeStateExpr rootState;
    private String planName;
    private boolean isTopLevelPlan = false;
    
    public Plan(String planName) {
	    this.planName = planName;
	}
    
    public List<NodeStateMachine> getMachines() {
    	return stateMachines;
    }
    
    public Set<ILVariable> getVariables() {
    	return variables;
    }
	
    public NodeStateMachine getRootMachine() {
    	return root;
    }

	public void addVariable(ILVariable var) {
	    variables.add(var);
	}
	
	public void addVariables(List<? extends ILVariable> vars) {
	    variables.addAll(vars);
	}
	
	public void addStateMachine(NodeStateMachine nsm) {
	    stateMachines.add(nsm);
	}
	
	public void setRoot(NodeStateMachine nsm, GetNodeStateExpr state, ILVariable outcome) {
	    root = nsm;
	    rootOutcome = outcome;
	    rootState = state;
	}
	
	public NodeStateMachine findMachineForNode(NodeUID nodeId) {
	    for (NodeStateMachine nsm : stateMachines) {
	        if (nsm.getNodeIds().contains(nodeId)) {
	            return nsm;
	        }
	    }
	    throw new RuntimeException("Node ID "+nodeId+" isn't claimed by any state machine.");
	}

	public String getPlanName() {
		return planName;
	}

	public ILExpression getRootNodeOutcome() {
		return rootOutcome;
	}

	public ILExpression getRootNodeState() {
		return rootState;
	}

	public boolean isTopLevelPlan() {
		return isTopLevelPlan;
	}

	public void setTopLevelPlan(boolean isTopLevelPlan) {
		this.isTopLevelPlan = isTopLevelPlan;
	}
	
}
