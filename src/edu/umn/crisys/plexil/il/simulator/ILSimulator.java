package edu.umn.crisys.plexil.il.simulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.Plan;
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
import edu.umn.crisys.plexil.il.expr.CascadingExprVisitor;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILOperation;
import edu.umn.crisys.plexil.il.expr.ILOperator;
import edu.umn.crisys.plexil.il.expr.LookupExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExpr;
import edu.umn.crisys.plexil.il.expr.vars.ArrayVar;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.expr.vars.LibraryVar;
import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.jkind.results.ScriptRecorderFromLustreData;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.PlanState;
import edu.umn.crisys.plexil.runtime.plx.SimpleCurrentNext;
import edu.umn.crisys.plexil.runtime.plx.SimplePArray;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PInteger;
import edu.umn.crisys.plexil.runtime.values.PReal;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.world.CommandHandler;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import edu.umn.crisys.plexil.runtime.world.UpdateHandler;

public class ILSimulator extends JavaPlan {
	
	public static boolean LIMIT_TO_LUSTRE_FUNCTIONALITY = false;

	private Plan ilPlan;
	private Map<ILExpr, SimpleCurrentNext<PValue>> simpleVars = new HashMap<>();
	private Map<ILExpr, SimplePArray<PValue>> arrayVars = new HashMap<>();
	private Map<CommandAction, CommandHandler> commandHandlers = new HashMap<>();
	private Map<NodeStateMachine, SimpleCurrentNext<Integer>> states = new HashMap<>();
	
	/**
	 * This visitor goes through an expression replacing variables and inputs
	 * with their current values. 
	 */
	private ExprVisitor<Void,PValue> myResolver = new CascadingExprVisitor<Void, PValue>() {

		@Override
		public PValue visit(LookupExpr lookup, Void param) {
			PValue[] args = new PValue[]{};
			if (LIMIT_TO_LUSTRE_FUNCTIONALITY && ! lookup.getLookupArgs().isEmpty()) {
				System.err.println("WARNING: Lookup args are being dropped for Lustre compatibility!");
			} else {
				args = lookup.getLookupArgs().stream().map(ILSimulator.this::eval)
						.collect(Collectors.toList()).toArray(new PValue[]{});
			}
			
			if (lookup.getTolerance().isPresent()) {
				return getWorld().lookupOnChange(asString(eval(lookup.getLookupName())), 
						asReal(eval(lookup.getTolerance().get())),
						args);
			} else {
				return getWorld().lookupNow(asString(eval(lookup.getLookupName())), 
						args);
			}
		}

		@Override
		public PValue visit(RootAncestorExpr root, Void param) {
			switch (root) {
			case END:
				return getInterface().evalAncestorEnd();
			case EXIT:
				return getInterface().evalAncestorExit();
			case INVARIANT:
				return getInterface().evalAncestorInvariant();
			case STATE:
				return getInterface().evalParentState();
			default:
				throw new RuntimeException("missing case");
			}
		}

		@Override
		public PValue visit(SimpleVar var, Void param) {
			return simpleVars.get(var).getCurrent();
		}

		@Override
		public PValue visit(ArrayVar array, Void param) {
			return arrayVars.get(array).getCurrent();
		}

		@Override
		public PValue visit(GetNodeStateExpr state, Void param) {
			return getCurrentStateOf(state.getNodeUid());
		}
		

	};
	
	public ILSimulator(Plan ilPlan, ExternalWorld world) {
		super(world);
		init(ilPlan);
	}

	public void reset() {
		simpleVars.clear();
		arrayVars.clear();
		states.clear();
		init(ilPlan);
	}
	
