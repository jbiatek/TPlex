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

import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.ast.globaldecl.CommandDecl;
import edu.umn.crisys.plexil.ast.globaldecl.LibraryDecl;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.ast.globaldecl.PlexilInterface;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.il.action.ILActionVisitor;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprModifier;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;

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

	/**
	 * Top level interface from the original Plexil plan. 
	 */
	private PlexilInterface pInterface = new PlexilInterface();
	
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
    
    public String printFullPlan() {
    	StringBuilder sb = new StringBuilder();
    	
    	variables.forEach(v -> sb.append("IL Variable: "+v+"\n"));
    	stateMachines.forEach(nsm -> sb.append(nsm.toLongString()));
    	
    	return sb.toString();
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
	
	public void setTimeIfNotPresent(PlexilType type) {
		if (stateDecls.stream()
				.anyMatch(d -> d.getName().equals("time"))) {
			return;
		}
		LookupDecl time = new LookupDecl("time");
		time.setReturnValue(new VariableDecl("<timetype>", type));
		stateDecls.add(time);
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

	public ILExpr getRootNodeOutcome() {
		return rootOutcome;
	}

	public ILExpr getRootNodeState() {
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
	
	public <P> void visitAllGuards(ILExprVisitor<P, ?> visitor, P param) {
		for (NodeStateMachine nsm : getMachines()) {
			for (Transition t : nsm.getTransitions()) {
				t.guard.accept(visitor, param);
			}
		}
	}
	
	public <Param> void modifyAllGuards(ILExprModifier<Param> visitor, Param param) {
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

	public PlexilInterface getRootPlexilInterface() {
		return pInterface;
	}

	public void setRootPlexilInterface(PlexilInterface pInterface) {
		this.pInterface = pInterface;
	}
}
