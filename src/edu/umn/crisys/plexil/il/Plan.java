/**
 * @author Whalen
 *
 */

package edu.umn.crisys.plexil.il;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.globaldecl.CommandDecl;
import edu.umn.crisys.plexil.ast.globaldecl.LibraryDecl;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.il.action.ILActionVisitor;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExprModifier;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.vars.ILVariable;

public class Plan {
    
	private List<NodeStateMachine> stateMachines = new LinkedList<NodeStateMachine>(); 
	private Set<ILVariable> variables = new HashSet<ILVariable>();
	
    /**
     * Commands declared in the original Plexil plan
     */
	private List<CommandDecl> commandDecls = new ArrayList<CommandDecl>();
    
    /**
     * State declarations (aka lookups) declared in the original Plexil plan
     */
	private List<LookupDecl> stateDecls = new ArrayList<LookupDecl>();
    
    /**
     *  Library node declarations from the original Plexil plan
     */
	private List<LibraryDecl> libraryDecls = new ArrayList<LibraryDecl>();

	private OriginalHierarchy snapshot;
	
	private NodeStateMachine root;
	private ILVariable rootOutcome;
	private GetNodeStateExpr rootState;
    private String planName;
    private boolean isTopLevelPlan = false;
    
    public Plan(String planName) {
	    this.planName = planName;
	}
    
    @Override
    public String toString() {
    	return planName;
    }
    
    public List<LibraryDecl> getLibraryDecls() {
		return libraryDecls;
	}

	public void setLibraryDecls(List<LibraryDecl> libraryDecls) {
		this.libraryDecls = libraryDecls;
	}

	public List<LookupDecl> getStateDecls() {
		return stateDecls;
	}

	public void setStateDecls(List<LookupDecl> stateDecls) {
		this.stateDecls = stateDecls;
	}

	public List<CommandDecl> getCommandDecls() {
		return commandDecls;
	}

	public void setCommandDecls(List<CommandDecl> commandDecls) {
		this.commandDecls = commandDecls;
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
	
	public <P> void visitAllActions(ILActionVisitor<P,?> visitor, P param) {
	    for (NodeStateMachine sm : getMachines()) {
	        for (Transition t : sm.getTransitions()) {
	            for (PlexilAction a : t.actions) {
	            	a.accept(visitor, param);
	            }
	        }
	        for (State s : sm.getStates()) {
	            for (PlexilAction a : s.entryActions) {
	                a.accept(visitor, param);
	            }
	            for (PlexilAction a : s.inActions) {
	                a.accept(visitor, param);
	            }
	        }
	    }
	}
	
	public <P> void filterActions(ILActionVisitor<P,Boolean> visitor, final P param) {
		getMachines().stream().forEach(
				nsm -> {
					nsm.getTransitions().stream().forEach(
						t -> t.actions.removeIf(a-> ! a.accept(visitor, param)));
					nsm.getStates().stream().forEach(
						s -> {
							s.entryActions.removeIf(a -> ! a.accept(visitor, param));
							s.inActions.removeIf(a -> ! a.accept(visitor, param));
						});
				}
				);

		return;
	}
	
	public <Param> void modifyAllExpressions(ILExprModifier<Param> visitor, Param param) {
		for (NodeStateMachine nsm : getMachines()) {
			for (Transition t : nsm.getTransitions()) {
				t.guard = t.guard.accept(visitor, param);
			}
		}

	}

	public OriginalHierarchy getOriginalHierarchy() {
		return snapshot;
	}

	public void setOriginalHierarchy(OriginalHierarchy snapshot) {
		this.snapshot = snapshot;
	}
}