	private void init(Plan ilPlan) {
		this.ilPlan = ilPlan;
		// Clean out the snapshots to get rid of optimized-out variables
		ilPlan.getOriginalHierarchy().removeDeletedVariables(ilPlan);
		
		for (ILVariable var : ilPlan.getVariables()) {
			var.accept(new CascadingExprVisitor<Void,Void>() {

				@Override
				public Void visit(SimpleVar varInfo, Void param) {
					SimpleCurrentNext<PValue> var = 
							new SimpleCurrentNext<PValue>(eval(varInfo.getInitialValue()));
					simpleVars.put(varInfo, var);
					return null;
				}

				@Override
				public Void visit(ArrayVar arrayInfo, Void param) {
					@SuppressWarnings("unchecked")
					SimplePArray<PValue> array = new SimplePArray<PValue>(
							arrayInfo.getMaxSize(), 
							(PValueList<PValue>) arrayInfo.getInitialValue());
					arrayVars.put(arrayInfo, array);
					return null;
				}

				@Override
				public Void visit(LibraryVar lib, Void param) {
					throw new RuntimeException("Libraries not supported");
				}
			
			}, null);
		}

		for (NodeStateMachine nsm : ilPlan.getMachines()) {
			// Each one starts in state 0
			states.put(nsm, new SimpleCurrentNext<Integer>(0));
			nsm.orderTransitionsByPriority();
		}
	}
	
	/**
	 * Evaluate the given expression inside of this simulator. Variables and 
	 * inputs will be evaluated based on the current state of the simulation.
	 * @param e
	 * @return
	 */
	public PValue eval(ILExpr e) {
		if (e instanceof PValue) return (PValue) e;
		
		PValue v = e.eval(expr -> Optional.of(expr.accept(myResolver)))
				.orElseThrow(() -> 
				new RuntimeException("Couldn't eval "+e));
		// Make sure that the value is the same type
		e.getType().strictTypeCheck(v);
		
		return v;
		
	}
	
	/**
	 * Evaluate a NativeBool expression, and return it as a native Java boolean.
	 * @param e
	 * @return
	 */
	private boolean evalNative(ILExpr e) {
		return ((NativeBool) eval(e)).getValue();
	}
	
