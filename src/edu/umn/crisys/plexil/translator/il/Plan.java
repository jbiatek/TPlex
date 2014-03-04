/**
 * @author Whalen
 *
 */

package edu.umn.crisys.plexil.translator.il;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.il.optimize.PruneUnusedTimepoints;
import edu.umn.crisys.plexil.il.optimize.RemoveDeadTransitions;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il2java.StateMachineToJava;
import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.LibraryInterface;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;
import edu.umn.crisys.plexil.translator.il.vars.NodeStateReference;

public class Plan {


    
	private List<NodeStateMachine> stateMachines = new LinkedList<NodeStateMachine>(); 
	private Set<IntermediateVariable> variables = new HashSet<IntermediateVariable>();
	
    public Set<String> globalVarNameList = new HashSet<String>();
	
	public NodeStateMachine root;
	public IntermediateVariable rootOutcome;
	public NodeStateReference rootState;
    public String planName;
    
    public Plan(String planName) {
	    this.planName = planName;
	}
    
    public List<NodeStateMachine> getMachines() {
    	return stateMachines;
    }
    
    public Set<IntermediateVariable> getVariables() {
    	return variables;
    }
	

	

	public void addVariable(IntermediateVariable var) {
	    variables.add(var);
	}
	
	public void addVariables(List<? extends IntermediateVariable> vars) {
	    variables.addAll(vars);
	}
	
	public void addStateMachine(NodeStateMachine nsm) {
	    stateMachines.add(nsm);
	}
	
	public void setRoot(NodeStateMachine nsm, NodeStateReference state, IntermediateVariable outcome) {
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
	
}
