package edu.umn.crisys.plexil.il.simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupOnChangeExpr;
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
import edu.umn.crisys.plexil.il.expr.AliasExpr;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILEval;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeEqual;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeEval;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExpr;
import edu.umn.crisys.plexil.il.expr.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.ILVarVisitor;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.SimpleCurrentNext;
import edu.umn.crisys.plexil.runtime.plx.SimplePArray;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
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
import edu.umn.crisys.plexil.test.java.PlanState;
import edu.umn.crisys.plexil.test.java.PlexilTestable;

public class ILSimulator extends JavaPlan implements PlexilTestable {

	private Plan ilPlan;
	private Map<Expression, SimpleCurrentNext<PValue>> simpleVars = new HashMap<>();
	private Map<Expression, SimplePArray<PValue>> arrayVars = new HashMap<>();
	private Map<NodeStateMachine, SimpleCurrentNext<Integer>> states = new HashMap<>();
	private NativeEval myNativeEval = new NativeEval() {

		@Override
		public Optional<Boolean> visitPlexilExprToNative(
				PlexilExprToNative pen, Void param) {
			return pen.getPlexilExpr().accept(myEvaluator, null)
					.map((v) -> pen.getCondition().checkValue(v));
		}
		
		@Override
		public Optional<Boolean> visitNativeEqual(NativeEqual e, Void param) {
			Optional<PValue> left = e.getLeft().accept(myEvaluator, null);
			Optional<PValue> right = e.getRight().accept(myEvaluator, null);
			
			if (left.isPresent() && right.isPresent()) {
				return Optional.of(left.get().equals(right.get()));
			} else {
				return Optional.empty();
			}
		}

		
	};
	
	private ILEval myEvaluator = new ILEval() {

		@Override
		public Optional<PValue> visitArrayIndex(ArrayIndexExpr array, Void param) {
			Optional<PValue> theArrayResult = array.getArray().accept(this, param);
			Optional<PValue> theIndexResult = array.getIndex().accept(this, param);
			
			if (theArrayResult.isPresent() && theIndexResult.isPresent()) {
				PValue theArray = theArrayResult.get();
				PValue theIndex = theIndexResult.get();
				if (theArray instanceof PValueList && theIndex instanceof PInteger) {
					return Optional.of(
							((PValueList<?>)theArray).get(((PInteger)theIndex)));
				}
			}
			return Optional.empty();
		}

		@Override
		public Optional<PValue> visitLookupNow(LookupNowExpr lookup, Void param) {
			return Optional.of(getWorld().lookupNow(asString(eval(lookup.getLookupName())), 
					lookup.getLookupArgs().stream().map(ILSimulator.this::eval)
					.collect(Collectors.toList()).toArray(new PValue[]{})));
		}

		@Override
		public Optional<PValue> visitLookupOnChange(LookupOnChangeExpr lookup, Void param) {
			return Optional.of(getWorld().lookupOnChange(asString(eval(lookup.getLookupName())), 
					asReal(eval(lookup.getTolerance())),
					lookup.getLookupArgs().stream().map(ILSimulator.this::eval)
					.collect(Collectors.toList()).toArray(new PValue[]{})));
		}

		@Override
		public Optional<PValue> visitRootParentState(RootParentStateExpr state, Void param) {
			return Optional.of(getInterface().evalParentState());
		}

		@Override
		public Optional<PValue> visitRootParentExit(RootAncestorExitExpr ancExit,
				Void param) {
			return Optional.of(getInterface().evalAncestorExit());
		}

		@Override
		public Optional<PValue> visitRootParentEnd(RootAncestorEndExpr ancEnd, Void param) {
			return Optional.of(getInterface().evalAncestorEnd());
		}

		@Override
		public Optional<PValue> visitRootParentInvariant(
				RootAncestorInvariantExpr ancInv, Void param) {
			return Optional.of(getInterface().evalAncestorInvariant());
		}


		@Override
		public Optional<PValue> visitSimple(SimpleVar var, Void param) {
			return Optional.of(simpleVars.get(var).getCurrent());
		}

		@Override
		public Optional<PValue> visitArray(ArrayVar array, Void param) {
			return Optional.of(arrayVars.get(array).getCurrent());
		}

		@Override
		public Optional<PValue> visitLibrary(LibraryVar lib, Void param) {
			return Optional.empty();
		}

		@Override
		public Optional<PValue> visitAlias(AliasExpr alias, Void param) {
			return Optional.empty();
		}

		@Override
		public Optional<PValue> visitGetNodeState(GetNodeStateExpr state, Void param) {
			return Optional.of(getCurrentStateOf(state.getNodeUid()));
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
			var.accept(new ILVarVisitor<Void,Void>() {

				@Override
				public Void visitSimple(SimpleVar varInfo, Void param) {
					SimpleCurrentNext<PValue> var = 
							new SimpleCurrentNext<PValue>(eval(varInfo.getInitialValue()));
					simpleVars.put(varInfo, var);
					return null;
				}

				@Override
				public Void visitArray(ArrayVar arrayInfo, Void param) {
					SimplePArray<PValue> array = new SimplePArray<PValue>(arrayInfo.getType(),
							arrayInfo.getMaxSize(), arrayInfo.getInitialValue());
					arrayVars.put(arrayInfo, array);
					return null;
				}

				@Override
				public Void visitLibrary(LibraryVar lib, Void param) {
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
		return ((ILExpression)e).accept(myEvaluator, null)
				.orElseThrow(() -> new RuntimeException("Couldn't eval "+e));
	}
	
	private boolean eval(NativeExpr e) {
		return e.accept(myNativeEval, null)
				.orElseThrow(() -> new RuntimeException("Couldn't eval "+e));
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
				Optional<ILExpression> maybeLhs = cmd.getPossibleLeftHandSide();
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
							// Commit it immediately, this is happening between
							// steps. 
							simpleVars.get(maybeLhs.get()).commit();
						} else {
							throw new RuntimeException("Tried to return to a Command "
									+ "node that doesn't have an assignment.");
						}
					}
				};

				getWorld().command(handle, asString(eval(cmd.getName())), 
						cmd.getArgs().stream().map(ILSimulator.this::eval)
						.collect(Collectors.toList()).toArray(new PValue[]{}));
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
						// Immediately commit, we're between steps when this happens
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
				if (t.start.equals(currentState) && eval(t.guard) ) {
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
				}
			}
			// Whatever state we're entering, execute its "in" actions
			State newState = nsm.getStates().get(states.get(nsm).getNext());
			newState.inActions.forEach(this::exec);
		}
		// Now notify listeners that the step is over.
		notifyMicroStep();
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