	/**
	 * Perform an assignment inside the IL. This method finds the correct 
	 * variable for the LHS expression and assigns it the value of the right
	 * hand side. This method can handle SimpleVars, ArrayVars, and array
	 * assignments. It does not handle assigning to aliases yet. 
	 * 
	 * If told to commit immediately, it will. If not, it will be queued to be
	 * committed at the end of this micro step. 
	 * 
	 * @param lhs
	 * @param rhs
	 * @param commitImmediately
	 * @return
	 */
	private void setNext(ILExpr lhs, ILExpr rhs, boolean commitImmediately) {
		// Find this variable and make the assignment
		if (lhs instanceof SimpleVar) {
			SimpleVar varInfo = (SimpleVar) lhs;
			PValue rightValue = eval(rhs);
			varInfo.getType().strictTypeCheck(rightValue);
			
			simpleVars.get(lhs).setNext(rightValue);
			if (commitImmediately) {
				simpleVars.get(lhs).commit();
			} else {
				commitAfterMicroStep(simpleVars.get(lhs));
			}
		} else if (lhs instanceof ArrayVar) {
			ArrayVar varInfo = (ArrayVar) lhs;
			PValue rightValue = eval(rhs);
			varInfo.getType().strictTypeCheck(rightValue);
			
			arrayVars.get(lhs).arrayAssign(eval(rhs));
			if (commitImmediately) {
				arrayVars.get(lhs).commit();
			} else {
				commitAfterMicroStep(arrayVars.get(lhs));
			}
		} else if (lhs instanceof ILOperation) {
			// This will be an array index of some sort.
			ILOperation arrayIndex = (ILOperation) lhs;
			if (arrayIndex.getOperator() != ILOperator.PBOOL_INDEX
					&& arrayIndex.getOperator() != ILOperator.PINT_INDEX
					&& arrayIndex.getOperator() != ILOperator.PREAL_INDEX
					&& arrayIndex.getOperator() != ILOperator.PSTRING_INDEX) {
				throw new RuntimeException("Assigning to "+arrayIndex+" not supported");
			}
			PInteger index = asInt(eval(arrayIndex.getBinarySecond()));
			PValue value = eval(rhs);
			
			// Ensure that this value fits in arrays of this type.
			arrayIndex.getType().strictTypeCheck(value);
			
			arrayVars.get(arrayIndex.getBinaryFirst())
				.indexAssign(index, value);
			if (commitImmediately) {
				arrayVars.get(arrayIndex.getBinaryFirst())
					.commit();
			} else {
				commitAfterMicroStep(arrayVars.get(arrayIndex.getBinaryFirst()));
			}
		} else {
			throw new RuntimeException("Handle "+lhs.getClass()+" here");
			// And don't forget to strictTypeCheck() it first. 
		}
	}

	
	private void exec(PlexilAction a) {
		a.accept(new ILActionVisitor<Void, Void>() {

			@Override
			public Void visitAlsoRunNodes(AlsoRunNodesAction run, Void param) {
				// No need, we just run all nodes every time
				return null;
			}
			

			@Override
			public Void visitAssign(AssignAction assign, Void param) {
				setNext(assign.getLHS(), assign.getRHS(), false);
				return null;
			}

			@Override
			public Void visitCommand(CommandAction cmd, Void param) {
				// Wrap this command up in something that the external world 
				// can use to interact with it. 

				CommandHandler handler = new CommandHandler() {

					@Override
					public void setCommandHandle(CommandHandleState state) {
						// Apply this change immediately
						setNext(cmd.getHandle(), state, true);
					}

					@Override
					public void commandReturns(PValue value) {
						ILExpr lhs = cmd.getPossibleLeftHandSide()
								.orElseThrow(() -> new RuntimeException(
								"Environment is trying to have "+cmd+" return"
							+ " a value, but no left-hand side is present."));
						// Assign it and commit immediately, since we should 
						// be between macro steps. 
						setNext(lhs, value, true);
					}

					@Override
					public void acknowledgeAbort() {
						setNext(cmd.getAckFlag(), NativeBool.TRUE, true);
					}
				};
				
				// Save this handler for later reference if necessary.
				commandHandlers.put(cmd, handler);
				
				PString commandName = asString(eval(cmd.getName()));
				if (JavaPlan.DEBUG) {
					System.out.println("**** Issuing command "+commandName);
				}
				PValue[] args = new PValue[]{};
				if (LIMIT_TO_LUSTRE_FUNCTIONALITY && ! cmd.getArgs().isEmpty()) {
					System.err.println("WARNING: Arguments are being dropped for Lustre compatibility!!!!");
				} else {
					args = cmd.getArgs().stream().map(ILSimulator.this::eval)
							.collect(Collectors.toList()).toArray(new PValue[]{});
				}
				
				if (getWorld() instanceof ScriptRecorderFromLustreData) {
					// This needs to be told the *name* of the node in addition
					// to all the other stuff. 
					ScriptRecorderFromLustreData rec = (ScriptRecorderFromLustreData) getWorld();
					rec.specialCommand(cmd.getHandle(), handler, commandName, args);
				} else {
					getWorld().command(handler, commandName, args);
				}
				return null;
			}
			
			

			@Override
			public Void visitAbortCommand(AbortCommand abort, Void param) {
				// Get the handle that this command sent out originally
				CommandHandler handler = commandHandlers.get(abort.getOriginalCommand());
				// Tell the external world to abort
				getWorld().commandAbort(handler);
				return null;
			}


			@Override
			public Void visitEndMacroStep(EndMacroStep end, Void param) {
				endMacroStep();
				return null;
			}

			@Override
			public Void visitRunLibraryNode(RunLibraryNodeAction lib, Void param) {
				throw new RuntimeException("Libraries not supported");
			}

			@Override
			public Void visitComposite(CompositeAction composite, Void param) {
				composite.getActions().forEach(act -> act.accept(this, null));
				return null;
			}

			@Override
			public Void visitUpdate(UpdateAction update, Void param) {
				UpdateHandler handle = new UpdateHandler() {

					@Override
					public void acknowledgeUpdate() {
						// Commit this immediately since we're between steps
						setNext(update.getHandle(), BooleanValue.get(true), true);
					}

					@Override
					public String getNodeName() {
						return update.getShortName();
					}

				};
				update.getUpdates().forEach( pair -> {
					getWorld().update(handle, pair.first, eval(pair.second));
				});
				return null;
			}

		}, null);
	}

