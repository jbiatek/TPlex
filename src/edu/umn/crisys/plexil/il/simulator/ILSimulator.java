package edu.umn.crisys.plexil.il.simulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.expr.CascadingExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILExprModifier;
import edu.umn.crisys.plexil.expr.il.RootAncestorExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AlsoRunNodesAction;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.CompositeAction;
import edu.umn.crisys.plexil.il.action.EndMacroStep;
import edu.umn.crisys.plexil.il.action.ILActionVisitor;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
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
	private Map<Expression, SimpleCurrentNext<PValue>> simpleVars = new HashMap<>();
	private Map<Expression, SimplePArray<PValue>> arrayVars = new HashMap<>();
	private Map<NodeStateMachine, SimpleCurrentNext<Integer>> states = new HashMap<>();
	
	private ILExprModifier<Void> myResolver = new ILExprModifier<Void>() {

		@Override
		public Expression visit(Expression e, Void param) {
			return e.getCloneWithArgs(e.getArguments().stream()
					.map(arg -> arg.accept(this))
					.collect(Collectors.toList()));
		}
		
		@Override
		public Expression visit(ArrayIndexExpr array, Void param) {
			PValue theArray = ILSimulator.this.eval(array.getArray());
			PValue theIndex = ILSimulator.this.eval(array.getIndex());

			if (theArray instanceof PValueList && theIndex instanceof PInteger) {
				return ((PValueList<?>)theArray).get(((PInteger)theIndex));
			} else {
				throw new RuntimeException("Tried to get index "+theIndex+" of "+theArray);
			}
		}

		@Override
		public Expression visit(LookupNowExpr lookup, Void param) {
			PValue[] args = new PValue[]{};
			if (LIMIT_TO_LUSTRE_FUNCTIONALITY && ! lookup.getLookupArgs().isEmpty()) {
				System.err.println("WARNING: Lookup args are being dropped for Lustre compatibility!");
			} else {
				args = lookup.getLookupArgs().stream().map(ILSimulator.this::eval)
						.collect(Collectors.toList()).toArray(new PValue[]{});
			}
			
			return getWorld().lookupNow(asString(eval(lookup.getLookupName())), 
					args);
		}

		@Override
		public Expression visit(LookupOnChangeExpr lookup, Void param) {
			PValue[] args = new PValue[]{};
			if (LIMIT_TO_LUSTRE_FUNCTIONALITY && ! lookup.getLookupArgs().isEmpty()) {
				System.err.println("WARNING: Lookup args are being dropped for Lustre compatibility!");
			} else {
				args = lookup.getLookupArgs().stream().map(ILSimulator.this::eval)
						.collect(Collectors.toList()).toArray(new PValue[]{});
			}
			
			return getWorld().lookupOnChange(asString(eval(lookup.getLookupName())), 
					asReal(eval(lookup.getTolerance())),
					args);
		}

		@Override
		public Expression visit(RootAncestorExpr root, Void param) {
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
		public Expression visit(SimpleVar var, Void param) {
			return simpleVars.get(var).getCurrent();
		}

		@Override
		public Expression visit(ArrayVar array, Void param) {
			return arrayVars.get(array).getCurrent();
		}

		@Override
		public Expression visit(GetNodeStateExpr state, Void param) {
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
	
	public PValue eval(Expression e) {
		return e.accept(myResolver).eval()
				.orElseThrow(() -> new RuntimeException("Couldn't eval "+e));
	}
	
	private boolean evalNative(Expression e) {
		return ((NativeBool) eval(e)).getValue();
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
				// Find this variable and make the assignment
				if (assign.getLHS() instanceof SimpleVar) {
					simpleVars.get(assign.getLHS()).setNext(
							eval(assign.getRHS()));
					commitAfterMicroStep(simpleVars.get(assign.getLHS()));
				} else if (assign.getLHS() instanceof ArrayVar) {
					arrayVars.get(assign.getLHS()).arrayAssign(
							eval(assign.getRHS()));
					commitAfterMicroStep(arrayVars.get(assign.getLHS()));
				} else if (assign.getLHS() instanceof ArrayIndexExpr) {
					ArrayIndexExpr lhs = (ArrayIndexExpr) assign.getLHS();
					PInteger index = asInt(eval(lhs.getIndex()));
					arrayVars.get(lhs.getArray()).indexAssign(index, 
							eval(assign.getRHS()));
					
					commitAfterMicroStep(arrayVars.get(lhs.getArray()));
				} else {
					throw new RuntimeException("Handle "+assign.getLHS().getClass()+" here");
				}
				return null;
			}

			@Override
			public Void visitCommand(CommandAction cmd, Void param) {
				SimpleCurrentNext<PValue> cmdHandleVar =  simpleVars.get(cmd.getHandle());
				Optional<Expression> maybeLhs = cmd.getPossibleLeftHandSide();
				// Wrap this command up in something that the external world 
				// can use to interact with it. 

				CommandHandler handle = new CommandHandler() {

					@Override
					public void setCommandHandle(CommandHandleState state) {
						// Apply this change immediately
						cmdHandleVar.setNext(state);
						cmdHandleVar.commit();
					}

					@Override
					public void commandReturns(PValue value) {
						if (maybeLhs.isPresent()) {
							// Just assuming it's a simple var :P
							simpleVars.get(maybeLhs.get()).setNext(value);
							// Commit it now, we're between steps
							simpleVars.get(maybeLhs.get()).commit();;
						} else {
							throw new RuntimeException("Tried to return to a Command "
									+ "node that doesn't have an assignment.");
						}
					}
				};
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
					rec.specialCommand(cmd.getHandle(), handle, commandName, args);
				} else {
					getWorld().command(handle, commandName, args);
				}
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
						simpleVars.get(update.getHandle()).setNext(BooleanValue.get(true));
						// Commit this immediately since we're between steps
						simpleVars.get(update.getHandle()).commit();
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
