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
import edu.umn.crisys.plexil.il.action.AbortCommand;
import edu.umn.crisys.plexil.il.action.AlsoRunNodesAction;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.CompositeAction;
import edu.umn.crisys.plexil.il.action.EndMacroStep;
import edu.umn.crisys.plexil.il.action.ILActionVisitor;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprModifier;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
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
	
	public <P> void visitAllExpressions(ILExprVisitor<P, ?> visitor, P param) {
		visitAllGuards(visitor, param);
		// We also want to visit expressions inside actions.
		ILActionVisitor<P,Void> exprVisitor = new ILActionVisitor<P, Void>() {

			@Override
			public Void visitAlsoRunNodes(AlsoRunNodesAction run, P param) {
				// No expressions in these
				return null;
			}

			@Override
			public Void visitAssign(AssignAction assign, P param) {
				assign.getLHS().accept(visitor, param);
				assign.getRHS().accept(visitor, param);
				return null;
			}

			@Override
			public Void visitCommand(CommandAction cmd, P param) {
				cmd.getName().accept(visitor, param);
				cmd.getArgs().forEach(e -> e.accept(visitor, param));
				cmd.getHandle().accept(visitor, param);
				cmd.getAckFlag().accept(visitor, param);
				cmd.getPossibleLeftHandSide().ifPresent(e -> e.accept(visitor, param));
				return null;
			}

			@Override
			public Void visitEndMacroStep(EndMacroStep end, P param) {
				// No expressions here. 
				return null;
			}

			@Override
			public Void visitRunLibraryNode(RunLibraryNodeAction lib, P param) {
				lib.getLibNode().accept(visitor, param);
				lib.getLibNode().getAliases().values().forEach(e -> e.accept(visitor, param));
				return null;
			}

			@Override
			public Void visitComposite(CompositeAction composite, P param) {
				composite.getActions().forEach(a -> a.accept(this, param));
				return null;
			}

			@Override
			public Void visitUpdate(UpdateAction update, P param) {
				update.getUpdates().forEach(pair -> pair.second.accept(visitor, param));
				update.getHandle().accept(visitor, param);
				return null;
			}

			@Override
			public Void visitAbortCommand(AbortCommand abort, P param) {
				// This has no expressions, just a pointer to the original
				// command action. 
				abort.getOriginalCommand().accept(this, param);
				return null;
			}
		};
		visitAllActions(exprVisitor, param);
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
