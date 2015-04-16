package edu.umn.crisys.plexil.ast2il;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umn.crisys.plexil.ast.Node;
import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.expr.common.Operation;
import edu.umn.crisys.plexil.ast.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CompositeAction;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExpr;
import edu.umn.crisys.plexil.il.expr.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.il.expr.nativebool.PlexilExprToNative.Condition;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.NodeTimepoint;
import edu.umn.crisys.util.Pair;

public class StateMachineBuilder {
	
    public static enum Description {
        START_CONDITION,
        SKIP_CONDITION,
        PRE_CONDITION,
        INVARIANT_CONDITION,
        REPEAT_CONDITION,
        POST_CONDITION,
        END_CONDITION,
        EXIT_CONDITION,

        ANCESTOR_ENDS_DISJOINED,
        ANCESTOR_EXITS_DISJOINED,
        ANCESTOR_INVARIANTS_CONJOINED,
        FAILURE_IS_PARENT_EXIT,
        FAILURE_IS_PARENT_FAIL,
        PARENT_EXECUTING,
        PARENT_WAITING,
        PARENT_FINISHED,
        
        COMMAND_ABORT_COMPLETE,
        COMMAND_ACCEPTED,
        COMMAND_DENIED,
        
        ALL_CHILDREN_FINISHED,
        ALL_CHILDREN_WAITING_OR_FINISHED,
        
        UPDATE_INVOCATION_SUCCESS;
        
        public String niceString() {
            String enumStr = toString();
            return Character.toUpperCase(enumStr.charAt(0))
                 + enumStr.substring(1).toLowerCase().replaceAll("_", " ");
        }
        
    }

	private NodeToIL translator;
	private Node astNode;
    private Map<Description, ILExpression> ilExprCache = 
            new HashMap<Description, ILExpression>();

	
	public StateMachineBuilder(NodeToIL translator, Node node) {
		this.translator = translator;
		this.astNode = node;
	}
	
