package edu.umn.crisys.plexil.ast2il;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umn.crisys.plexil.ast.Node;
import edu.umn.crisys.plexil.ast.expr.ASTLookupExpr;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.PlexilExprDescription;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CompositeAction;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILOperator;
import edu.umn.crisys.plexil.il.expr.NamedCondition;
import edu.umn.crisys.plexil.il.expr.RootAncestorExpr;
import edu.umn.crisys.plexil.il.expr.vars.ArrayVar;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.expr.vars.LibraryVar;
import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.NodeTimepoint;
import edu.umn.crisys.util.Pair;

public class StateMachineBuilder {
	
    public static String TIME = "time";

	private NodeToIL translator;
	private Node astNode;
    private Map<PlexilExprDescription, NamedCondition> ilExprCache = new HashMap<>();
	private Map<PlexilExprDescription, ILExpr> nativeExprCache = new HashMap<>();
	    
	
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
                parentIsFinished()))
                .addAction(setOutcome(NodeOutcome.SKIPPED));

        nsm.addTransition(makeTransition(map, 1, NodeState.INACTIVE, NodeState.WAITING, 
                parentIsExecuting()));
    }
    
    public void addWaitingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        {
            // All of these are the same, just different guards
            int priority = 0;
            for (ILExpr g : new ILExpr[]{ 
                    ancestorExitsDisjoined(ILOperator.IS_TRUE),
                    exitCondition(ILOperator.IS_TRUE),
                    ancestorInvariantsConjoined(ILOperator.IS_FALSE),
                    ancestorEndsDisjoined(ILOperator.IS_TRUE),
                    skipCondition(ILOperator.IS_TRUE)
            }) {
                priority++;
                nsm.addTransition(makeTransition(map, priority, 
                        NodeState.WAITING, NodeState.FINISHED, g)
                        .addAction(setOutcome(NodeOutcome.SKIPPED)));
            }
        }
        
        nsm.addTransition(makeTransition(map, 6, NodeState.WAITING, NodeState.ITERATION_ENDED, 
                startCondition(ILOperator.IS_TRUE),
                preCondition(ILOperator.IS_NOT_TRUE))
                .addAction(setOutcome(
                        NodeOutcome.FAILURE, NodeFailureType.PRE_CONDITION_FAILED)));
        
        nsm.addTransition(makeTransition(map, 6, NodeState.WAITING, NodeState.EXECUTING, 
                startCondition(ILOperator.IS_TRUE),
                preCondition(ILOperator.IS_TRUE)));
        
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
            checkPost(endCondition(ILOperator.IS_TRUE), map, 5, NodeState.EXECUTING, NodeState.ITERATION_ENDED);
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
            checkPost(endCondition(ILOperator.IS_TRUE), map, 5, NodeState.EXECUTING, NodeState.ITERATION_ENDED);
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
                endCondition(ILOperator.IS_TRUE)));
    }
    
    public void addFinishingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        if (astNode.isCommandNode()) {
            addFinishingTransitions(commandHandleIsKnown(), nsm, map);
        } else if (astNode.isListNode() || astNode.isLibraryNode()) {
            addFinishingTransitions(allChildrenWaitingOrFinished(), nsm, map);
        } 
    }
    
    public void addFinishingTransitions(ILExpr finishGuard, NodeStateMachine nsm, Map<NodeState,State> map) {
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
        Transition parentExited = makeTransition(map, 1, NodeState.FAILING, NodeState.FINISHED, failureTypeIsParentExited());
        Transition parentFailed = makeTransition(map, 2, NodeState.FAILING, NodeState.FINISHED, failureTypeIsParentFailed());
        Transition myFault = makeTransition(map, 3, NodeState.FAILING, NodeState.ITERATION_ENDED);
        
        // There are additional guards that have to be met for some nodes:
        if (astNode.isCommandNode()) {
        	ILExpr abortComplete = abortComplete();
            parentExited.addGuard(abortComplete);
            parentFailed.addGuard(abortComplete);
            myFault.addGuard(abortComplete);
        } else if (astNode.isUpdateNode()) {
        	ILExpr updateComplete = ILOperator.IS_TRUE.expr(translator.getUpdateHandle());
            parentExited.addGuard(updateComplete);
            parentFailed.addGuard(updateComplete);
            myFault.addGuard(updateComplete);
        } else if (astNode.isListNode() || astNode.isLibraryNode()) {
        	ILExpr childrenWaitingOrFinished = allChildrenWaitingOrFinished();
            parentExited.addGuard(childrenWaitingOrFinished);
            parentFailed.addGuard(childrenWaitingOrFinished);
            myFault.addGuard(childrenWaitingOrFinished);
        } 
        
        nsm.addTransition(parentExited);
        nsm.addTransition(parentFailed);
        nsm.addTransition(myFault);
    }
    
    public void addIterationEndedTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        nsm.addTransition(checkAncestorExit(map, 1, NodeState.ITERATION_ENDED, NodeState.FINISHED));
        nsm.addTransition(checkAncestorInvariant(map, 2, NodeState.ITERATION_ENDED, NodeState.FINISHED));
        nsm.addTransition(makeTransition(map, 3, NodeState.ITERATION_ENDED, NodeState.FINISHED, 
                ancestorEndsDisjoined(ILOperator.IS_TRUE)));
        
        nsm.addTransition(makeTransition(map, 4, NodeState.ITERATION_ENDED, NodeState.FINISHED, 
                repeatCondition(ILOperator.IS_FALSE)));
        
        nsm.addTransition(makeTransition(map, 4, NodeState.ITERATION_ENDED, NodeState.WAITING, 
                repeatCondition(ILOperator.IS_TRUE)))
                .addAction(getResetNodeAction());
        
    }
    
    public void addFinishedTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        nsm.addTransition(makeTransition(map, 1, NodeState.FINISHED, NodeState.INACTIVE, 
                parentIsWaiting()))
                .addAction(getResetNodeAction());
    }

    
    /*
     * Support methods for creating the IL state machine ----------------------
     */
    
    private ILExpr lookupTime() {
    	return translator.toIL(new ASTLookupExpr(NodeToIL.TIMEPOINT_TYPE, TIME), 
    			NodeToIL.TIMEPOINT_TYPE);
    }
    
    private Transition makeTransition(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end,
            ILExpr...guards) {
        String desc = translator.getUID() +" : "+start+" ("+priority+") -> "+end;
        Transition t = new Transition(desc, priority, map.get(start), map.get(end), guards);
        
        // Timepoints get set for every single transition
        // The starting state is now ending
        t.addAction(new AssignAction(translator.getNodeTimepoint(start, NodeTimepoint.END), 
        		lookupTime(), 0));
        // And the destination state is now starting.
        t.addAction(new AssignAction(translator.getNodeTimepoint(end, NodeTimepoint.START), 
        		lookupTime(), 0));
        
        return t;
    }
    
    private ILExpr makeGuard(PlexilExprDescription d, ILOperator cond) {
        if ( ! ilExprCache.containsKey(d)) {
            throw new RuntimeException("Expression was not ready: "+d);
        }
        return cond.expr(ilExprCache.get(d));
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
        Transition t = makeTransition(map, priority, start, end, ancestorExitsDisjoined(ILOperator.IS_TRUE));
        t.addAction(setOutcome(NodeOutcome.INTERRUPTED, NodeFailureType.PARENT_EXITED));
        
        return t;
    }
    
    private Transition checkExit(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end) {
        Transition t = makeTransition(map, priority, start, end, exitCondition(ILOperator.IS_TRUE));
        t.addAction(setOutcome(NodeOutcome.INTERRUPTED, NodeFailureType.EXITED));
        
        return t;

    }
    
    private Transition checkAncestorInvariant(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end) {
        Transition t = makeTransition(map, priority, start, end, ancestorInvariantsConjoined(ILOperator.IS_FALSE));
        t.addAction(setOutcome(NodeOutcome.FAILURE, NodeFailureType.PARENT_FAILED));
        
        return t;
    }
    
    private Transition checkInvariant(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end) {
        Transition t = makeTransition(map, priority, start, end, invariantCondition(ILOperator.IS_FALSE));
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
    private Pair<Transition,Transition> checkPost(ILExpr guard, 
            Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end) {
        Transition success = makeTransition(map, priority, start, end, guard, postCondition(ILOperator.IS_TRUE));
        success.addAction(setOutcome(NodeOutcome.SUCCESS));
        Transition fail = makeTransition(map, priority, start, end, guard, postCondition(ILOperator.IS_NOT_TRUE));
        fail.addAction(setOutcome(NodeOutcome.FAILURE, NodeFailureType.POST_CONDITION_FAILED));
        return new Pair<Transition,Transition>(success, fail);
    }
    
    /*
     * All the various guards that are used in the diagrams ------------------
     */
    
    private void cacheNamedExpression(PlexilExprDescription desc, ILExpr ilExpr) {
    	ilExprCache.put(desc, new NamedCondition(ilExpr, translator.getUID(), desc));
    }
    
    private ILExpr startCondition(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.START_CONDITION)) {
            cacheNamedExpression(PlexilExprDescription.START_CONDITION, 
                    translator.toIL(astNode.getStartCondition(), PlexilType.BOOLEAN));
        }
        return makeGuard(PlexilExprDescription.START_CONDITION, cond);
    }
    
    private ILExpr skipCondition(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.SKIP_CONDITION)) {
            cacheNamedExpression(PlexilExprDescription.SKIP_CONDITION, 
                    translator.toIL(astNode.getSkipCondition(), PlexilType.BOOLEAN));
        }
        return makeGuard(PlexilExprDescription.SKIP_CONDITION, cond);
    }
    
    private ILExpr preCondition(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.PRE_CONDITION)) {
            cacheNamedExpression(PlexilExprDescription.PRE_CONDITION, 
                    translator.toIL(astNode.getPreCondition(), PlexilType.BOOLEAN));
        }
        return makeGuard(PlexilExprDescription.PRE_CONDITION, cond);
    }
    
    private ILExpr invariantCondition(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.INVARIANT_CONDITION)) {
            cacheNamedExpression(PlexilExprDescription.INVARIANT_CONDITION, 
                    translator.toIL(astNode.getInvariantCondition(), PlexilType.BOOLEAN));
        }
        return makeGuard(PlexilExprDescription.INVARIANT_CONDITION, cond);
    }
    
    private ILExpr repeatCondition(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.REPEAT_CONDITION)) {
            cacheNamedExpression(PlexilExprDescription.REPEAT_CONDITION, 
                    translator.toIL(astNode.getRepeatCondition(), PlexilType.BOOLEAN));
        }
        return makeGuard(PlexilExprDescription.REPEAT_CONDITION, cond);
    }
    
    private ILExpr postCondition(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.POST_CONDITION)) {
            cacheNamedExpression(PlexilExprDescription.POST_CONDITION, 
                    translator.toIL(astNode.getPostCondition(), PlexilType.BOOLEAN));
        }
        return makeGuard(PlexilExprDescription.POST_CONDITION, cond);
    }
    
    private ILExpr endCondition(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.END_CONDITION)) {
            cacheNamedExpression(PlexilExprDescription.END_CONDITION, 
            		translator.getCalculatedILEndCondition());
        }
        return makeGuard(PlexilExprDescription.END_CONDITION, cond);
    }
    
    private ILExpr exitCondition(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.EXIT_CONDITION)) {
            cacheNamedExpression(PlexilExprDescription.EXIT_CONDITION, 
                    translator.toIL(astNode.getExitCondition(), PlexilType.BOOLEAN));
        }
        return makeGuard(PlexilExprDescription.EXIT_CONDITION, cond);
    }
    
    private ILExpr ancestorEndsDisjoined(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.ANCESTOR_ENDS_DISJOINED)) {
        	cacheNamedExpression(PlexilExprDescription.ANCESTOR_ENDS_DISJOINED, 
                	translator.getParent().map(NodeToIL::getThisOrAncestorsEnds)
            		.orElse(RootAncestorExpr.END));
        }
        return makeGuard(PlexilExprDescription.ANCESTOR_ENDS_DISJOINED, cond);
    }
    
    private ILExpr ancestorExitsDisjoined(ILOperator cond) {
        if ( ! ilExprCache.containsKey(PlexilExprDescription.ANCESTOR_EXITS_DISJOINED)) {
        	cacheNamedExpression(PlexilExprDescription.ANCESTOR_EXITS_DISJOINED, 
        			translator.getParent().map(NodeToIL::getThisOrAncestorsExits)
        			.orElse(RootAncestorExpr.EXIT));
        }
        return makeGuard(PlexilExprDescription.ANCESTOR_EXITS_DISJOINED, cond);
    }
    
    private ILExpr ancestorInvariantsConjoined(ILOperator cond) {
    	if ( ! ilExprCache.containsKey(PlexilExprDescription.ANCESTOR_INVARIANTS_CONJOINED)) {
    		cacheNamedExpression(PlexilExprDescription.ANCESTOR_INVARIANTS_CONJOINED, 
    				translator.getParent().map(NodeToIL::getThisAndAncestorsInvariants)
    				.orElse(RootAncestorExpr.INVARIANT));
    	}
    	return makeGuard(PlexilExprDescription.ANCESTOR_INVARIANTS_CONJOINED, cond);
    }

    private ILExpr parentIsFinished() {
        return getParentIsInState(NodeState.FINISHED, PlexilExprDescription.PARENT_FINISHED);
    }

    private ILExpr parentIsExecuting() {
        return getParentIsInState(NodeState.EXECUTING, PlexilExprDescription.PARENT_EXECUTING);
    }

    private ILExpr parentIsWaiting() {
        return getParentIsInState(NodeState.WAITING, PlexilExprDescription.PARENT_WAITING);
    }

    private ILExpr getParentIsInState(NodeState state, PlexilExprDescription d) {
        if ( ! nativeExprCache.containsKey(d) ) {
        	nativeExprCache.put(d, 
        			ILOperator.DIRECT_COMPARE.expr(state, 
        					translator.getParent().map(
        							(parent) -> (ILExpr)parent.getState())
        					.orElse(RootAncestorExpr.STATE)
        			));
        }
        return nativeExprCache.get(d);
    }

    private ILExpr failureTypeIsParentFailed() {
        if ( ! nativeExprCache.containsKey(PlexilExprDescription.FAILURE_IS_PARENT_FAIL)) {
        	nativeExprCache.put(
        			PlexilExprDescription.FAILURE_IS_PARENT_FAIL,
        			ILOperator.DIRECT_COMPARE.expr(
                    		translator.getFailure(), 
                            NodeFailureType.PARENT_FAILED));
        }
        
        return nativeExprCache.get(PlexilExprDescription.FAILURE_IS_PARENT_FAIL);
    }

    private ILExpr failureTypeIsParentExited() {
        if ( ! nativeExprCache.containsKey(PlexilExprDescription.FAILURE_IS_PARENT_EXIT)) {
        	nativeExprCache.put(PlexilExprDescription.FAILURE_IS_PARENT_EXIT,
        			ILOperator.DIRECT_COMPARE.expr(
                    		translator.getFailure(), 
                            NodeFailureType.PARENT_EXITED));
        }
        
        return nativeExprCache.get(PlexilExprDescription.FAILURE_IS_PARENT_EXIT);
    }
    
    
    private ILExpr commandHandleIsKnown() {
    	return ILOperator.ISKNOWN_OPERATOR.expr(translator.getCommandHandle());
    }

    private ILExpr allChildrenWaitingOrFinished() {
    	if ( ! nativeExprCache.containsKey(PlexilExprDescription.ALL_CHILDREN_WAITING_OR_FINISHED)) {
    		if (translator.hasLibraryHandle()) {
    			// Treat it as the child
    			nativeExprCache.put(PlexilExprDescription.ALL_CHILDREN_WAITING_OR_FINISHED, 
    					ILOperator.OR.expr(
    							ILOperator.DIRECT_COMPARE.expr(
    									translator.getLibraryHandle(), 
    									NodeState.WAITING),
    							ILOperator.DIRECT_COMPARE.expr(
    									translator.getLibraryHandle(), 
    									NodeState.FINISHED)
    							)        				
    					);
    		} else {
    			// Just a regular list node with children. 
    			List<ILExpr> clauses = new ArrayList<ILExpr>();
    			for (NodeToIL child : translator.getChildren()) {
    				clauses.add(
    						ILOperator.OR.expr(
    								ILOperator.DIRECT_COMPARE.expr(child.getState(), NodeState.WAITING),
    								ILOperator.DIRECT_COMPARE.expr(child.getState(), NodeState.FINISHED)
    								)
    						);
    			}
    			ILExpr all = clauses.size() == 1 ? clauses.get(0) : ILOperator.AND.expr(clauses);
    			nativeExprCache.put(PlexilExprDescription.ALL_CHILDREN_WAITING_OR_FINISHED,
    					all);
    		}
    	}
    	return nativeExprCache.get(PlexilExprDescription.ALL_CHILDREN_WAITING_OR_FINISHED);
    }
    
    private ILExpr abortComplete() {
    	return translator.getCommandAbortAck();
    }
}