	private PString asString(PValue v) {
		return ((PString) v);
	}
	
	private PReal asReal(PValue v) {
		return (PReal) v;
	}
	
	private PInteger asInt(PValue v) {
		return (PInteger) v;
	}

	private NodeState getCurrentStateOf(NodeUID uid) {
		NodeStateMachine nsm = findContainingMachineFor(uid);
		Integer currentState = states.get(nsm).getCurrent();
		return nsm.getStates().get(currentState).tags.get(uid);
	}
	
	private NodeStateMachine findContainingMachineFor(NodeUID uid) {
		return ilPlan.getMachines().stream()
		.filter(nsm -> nsm.getNodeIds().contains(uid))
		.findFirst()
		.orElseThrow(() -> new RuntimeException("Couldn't find UID "+uid));
	}
	
	private List<ILSimObserver> getSimObservers() {
		return getObservers().stream()
				.filter(o -> o instanceof ILSimObserver)
				.map(o -> (ILSimObserver) o)
				.collect(Collectors.toList());
	}
	
	private void notifyTransitionTaken(Transition t) {
		getSimObservers().forEach(o -> o.transitionTaken(t));
	}
	
	@Override
	public void doMicroStep() {
		for (NodeStateMachine nsm : states.keySet()) {
			// Run this state machine
			State currentState = nsm.getStates().get(states.get(nsm).getCurrent());
			for (Transition t : nsm.getTransitions()) {
				if (t.start.equals(currentState) && evalNative(t.guard) ) {
					if (JavaPlan.DEBUG) {
						System.out.println(t.toString());
					}
					// Perform this transition's actions
					t.actions.forEach(this::exec);
					// Perform actions for the new state
					t.end.entryActions.forEach(this::exec);
					// Arrive in the new state
					states.get(nsm).setNext(t.end.getIndex());
					// ... when the step ends
					commitAfterMicroStep(states.get(nsm));
					// Notify that this transition was taken
					notifyTransitionTaken(t);
					// Finally, notify that we didn't quiesce
					changeOccurred();
					// And stop trying to find new transitions.
					break;
				}
			}
			// Whatever state we're entering, execute its "in" actions
			State newState = nsm.getStates().get(states.get(nsm).getNext());
			newState.inActions.forEach(this::exec);
		}
	}
	@Override
	public NodeOutcome getRootNodeOutcome() {
		return (NodeOutcome) simpleVars.get(
				(SimpleVar)ilPlan.getRootNodeOutcome()).getCurrent();
	}
	
	@Override
	public NodeState getRootNodeState() {
		return (NodeState) eval(ilPlan.getRootNodeState());
	}

	@Override
	public PlanState getSnapshot() {
		return getPlanStateOf(ilPlan.getOriginalHierarchy(), null);
	}

	private PlanState getPlanStateOf(OriginalHierarchy node,
			PlanState parent) {
		PlanState state;
		if (parent == null) {
			state = new PlanState(node.getUID().getShortName());
		} else {
			state = new PlanState(node.getUID().getShortName(), parent);
		}
		
		for (String varName : node.getVariables().keySet()) {
			state.addVariable(varName, eval(node.getVariables().get(varName)));
		}
		for (OriginalHierarchy child : node.getChildren()) {
			state.addChild(getPlanStateOf(child, state));
		}
		if (node.getLibraryChild().isPresent()) {
			throw new RuntimeException("Libraries not supported");
		}
		return state;
	}
}