	public void addAllTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        this.addInactiveTransitions(nsm, map);
        this.addWaitingTransitions(nsm, map);
        this.addExecutingTransitions(nsm, map);
        this.addFinishingTransitions(nsm, map);
        this.addFailingTransitions(nsm, map);
        this.addIterationEndedTransitions(nsm, map);
        this.addFinishedTransitions(nsm, map);
	}
	
    /*
     * Node state transition diagrams -----------------------------------------
     */
    
    public void addInactiveTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        // TODO: Update this to the new semantics.
        nsm.addTransition(makeTransition(map, 1, NodeState.INACTIVE, NodeState.FINISHED,
                parentIsFinished(Condition.TRUE)))
                .addAction(setOutcome(NodeOutcome.SKIPPED));

        nsm.addTransition(makeTransition(map, 1, NodeState.INACTIVE, NodeState.WAITING, 
                parentIsExecuting(Condition.TRUE)));
    }
    
    public void addWaitingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        {
            // All of these are the same, just different guards
            int priority = 0;
            for (NativeExpr g : new NativeExpr[]{ 
                    ancestorExitsDisjoined(Condition.TRUE),
                    exitCondition(Condition.TRUE),
                    ancestorInvariantsConjoined(Condition.FALSE),
                    ancestorEndsDisjoined(Condition.TRUE),
                    skipCondition(Condition.TRUE)
            }) {
                priority++;
                nsm.addTransition(makeTransition(map, priority, 
                        NodeState.WAITING, NodeState.FINISHED, g)
                        .addAction(setOutcome(NodeOutcome.SKIPPED)));
            }
        }
        
        nsm.addTransition(makeTransition(map, 6, NodeState.WAITING, NodeState.ITERATION_ENDED, 
                startCondition(Condition.TRUE),
                preCondition(Condition.NOTTRUE))
                .addAction(setOutcome(
                        NodeOutcome.FAILURE, NodeFailureType.PRE_CONDITION_FAILED)));
        
        nsm.addTransition(makeTransition(map, 6, NodeState.WAITING, NodeState.EXECUTING, 
                startCondition(Condition.TRUE),
                preCondition(Condition.TRUE)));
        
    }

    public void addExecutingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        if (astNode.isEmptyNode()) {
            addEmptyExecutingTransitions(nsm, map);
        } else if (astNode.isAssignmentNode() || astNode.isUpdateNode()) {
            addSimpleExecutingTransitions(nsm, map);
        } else if (astNode.isCommandNode() || astNode.isLibraryNode() || astNode.isListNode()) {
            addComplexExecutingTransitions(nsm, map);
        } else {
            throw new RuntimeException("No transitions found for EXECUTING");
        }
    }
    
    private void addEmptyExecutingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        // Empty nodes have nothing to wait for in FAILING, so they go straight to ITERATION_ENDED.
        nsm.addTransition(checkAncestorExit(map, 1, NodeState.EXECUTING, NodeState.FINISHED));
        nsm.addTransition(checkExit(map, 2, NodeState.EXECUTING, NodeState.ITERATION_ENDED));
        nsm.addTransition(checkAncestorInvariant(map, 3, NodeState.EXECUTING, NodeState.FINISHED));
        nsm.addTransition(checkInvariant(map, 4, NodeState.EXECUTING, NodeState.ITERATION_ENDED));
        
        Pair<Transition, Transition> postChecks = 
            checkPost(endCondition(Condition.TRUE), map, 5, NodeState.EXECUTING, NodeState.ITERATION_ENDED);
        nsm.addTransition(postChecks.first);
        nsm.addTransition(postChecks.second);
    }
    
    private void addSimpleExecutingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        // Simple nodes like Assignment or Update don't have to wait in 
        // FINISHING for anything, so they just do the post condition check 
        // after executing. 
        nsm.addTransition(checkAncestorExit(map, 1, NodeState.EXECUTING, NodeState.FAILING));
        nsm.addTransition(checkExit(map, 2, NodeState.EXECUTING, NodeState.FAILING));
        nsm.addTransition(checkAncestorInvariant(map, 3, NodeState.EXECUTING, NodeState.FAILING));
        nsm.addTransition(checkInvariant(map, 4, NodeState.EXECUTING, NodeState.FAILING));

        Pair<Transition, Transition> postChecks = 
            checkPost(endCondition(Condition.TRUE), map, 5, NodeState.EXECUTING, NodeState.ITERATION_ENDED);
        nsm.addTransition(postChecks.first);
        nsm.addTransition(postChecks.second);
    }
    
    private void addComplexExecutingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        // Complex nodes have things to wait for after executing, so they hang
        // out in FINISHING for a while.
        nsm.addTransition(checkAncestorExit(map, 1, NodeState.EXECUTING, NodeState.FAILING));
        nsm.addTransition(checkExit(map, 2, NodeState.EXECUTING, NodeState.FAILING));
        nsm.addTransition(checkAncestorInvariant(map, 3, NodeState.EXECUTING, NodeState.FAILING));
        nsm.addTransition(checkInvariant(map, 4, NodeState.EXECUTING, NodeState.FAILING));
        
        nsm.addTransition(makeTransition(map, 5, NodeState.EXECUTING, NodeState.FINISHING, 
                endCondition(Condition.TRUE)));
    }
    
    public void addFinishingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        if (astNode.isCommandNode()) {
            addFinishingTransitions(commandHandleKnown(Condition.TRUE), nsm, map);
        } else if (astNode.isListNode()) {
            addFinishingTransitions(allChildrenWaitingOrFinished(Condition.TRUE), nsm, map);
        } else if (astNode.isLibraryNode()) {
            addFinishingTransitions(libraryChildWaitingOrFinished(Condition.TRUE), nsm, map);
        }
    }
    
    public void addFinishingTransitions(NativeExpr finishGuard, NodeStateMachine nsm, Map<NodeState,State> map) {
        nsm.addTransition(checkAncestorExit(map, 1, NodeState.FINISHING, NodeState.FAILING));
        nsm.addTransition(checkExit(map, 2, NodeState.FINISHING, NodeState.FAILING));
        nsm.addTransition(checkAncestorInvariant(map, 3, NodeState.FINISHING, NodeState.FAILING));
        nsm.addTransition(checkInvariant(map, 4, NodeState.FINISHING, NodeState.FAILING));

        Pair<Transition, Transition> postChecks = 
            checkPost(finishGuard, map, 5, NodeState.FINISHING, NodeState.ITERATION_ENDED);
        nsm.addTransition(postChecks.first);
        nsm.addTransition(postChecks.second);
    }
    
    public void addFailingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        Transition parentExited = makeTransition(map, 1, NodeState.FAILING, NodeState.FINISHED, failureTypeIsParentExited(Condition.TRUE));
        Transition parentFailed = makeTransition(map, 2, NodeState.FAILING, NodeState.FINISHED, failureTypeIsParentFailed(Condition.TRUE));
        Transition myFault = makeTransition(map, 3, NodeState.FAILING, NodeState.ITERATION_ENDED);
        
        // There are additional guards that have to be met for some nodes:
        if (astNode.isCommandNode()) {
        	NativeExpr abortComplete = abortComplete(Condition.TRUE);
            parentExited.addGuard(abortComplete);
            parentFailed.addGuard(abortComplete);
            myFault.addGuard(abortComplete);
        } else if (astNode.isUpdateNode()) {
        	NativeExpr updateComplete = PlexilExprToNative.isTrue(translator.getUpdateHandle());
            parentExited.addGuard(updateComplete);
            parentFailed.addGuard(updateComplete);
            myFault.addGuard(updateComplete);
        } else if (astNode.isListNode()) {
        	NativeExpr childrenWaitingOrFinished = allChildrenWaitingOrFinished(Condition.TRUE);
            parentExited.addGuard(childrenWaitingOrFinished);
            parentFailed.addGuard(childrenWaitingOrFinished);
            myFault.addGuard(childrenWaitingOrFinished);
        } else if (astNode.isLibraryNode()) {
        	NativeExpr childWaitingOrFinished = libraryChildWaitingOrFinished(Condition.TRUE);
            parentExited.addGuard(childWaitingOrFinished);
            parentFailed.addGuard(childWaitingOrFinished);
            myFault.addGuard(childWaitingOrFinished);

        }
        
        nsm.addTransition(parentExited);
        nsm.addTransition(parentFailed);
        nsm.addTransition(myFault);
    }
    
    public void addIterationEndedTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        nsm.addTransition(checkAncestorExit(map, 1, NodeState.ITERATION_ENDED, NodeState.FINISHED));
        nsm.addTransition(checkAncestorInvariant(map, 2, NodeState.ITERATION_ENDED, NodeState.FINISHED));
        nsm.addTransition(makeTransition(map, 3, NodeState.ITERATION_ENDED, NodeState.FINISHED, 
                ancestorEndsDisjoined(Condition.TRUE)));
        
        nsm.addTransition(makeTransition(map, 4, NodeState.ITERATION_ENDED, NodeState.FINISHED, 
                repeatCondition(Condition.FALSE)));
        
        nsm.addTransition(makeTransition(map, 4, NodeState.ITERATION_ENDED, NodeState.WAITING, 
                repeatCondition(Condition.TRUE)))
                .addAction(getResetNodeAction());
        
    }
    
    public void addFinishedTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        nsm.addTransition(makeTransition(map, 1, NodeState.FINISHED, NodeState.INACTIVE, 
                parentIsWaiting(Condition.TRUE)))
                .addAction(getResetNodeAction());
    }

    
    /*
     * Support methods for creating the IL state machine ----------------------
     */
    
    private Transition makeTransition(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end,
            NativeExpr...guards) {
        String desc = translator.getUID() +" : "+start+" ("+priority+") -> "+end;
        Transition t = new Transition(desc, priority, map.get(start), map.get(end), guards);
        
        // Timepoints get set for every single transition
        // The starting state is now ending
        t.addAction(new AssignAction(translator.getNodeTimepoint(start, NodeTimepoint.END), 
        		Operation.castToNumeric(new LookupNowExpr("time")), 0));
        // And the destination state is now starting.
        t.addAction(new AssignAction(translator.getNodeTimepoint(end, NodeTimepoint.START), 
        		Operation.castToNumeric(new LookupNowExpr("time")), 0));
        
        return t;
    }
    
    private NativeExpr makeGuard(Description d, Condition cond) {
        if ( ! ilExprCache.containsKey(d)) {
            throw new RuntimeException("Expression was not ready: "+d);
        }
        return new PlexilExprToNative(ilExprCache.get(d), cond);
    }
    
    private CompositeAction getResetNodeAction() {
        CompositeAction reset = new CompositeAction();
        for (String v : translator.getAllVariables()) {
        	ILVariable ilVar = translator.getVariable(v);
        	if (ilVar instanceof SimpleVar) {
        		SimpleVar simple = (SimpleVar) ilVar;
        		reset.getActions().add(
        				new AssignAction(simple, simple.getInitialValue(), 0));
        	} else if (ilVar instanceof ArrayVar) {
        		ArrayVar array = (ArrayVar) ilVar;
        		reset.getActions().add(
        				new AssignAction(array, array.getInitialValue(), 0));
        	} else if (ilVar instanceof LibraryVar) {
        		continue;
        	} else {
        		throw new RuntimeException("Can't reset variables of type "+ilVar.getClass());
        	}
        }
        return reset;
    }
    
    private PlexilAction setOutcome(NodeOutcome outcome) {
        return setOutcome(outcome, NodeFailureType.UNKNOWN);
    }
    
    private PlexilAction setOutcome(NodeOutcome outcome, NodeFailureType failure) {
    	return new CompositeAction(
    			new AssignAction(translator.getOutcome(), outcome, 0),
    			new AssignAction(translator.getFailure(), failure, 0)
    			);
    }
    
    /**
     * Create a transition that sets the outcome to INTERRUPTED, PARENT_EXITED
     * if an ancestor's exit condition is true. 
     * 
     * @param map
     * @param priority
     * @param start
     * @param end
     * @return
     */
    private Transition checkAncestorExit(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end) {
        Transition t = makeTransition(map, priority, start, end, ancestorExitsDisjoined(Condition.TRUE));
        t.addAction(setOutcome(NodeOutcome.INTERRUPTED, NodeFailureType.PARENT_EXITED));
        
        return t;
    }
    
    private Transition checkExit(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end) {
        Transition t = makeTransition(map, priority, start, end, exitCondition(Condition.TRUE));
        t.addAction(setOutcome(NodeOutcome.INTERRUPTED, NodeFailureType.EXITED));
        
        return t;

    }
    
    private Transition checkAncestorInvariant(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end) {
        Transition t = makeTransition(map, priority, start, end, ancestorInvariantsConjoined(Condition.FALSE));
        t.addAction(setOutcome(NodeOutcome.FAILURE, NodeFailureType.PARENT_FAILED));
        
        return t;
    }
    
    private Transition checkInvariant(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end) {
        Transition t = makeTransition(map, priority, start, end, invariantCondition(Condition.FALSE));
        t.addAction(setOutcome(NodeOutcome.FAILURE, NodeFailureType.INVARIANT_CONDITION_FAILED));
        
        return t;
    }
    
    /**
     * Returns two transitions that check the post condition. One sets the 
     * outcome as SUCCESS, the other as FAILURE, POST_CONDITION_FAILED, based
     * on whether the post condition is true or not, respectively. 
     * 
     * @param guard an additional guard on the transition (for example, EndCondition True)
     * @param map the mapping of Plexil states to IL states
     * @param priority 
     * @param start
     * @param end
     * @return both post-condition check transitions.
     */
    private Pair<Transition,Transition> checkPost(NativeExpr guard, 
            Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end) {
        Transition success = makeTransition(map, priority, start, end, guard, postCondition(Condition.TRUE));
        success.addAction(setOutcome(NodeOutcome.SUCCESS));
        Transition fail = makeTransition(map, priority, start, end, guard, postCondition(Condition.NOTTRUE));
        fail.addAction(setOutcome(NodeOutcome.FAILURE, NodeFailureType.POST_CONDITION_FAILED));
        return new Pair<Transition,Transition>(success, fail);
    }
    
    /*
     * All the various guards that are used in the diagrams ------------------
     */
    
    private NativeExpr startCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.START_CONDITION)) {
            ilExprCache.put(Description.START_CONDITION, 
                    translator.toIL(astNode.getStartCondition()));
        }
        return makeGuard(Description.START_CONDITION, cond);
    }
    
    private NativeExpr skipCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.SKIP_CONDITION)) {
            ilExprCache.put(Description.SKIP_CONDITION, 
                    translator.toIL(astNode.getSkipCondition()));
        }
        return makeGuard(Description.SKIP_CONDITION, cond);
    }
    
    private NativeExpr preCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.PRE_CONDITION)) {
            ilExprCache.put(Description.PRE_CONDITION, 
                    translator.toIL(astNode.getPreCondition()));
        }
        return makeGuard(Description.PRE_CONDITION, cond);
    }
    
    private NativeExpr invariantCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.INVARIANT_CONDITION)) {
            ilExprCache.put(Description.INVARIANT_CONDITION, 
                    translator.toIL(astNode.getInvariantCondition()));
        }
        return makeGuard(Description.INVARIANT_CONDITION, cond);
    }
    
    private NativeExpr repeatCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.REPEAT_CONDITION)) {
            ilExprCache.put(Description.REPEAT_CONDITION, 
                    translator.toIL(astNode.getRepeatCondition()));
        }
        return makeGuard(Description.REPEAT_CONDITION, cond);
    }
    
    private NativeExpr postCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.POST_CONDITION)) {
            ilExprCache.put(Description.POST_CONDITION, 
                    translator.toIL(astNode.getPostCondition()));
        }
        return makeGuard(Description.POST_CONDITION, cond);
    }
    
    private NativeExpr endCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.END_CONDITION)) {
        	// Hold up! From the documentation:
        	// The actual End Condition of Command and Update nodes is the 
        	// conjunction of the explicitly specified expression and the default 
        	// condition.
        	
        	// But actually, this isn't the whole truth. For commands:
        	// Note that the supplied EndCondition is ORed with 
        	// (command_handle == COMMAND_DENIED || command_handle == COMMAND_FAILED) . 
        	// This allows the node to transition in the event the resource 
        	// arbiter rejects the command.
        	
        	// In the source code, it seems that the latter is correct. The
        	// end condition isn't conjoined with anything, but it is disjoined
        	// with that expression. (CommandNode.cc)
        	
        	// It also looks like the default end condition of commands is
        	// actually just "true". The semantics wiki page apparently lies 
        	// about that. 
        	
        	ILExpression endCondition;
        	
        	if (astNode.isUpdateNode()) {
        		endCondition = translator.toIL(
        				Operation.and(DefaultEndExpr.get(), astNode.getEndCondition()));
        	} else if (astNode.isCommandNode()) {
        		endCondition = 
        				Operation.or(translator.toIL(astNode.getEndCondition()), 
        							Operation.eq(translator.getCommandHandle(), CommandHandleState.COMMAND_DENIED),
        							Operation.eq(translator.getCommandHandle(), CommandHandleState.COMMAND_FAILED)
        						);
        	} else {
        		// No wrapper necessary
        		endCondition = translator.toIL(astNode.getEndCondition());
        	}
        	
            ilExprCache.put(Description.END_CONDITION, endCondition);
        }
        return makeGuard(Description.END_CONDITION, cond);
    }
    
    private NativeExpr exitCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.EXIT_CONDITION)) {
            ilExprCache.put(Description.EXIT_CONDITION, 
                    translator.toIL(astNode.getExitCondition()));
        }
        return makeGuard(Description.EXIT_CONDITION, cond);
    }
    
    private NativeExpr ancestorEndsDisjoined(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.ANCESTOR_ENDS_DISJOINED)) {
        	ilExprCache.put(Description.ANCESTOR_ENDS_DISJOINED, 
                	translator.getParent().map(NodeToIL::getThisOrAncestorsEnds)
            		.orElse(new RootAncestorEndExpr()));
        }
        return makeGuard(Description.ANCESTOR_ENDS_DISJOINED, cond);
    }
    
    private NativeExpr ancestorExitsDisjoined(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.ANCESTOR_EXITS_DISJOINED)) {
        	ilExprCache.put(Description.ANCESTOR_EXITS_DISJOINED, 
        			translator.getParent().map(NodeToIL::getThisOrAncestorsExits)
        			.orElse(new RootAncestorExitExpr()));
        }
        return makeGuard(Description.ANCESTOR_EXITS_DISJOINED, cond);
    }
    
    private NativeExpr ancestorInvariantsConjoined(Condition cond) {
    	if ( ! ilExprCache.containsKey(Description.ANCESTOR_INVARIANTS_CONJOINED)) {
    		ilExprCache.put(Description.ANCESTOR_INVARIANTS_CONJOINED, 
    				translator.getParent().map(NodeToIL::getThisAndAncestorsInvariants)
    				.orElse(new RootAncestorInvariantExpr()));
    	}
    	return makeGuard(Description.ANCESTOR_INVARIANTS_CONJOINED, cond);
    }

    private NativeExpr parentIsFinished(Condition cond) {
        return getParentIsInState(NodeState.FINISHED, Description.PARENT_FINISHED, cond);
    }

    private NativeExpr parentIsExecuting(Condition cond) {
        return getParentIsInState(NodeState.EXECUTING, Description.PARENT_EXECUTING, cond);
    }

    private NativeExpr parentIsWaiting(Condition cond) {
        return getParentIsInState(NodeState.WAITING, Description.PARENT_WAITING, cond);
    }

    private NativeExpr getParentIsInState(NodeState state, Description d, Condition cond) {
        if ( ! ilExprCache.containsKey(d) ) {
        	ilExprCache.put(d, Operation.eq(state, 
        			translator.getParent().map((parent) -> (ILExpression)parent.getState())
        			.orElse(new RootParentStateExpr())
        			));
        }
        return makeGuard(d, cond);
    }

    private NativeExpr failureTypeIsParentFailed(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.FAILURE_IS_PARENT_FAIL)) {
            ilExprCache.put(Description.FAILURE_IS_PARENT_FAIL,
                    Operation.eq(
                    		translator.getFailure(), 
                            NodeFailureType.PARENT_FAILED));
        }
        
        return makeGuard(Description.FAILURE_IS_PARENT_FAIL, cond);
    }

    private NativeExpr failureTypeIsParentExited(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.FAILURE_IS_PARENT_EXIT)) {
            ilExprCache.put(Description.FAILURE_IS_PARENT_EXIT,
                    Operation.eq(
                    		translator.getFailure(), 
                            NodeFailureType.PARENT_EXITED));
        }
        
        return makeGuard(Description.FAILURE_IS_PARENT_EXIT, cond);
    }
    
    
    private NativeExpr commandHandleKnown(Condition cond) {
        return new PlexilExprToNative(Operation.isKnown(translator.getCommandHandle()), cond);
    }

    private NativeExpr allChildrenWaitingOrFinished(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.ALL_CHILDREN_WAITING_OR_FINISHED)) {
            List<Expression> clauses = new ArrayList<Expression>();
            for (NodeToIL child : translator.getChildren()) {
                clauses.add(
                        Operation.or(
                                Operation.eq(child.getState(), NodeState.WAITING),
                                Operation.eq(child.getState(), NodeState.FINISHED)
                            )
                        );
            }
            ilExprCache.put(Description.ALL_CHILDREN_WAITING_OR_FINISHED, Operation.and(clauses));
        }
        return makeGuard(Description.ALL_CHILDREN_WAITING_OR_FINISHED, cond);
    }
    
    private NativeExpr libraryChildWaitingOrFinished(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.ALL_CHILDREN_WAITING_OR_FINISHED)) {
        	if (translator.hasLibraryHandle()) {
	            ilExprCache.put(Description.ALL_CHILDREN_WAITING_OR_FINISHED, 
	                    Operation.or(
	                            Operation.eq(translator.getLibraryHandle(), NodeState.WAITING),
	                            Operation.eq(translator.getLibraryHandle(), NodeState.FINISHED)
	                    ));
        	} else {
        		// A library node won't actually have a handle if the library
        		// got statically included. In that case, it should be treated
        		// as a list node. 
        		return allChildrenWaitingOrFinished(cond);
        	}
        }
        return makeGuard(Description.ALL_CHILDREN_WAITING_OR_FINISHED, cond);
        
    }
    
    private NativeExpr abortComplete(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.COMMAND_ABORT_COMPLETE)) {
            ilExprCache.put(Description.COMMAND_ABORT_COMPLETE, 
                    Operation.eq(translator.getCommandHandle(), CommandHandleState.COMMAND_ABORTED));
        }
        return makeGuard(Description.COMMAND_ABORT_COMPLETE, cond);
    }
}
