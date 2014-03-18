package edu.umn.crisys.plexil.ast2il;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.ast.core.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.core.node.AssignmentBody;
import edu.umn.crisys.plexil.ast.core.node.CommandBody;
import edu.umn.crisys.plexil.ast.core.node.LibraryBody;
import edu.umn.crisys.plexil.ast.core.node.Node;
import edu.umn.crisys.plexil.ast.core.node.NodeBody;
import edu.umn.crisys.plexil.ast.core.node.NodeListBody;
import edu.umn.crisys.plexil.ast.core.node.UpdateBody;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AlsoRunNodesAction;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CaptureCurrentValueAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.ResetNodeAction;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.SetOutcomeAction;
import edu.umn.crisys.plexil.il.action.SetTimepointAction;
import edu.umn.crisys.plexil.il.action.SetVarToPreviousValueAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard.Condition;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard.Description;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.NodeTimepoint;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.translator.il.vars.AliasedVariableReference;
import edu.umn.crisys.plexil.translator.il.vars.ArrayElementReference;
import edu.umn.crisys.plexil.translator.il.vars.ArrayReference;
import edu.umn.crisys.plexil.translator.il.vars.CommandHandleReference;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;
import edu.umn.crisys.plexil.translator.il.vars.LibraryNodeReference;
import edu.umn.crisys.plexil.translator.il.vars.NodeStateReference;
import edu.umn.crisys.plexil.translator.il.vars.NodeTimepointReference;
import edu.umn.crisys.plexil.translator.il.vars.PreviousValueReference;
import edu.umn.crisys.plexil.translator.il.vars.UpdateHandleReference;
import edu.umn.crisys.plexil.translator.il.vars.VariableReference;
import edu.umn.crisys.util.Pair;

/**
 * An object that takes an AST Node, and transforms it into IL. It knows a lot
 * about Plexil semantics. Translation takes place in 3 basic ways:
 * 
 * <ul>
 * <li> Creation of IL variables. This takes place in the constructor, so that
 * they're available later.
 * <li> Transforming AST Expressions into IL expressions. This happens on the
 * fly as IL Expressions are needed. This mostly involves finding variables
 * and replacing, say, "foo" with the actual IL variable. 
 * <li> Lastly, creation of the state machine. It's broken down into about
 * as many parts as I could think of, because it gets complicated. 
 * 
 * @author jbiatek
 *
 */
public class NodeToIL {
    
    private static final String STATE = ".state";
    private static final String OUTCOME = ".outcome";
    private static final String FAILURE = ".failure";
    private static final String COMMAND_HANDLE = ".command_handle";
    private static final String LIBRARY_HANDLE = ".library_handle";
    private static final String UPDATE_HANDLE = ".update_handle";
    private static final String PREVIOUS_VALUE = ".previous_value";
    
    private Node myNode;
    private NodeUID myUid;
    private NodeToIL parent;
    private ASTExprToILExpr exprToIL = new ASTExprToILExpr(this);
    
    private List<NodeToIL> children = new ArrayList<NodeToIL>();
    
    private Map<String, IntermediateVariable> ilVars = 
        new HashMap<String, IntermediateVariable>();
    
    private Map<Description, ILExpression> ilExprCache = 
        new HashMap<TransitionGuard.Description, ILExpression>();
    
    public NodeToIL(Node node) {
        this(node, null);
    }
    
    public NodeToIL(Node node, NodeToIL parent) {
        this.myNode = node;
        this.parent = parent;
        if (parent == null) {
            myUid = new NodeUID(node.getPlexilID());
        } else {
            myUid = new NodeUID(parent.myUid, node.getPlexilID());
        }
        // We should create the IL variables now.
        createILVars();
        checkForChildren();
        
        if (parent == null) {
            // Okay, now we can do this.
            parentsAreReady();
        }
    }
    
    private void createILVars() {
        // Internal vars for all nodes
        ilVars.put(STATE, new NodeStateReference(myUid));
        ilVars.put(OUTCOME, new VariableReference(myUid, PlexilType.OUTCOME, "UNKNOWN"));
        ilVars.put(FAILURE, new VariableReference(myUid, PlexilType.FAILURE, "UNKNOWN"));
        // Node timepoints
        for (NodeState state : NodeState.values()) {
            for (NodeTimepoint tpt : NodeTimepoint.values()) {
                ilVars.put("."+state+"."+tpt, new NodeTimepointReference(myUid, state, tpt));
            }
        }
        
        // Variables defined by the programmer in the node
        for (String varName : myNode.getVarNames()) {
            PlexilType type = myNode.getVarType(varName);
            if (type.isArrayType()) {
                // Array variables.
                ArrayLiteralExpr init = myNode.getInitArray(varName);
                int arraySize = myNode.getArraySize(varName);
                
                // Get the initial value, if any. If not, just initialize to an
                // empty array. 
                if (init == null) {
                    init = new ArrayLiteralExpr(type);
                }
                
                ilVars.put(varName, new ArrayReference(myUid, varName, type, arraySize, init));
            } else {
                // Standard variables.
                PValueExpression init = myNode.getInitVariable(varName);
                ilVars.put(varName, new VariableReference(myUid, varName, type, init));
            }
        }
    }
    
    private void checkForChildren() {
        if (isListNode()) {
            NodeListBody body = (NodeListBody) myNode.getNodeBody();
            for (Node child : body) {
                children.add(new NodeToIL(child, this));
            }
        }
    }
    
    private void parentsAreReady() {
        // This is called after construction when our parent nodes have finished
        // constructing themselves. We should do what we need to do, then pass
        // the message along to our children.
        
        // Special variables based on the type of node this is.
        NodeBody body = myNode.getNodeBody();
        if (isCommandNode()) {
            CommandBody cmd = (CommandBody) body;
            CommandHandleReference handle;
            int priority = myNode.getPriority();
            if (cmd.getVarToAssign() == null) {
                handle = new CommandHandleReference(myUid, priority);
            } else {
                IntermediateVariable returnTo = resolveVariableforWriting(cmd.getVarToAssign());
                handle = new CommandHandleReference(myUid, priority, returnTo);
            }
            ilVars.put(COMMAND_HANDLE, handle);
        } else if (isUpdateNode()) {
            ilVars.put(UPDATE_HANDLE, new UpdateHandleReference(myUid));
        } else if (isLibraryNode()) {
            LibraryBody lib = (LibraryBody) getNodeBody();
            Map<String,ILExpression> aliases = new HashMap<String, ILExpression>();
            
            for (String alias : lib.getAliases()) {
                aliases.put(alias, toIL(lib.getAlias(alias)));
            }
            
            LibraryNodeReference libRef = new LibraryNodeReference(
                    getUID(), lib.getNodeId(), getState(), aliases);
            ilVars.put(LIBRARY_HANDLE, libRef);
            
            // We have to set these after making the library reference, because
            // it's very likely that they *include* the library reference.
            libRef.setLibAndAncestorsInvariants(getThisAndAncestorsInvariants());
            libRef.setLibOrAncestorsEnds(getThisOrAncestorsEnds());
            libRef.setLibOrAncestorsExits(getThisOrAncestorsExits());
        } else if (isAssignmentNode()) {
            ilVars.put(PREVIOUS_VALUE, new PreviousValueReference(getUID()));
        }
        
        for (NodeToIL child : children) {
            child.parentsAreReady();
        }
    }
    
    private ILExpression toIL(Expression e) {
        return e.accept(exprToIL, null);
    }
    
    private List<ILExpression> toIL(List<? extends Expression> list) {
        List<ILExpression> ret = new ArrayList<ILExpression>();
        for (Expression e : list) {
            ret.add(toIL(e));
        }
        return ret;
    }
    
    public NodeToIL getParent() {
    	return parent;
    }
    
    public NodeStateReference getState() {
        return (NodeStateReference) ilVars.get(STATE);
    }
    
    public VariableReference getOutcome() {
        return (VariableReference) ilVars.get(OUTCOME);
    }
    
    public VariableReference getFailure() {
        return (VariableReference) ilVars.get(FAILURE);
    }
    
    public boolean hasCommandHandle() {
        return ilVars.containsKey(COMMAND_HANDLE);
    }
    
    public CommandHandleReference getCommandHandle() {
        if (! hasCommandHandle()) {
            throw new RuntimeException("Not a command node: "+myNode.getNodeBody());
        }
        return (CommandHandleReference) ilVars.get(COMMAND_HANDLE);
    }
    
    public boolean hasLibraryHandle() {
        return ilVars.containsKey(LIBRARY_HANDLE);
    }

    public LibraryNodeReference getLibraryHandle() {
        if (! hasLibraryHandle()) {
            throw new RuntimeException("Not a library node: "+myNode.getNodeBody());
        }
        return (LibraryNodeReference) ilVars.get(LIBRARY_HANDLE);
    }
    
    private boolean hasUpdateHandle() {
        return ilVars.containsKey(UPDATE_HANDLE);
    }
    
    public UpdateHandleReference getUpdateHandle() {
        if (! hasUpdateHandle()) {
            throw new RuntimeException("Not an update node: "+myNode.getNodeBody());
        }
        return (UpdateHandleReference) ilVars.get(UPDATE_HANDLE);
    }
    
    private PreviousValueReference getPreviousValue() {
        if ( ! ilVars.containsKey(PREVIOUS_VALUE)) {
            throw new RuntimeException("Doesn't contain previous value: "+this);
        }
        
        return (PreviousValueReference) ilVars.get(PREVIOUS_VALUE);
    }

    public NodeTimepointReference getNodeTimepoint(NodeState state, NodeTimepoint time) {
        IntermediateVariable tpt = ilVars.get("."+state+"."+time);
        if (tpt == null || ! (tpt instanceof NodeTimepointReference)) {
            throw new RuntimeException("Went looking for "+time+" of "+state+", got "+tpt);
        }
        return (NodeTimepointReference) tpt;
    }
    
    private ILExpression getThisOrAncestorsExits() {
        ILExpression myExit = toIL(myNode.getExitCondition());
        if (parent != null) {
            return Operation.or(myExit, parent.getThisOrAncestorsExits());
        } else {
            return Operation.or(myExit, new RootAncestorExitExpr());
        }
    }

    private ILExpression getThisOrAncestorsEnds() {
        ILExpression myEnd = toIL(myNode.getEndCondition());
        if (parent != null) {
            return Operation.or(myEnd, parent.getThisOrAncestorsEnds());
        } else {
            return Operation.or(myEnd, new RootAncestorEndExpr());
        }
    }

    private ILExpression getThisAndAncestorsInvariants() {
        ILExpression myInv = toIL(myNode.getInvariantCondition());
        if (parent != null) {
            return Operation.and(myInv, parent.getThisAndAncestorsInvariants());
        } else {
            return Operation.and(myInv, new RootAncestorInvariantExpr());
        }
    }

    public NodeUID getUID() {
        return myUid;
    }
    
    public List<NodeToIL> getChildren() {
        return children;
    }
    
    public Set<String> getAllVariables() {
        return ilVars.keySet();
    }

    public IntermediateVariable getVariable(String varName) {
        return ilVars.get(varName);
    }
    
    NodeBody getNodeBody() {
        return myNode.getNodeBody();
    }
    
    private boolean isEmptyNode() {
        return getNodeBody().getClass().equals(NodeBody.class);
    }
    
    private boolean isAssignmentNode() {
        return getNodeBody() instanceof AssignmentBody;
    }
    
    private boolean isUpdateNode() {
        return getNodeBody() instanceof UpdateBody;
    }
    
    private boolean isCommandNode() {
        return getNodeBody() instanceof CommandBody;
    }

    private boolean isListNode() {
        return getNodeBody() instanceof NodeListBody;
    }
    
    private boolean isLibraryNode() {
        return getNodeBody() instanceof LibraryBody;
    }


    
    public boolean hasInterface() {
        return myNode.hasInterface();
    }
    
    /**
     * Check this node's Interface to see if the variable is explicitly 
     * removed from the interface. Note that nodes inherit their interfaces
     * from their parents, but this method doesn't check any of that. It's just
     * what this node says about its own interface. 
     * @param varName
     * @return
     */
    public boolean isReadable(String varName) {
        if (hasInterface()) {
            return myNode.getInterfaceReadOnlyVars().contains(varName)
                    || myNode.getInterfaceWriteableVars().contains(varName);
        }
        return true;
    }
    /**
     * Check this node's Interface to see if the variable is explicitly 
     * set as read-only (or not visible at all). Note that nodes inherit their 
     * interfaces from their parents, but this method doesn't check any of that.
     * It's just what this node says about its own interface. 
     * @param varName
     * @return
     */

    public boolean isWritable(String varName) {
        if (hasInterface()) {
            return myNode.getInterfaceWriteableVars().contains(varName);
        }
        return true;
    }
    
    private IntermediateVariable resolveVariable(String name, boolean writing) {
        // Variables have lexical scope, which mean they are visible only 
        // within the action and any descendants of the action. Scope can be 
        // explicitly limited using the Interface clause. 
        
        // Is it in this Node? That would be pretty easy.
        if (getAllVariables().contains(name)) {
            return getVariable(name);
        }
        
        // It's not here. We need to make sure that our interface
        // allows us to look further.
        if (! isReadable(name)) {
            // There's no such thing as a "write-only" variable, so we don't
            // have access to this variable at all. 
            throw new RuntimeException("Variable "+name+" does not exist " +
                    "in the interface of "+getUID());
        } else if (writing && ! isWritable(name)) {
            throw new RuntimeException("Cannot write to variable "+name+" in "+getUID());
        }
        
        // We are allowed to look further. Is there a parent?
        if (parent == null) {
            // Nope, we are the root node. At this point, we have to assume
            // that we are a library node. Maybe they mistyped a name though.
            // I guess they'll figure that out on their own... 
            // TODO: See if there's a way to statically check this.
            return new AliasedVariableReference(name);
        } else {
            // This is their problem now.
            return parent.resolveVariable(name, writing);
        }
    }

    public ILExpression resolveVariable(String name, PlexilType type) {
        ILExpression expr = resolveVariable(name, false);
        if (expr instanceof AliasedVariableReference) {
            // We should try to cast this to the right type.
            if (type == PlexilType.BOOLEAN) {
                return Operation.castToBoolean(expr);
            } else if (type.isNumeric()) {
                return Operation.castToNumeric(expr);
            } else if (type == PlexilType.STRING) {
                return Operation.castToString(expr);
            }
        }
        return expr;
    }
    
    public IntermediateVariable resolveVariableforWriting(Expression e) {
        if (e instanceof UnresolvedVariableExpr) {
            UnresolvedVariableExpr var = (UnresolvedVariableExpr) e;
            return resolveVariable(var.getName(), true);
        } else if (e instanceof ArrayIndexExpr) {
            ArrayIndexExpr arr = (ArrayIndexExpr) e;
            return new ArrayElementReference(
                    (ArrayReference) resolveVariableforWriting(arr.getArray()), toIL(arr.getIndex()));
        }
        throw new RuntimeException(e+" does not look like a left-hand variable.");
    }
    
    public IntermediateVariable resolveVariableForWriting(String name, PlexilType type) {
        return resolveVariable(name, true);
    }
    
    public NodeToIL resolveNode(String plexilId) {
        // A PLEXIL action can access its own internal state, or the internal 
        // state of other actions, but only those actions which are its 
        // siblings, children, or ancestors.
        
        // TODO: I'm going to check them in that order. Is that right? 
        // TODO: Also, I'm taking "children" to mean "immediate children", which
        // I think is right but [citation needed]. 
    	
    	if (plexilId.equals(this.myNode.getPlexilID())) {
    		// Well, that was easy.
    		return this;
    	}
        
    	// Expand the search to siblings.
        if (parent != null) {
            List<NodeToIL> siblings = parent.getChildren();
            for (NodeToIL sibling : siblings) {
                if (sibling.myNode.getPlexilID().equals(plexilId)) {
                    return sibling;
                }
            }
        }
        
        // Children next.
        for (NodeToIL child : children) {
            if (child.myNode.getPlexilID().equals(plexilId)) {
                return child;
            }

        }
        
        // Now ancestors. We do it ourselves because it's not too much work,
        // and also we don't want them to check their children, siblings, etc.
        NodeToIL ptr = this.parent;
        while (ptr != null) {
            if (ptr.myNode.getPlexilID().equals(plexilId)) {
                return ptr;
            } else {
                ptr = ptr.parent;
            }
        }
        
        throw new RuntimeException("Plexil node ID not found: "+plexilId);
    }
    
    
    public void translate(Plan ilPlan) {
        // The two big componenets are our variables and the state machine.
        
        // We made the variables when we were constructed. 
        for (String variable : ilVars.keySet()) {
            ilPlan.addVariable(ilVars.get(variable));
        }
        
        // Now the big state machine. 
        NodeStateMachine nsm = new NodeStateMachine(getUID(), ilPlan);
        Map<NodeState,State> map = new HashMap<NodeState, State>();
        for (NodeState state : NodeState.values()) {
            map.put(state, new State(getUID(), state, nsm));
        }
        
        addInactiveTransitions(nsm, map);
        addWaitingTransitions(nsm, map);
        addExecutingTransitions(nsm, map);
        addFinishingTransitions(nsm, map);
        addFailingTransitions(nsm, map);
        addIterationEndedTransitions(nsm, map);
        addFinishedTransitions(nsm, map);

        // And of course, node actions. 
        
        if (isAssignmentNode()) {
            AssignmentBody body = (AssignmentBody) getNodeBody();
            IntermediateVariable lhs = resolveVariableforWriting(body.getLeftHandSide());
            AssignAction assign = new AssignAction(lhs, 
                    toIL(body.getRightHandSide()), myNode.getPriority());
            CaptureCurrentValueAction capture = new CaptureCurrentValueAction(lhs, getPreviousValue());
            map.get(NodeState.EXECUTING).addEntryAction(assign);
            map.get(NodeState.EXECUTING).addEntryAction(capture);
            
            SetVarToPreviousValueAction revert = new SetVarToPreviousValueAction(lhs, getPreviousValue(), myNode.getPriority());
            map.get(NodeState.FAILING).addEntryAction(revert);
        } else if (isCommandNode()) {
            CommandBody body = (CommandBody) getNodeBody();
            ILExpression name = toIL(body.getCommandName());
            List<ILExpression> args = toIL(body.getCommandArguments());
            CommandAction issueCmd = new CommandAction(getCommandHandle(), name, args);
            map.get(NodeState.EXECUTING).addEntryAction(issueCmd);
            
            // TODO: Implement aborting commands.
//            AbortCommandAction abort = new AbortCommandAction(getCommandHandle());
//            map.get(NodeState.FAILING).addEntryAction(abort);
            
        } else if (isUpdateNode()) {
            UpdateBody update = (UpdateBody) getNodeBody();
            UpdateAction doUpdate = new UpdateAction(getUpdateHandle());
            for ( Pair<String, ASTExpression> pair : update.getUpdates()) {
                doUpdate.addUpdatePair(pair.first, toIL(pair.second));
            }
            map.get(NodeState.EXECUTING).addEntryAction(doUpdate);
        } else if (isListNode()) {
            // Each child needs to be told to transition in all states but INACTIVE.
            List<NodeUID> childIds = new ArrayList<NodeUID>();
            for (NodeToIL child : children) {
                childIds.add(child.getUID());
            }
            AlsoRunNodesAction runChildren = new AlsoRunNodesAction(childIds, ilPlan);
            for (NodeState state : NodeState.values()) {
                if (state == NodeState.INACTIVE) continue;
                map.get(state).addInAction(runChildren);
            }
        } else if (isLibraryNode()) {
            // Similar deal here, we need to run the library node when not INACTIVE.
            RunLibraryNodeAction runLib = new RunLibraryNodeAction(getLibraryHandle());
            for (NodeState state : NodeState.values()) {
                if (state == NodeState.INACTIVE) continue;
                map.get(state).addInAction(runLib);
            }

        }
        
        // Assignment: Assign variable upon executing, restore variable upon failing.
        // Command: Issue command upon executing. Abort upon failing.
        // Update: Issue update upon executing.
        
        
        // Add the machine.
        ilPlan.addStateMachine(nsm);
        // Are we root?
        if (parent == null) {
            ilPlan.setRoot(nsm, getState(), getOutcome());
        }
        
        // Now our children should translate too.
        for (NodeToIL child : children) {
            child.translate(ilPlan);
        }
    }
    
    /*
     * Node state transition diagrams -----------------------------------------
     */
    
    private void addInactiveTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        // TODO: Update this to the new semantics.
        nsm.addTransition(makeTransition(map, 1, NodeState.INACTIVE, NodeState.FINISHED,
                parentIsFinished(Condition.TRUE)))
                .addAction(setOutcome(NodeOutcome.SKIPPED));

        nsm.addTransition(makeTransition(map, 1, NodeState.INACTIVE, NodeState.WAITING, 
                parentIsExecuting(Condition.TRUE)));
    }
    
    private void addWaitingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        {
            // All of these are the same, just different guards
            int priority = 0;
            for (TransitionGuard g : new TransitionGuard[]{ 
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

    private void addExecutingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        if (isEmptyNode()) {
            addEmptyExecutingTransitions(nsm, map);
        } else if (isAssignmentNode() || isUpdateNode()) {
            addSimpleExecutingTransitions(nsm, map);
        } else if (isCommandNode() || isLibraryNode() || isListNode()) {
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
    
    private void addFinishingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        if (isCommandNode()) {
            addFinishingTransitions(commandHandleKnown(Condition.TRUE), nsm, map);
        } else if (isListNode()) {
            addFinishingTransitions(allChildrenWaitingOrFinished(Condition.TRUE), nsm, map);
        } else if (isLibraryNode()) {
            addFinishingTransitions(libraryChildWaitingOrFinished(Condition.TRUE), nsm, map);
        }
    }
    
    private void addFinishingTransitions(TransitionGuard finishGuard, NodeStateMachine nsm, Map<NodeState,State> map) {
        nsm.addTransition(checkAncestorExit(map, 1, NodeState.FINISHING, NodeState.FAILING));
        nsm.addTransition(checkExit(map, 2, NodeState.FINISHING, NodeState.FAILING));
        nsm.addTransition(checkAncestorInvariant(map, 3, NodeState.FINISHING, NodeState.FAILING));
        nsm.addTransition(checkInvariant(map, 4, NodeState.FINISHING, NodeState.FAILING));

        Pair<Transition, Transition> postChecks = 
            checkPost(finishGuard, map, 5, NodeState.FINISHING, NodeState.ITERATION_ENDED);
        nsm.addTransition(postChecks.first);
        nsm.addTransition(postChecks.second);
    }
    
    private void addFailingTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        Transition parentExited = makeTransition(map, 1, NodeState.FAILING, NodeState.FINISHED, failureTypeIsParentExited(Condition.TRUE));
        Transition parentFailed = makeTransition(map, 2, NodeState.FAILING, NodeState.FINISHED, failureTypeIsParentFailed(Condition.TRUE));
        Transition myFault = makeTransition(map, 3, NodeState.FAILING, NodeState.ITERATION_ENDED);
        
        // There are additional guards that have to be met for some nodes:
        if (isCommandNode()) {
            TransitionGuard abortComplete = abortComplete(Condition.TRUE);
            parentExited.addGuard(abortComplete);
            parentFailed.addGuard(abortComplete);
            myFault.addGuard(abortComplete);
        } else if (isUpdateNode()) {
            TransitionGuard updateComplete = new TransitionGuard(
                    Description.UPDATE_INVOCATION_SUCCESS, getUpdateHandle(), Condition.TRUE);
            parentExited.addGuard(updateComplete);
            parentFailed.addGuard(updateComplete);
            myFault.addGuard(updateComplete);
        } else if (isListNode()) {
            TransitionGuard childrenWaitingOrFinished = allChildrenWaitingOrFinished(Condition.TRUE);
            parentExited.addGuard(childrenWaitingOrFinished);
            parentFailed.addGuard(childrenWaitingOrFinished);
            myFault.addGuard(childrenWaitingOrFinished);
        } else if (isLibraryNode()) {
            TransitionGuard childWaitingOrFinished = libraryChildWaitingOrFinished(Condition.TRUE);
            parentExited.addGuard(childWaitingOrFinished);
            parentFailed.addGuard(childWaitingOrFinished);
            myFault.addGuard(childWaitingOrFinished);

        }
    }
    
    private void addIterationEndedTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
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
    
    private void addFinishedTransitions(NodeStateMachine nsm, Map<NodeState,State> map) {
        nsm.addTransition(makeTransition(map, 1, NodeState.FINISHED, NodeState.INACTIVE, 
                parentIsWaiting(Condition.TRUE)))
                .addAction(getResetNodeAction());
    }

    
    /*
     * Support methods for creating the IL state machine ----------------------
     */
    
    private Transition makeTransition(Map<NodeState,State> map, int priority, 
            NodeState start, NodeState end,
            TransitionGuard...guards) {
        String desc = getUID() +" : "+start+" ("+priority+") -> "+end;
        Transition t = new Transition(desc, priority, map.get(start), map.get(end), guards);
        
        // Timepoints get set for every single transition
        // The starting state is now ending
        t.addAction(new SetTimepointAction(getNodeTimepoint(start, NodeTimepoint.END)));
        // And the destination state is now starting.
        t.addAction(new SetTimepointAction(getNodeTimepoint(end, NodeTimepoint.START)));
        
        return t;
    }
    
    private TransitionGuard makeGuard(Description d, Condition cond) {
        if ( ! ilExprCache.containsKey(d)) {
            throw new RuntimeException("Expression was not ready: "+d);
        }
        return new TransitionGuard(d, ilExprCache.get(d), cond);
    }
    
    private ResetNodeAction getResetNodeAction() {
        ResetNodeAction reset = new ResetNodeAction();
        for (String v : ilVars.keySet()) {
            reset.addVariableToReset(ilVars.get(v));
        }
        return reset;
    }
    
    private SetOutcomeAction setOutcome(NodeOutcome outcome) {
        return setOutcome(outcome, NodeFailureType.UNKNOWN);
    }
    
    private SetOutcomeAction setOutcome(NodeOutcome outcome, NodeFailureType failure) {
        return new SetOutcomeAction(getUID(), getOutcome(), outcome, getFailure(), failure);
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
    private Pair<Transition,Transition> checkPost(TransitionGuard guard, 
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
    
    private TransitionGuard startCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.START_CONDITION)) {
            ilExprCache.put(Description.START_CONDITION, 
                    toIL(myNode.getStartCondition()));
        }
        return makeGuard(Description.START_CONDITION, cond);
    }
    
    private TransitionGuard skipCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.SKIP_CONDITION)) {
            ilExprCache.put(Description.SKIP_CONDITION, 
                    toIL(myNode.getSkipCondition()));
        }
        return makeGuard(Description.SKIP_CONDITION, cond);
    }
    
    private TransitionGuard preCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.PRE_CONDITION)) {
            ilExprCache.put(Description.PRE_CONDITION, 
                    toIL(myNode.getPreCondition()));
        }
        return makeGuard(Description.PRE_CONDITION, cond);
    }
    
    private TransitionGuard invariantCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.INVARIANT_CONDITION)) {
            ilExprCache.put(Description.INVARIANT_CONDITION, 
                    toIL(myNode.getInvariantCondition()));
        }
        return makeGuard(Description.INVARIANT_CONDITION, cond);
    }
    
    private TransitionGuard repeatCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.REPEAT_CONDITION)) {
            ilExprCache.put(Description.REPEAT_CONDITION, 
                    toIL(myNode.getRepeatCondition()));
        }
        return makeGuard(Description.REPEAT_CONDITION, cond);
    }
    
    private TransitionGuard postCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.POST_CONDITION)) {
            ilExprCache.put(Description.POST_CONDITION, 
                    toIL(myNode.getPostCondition()));
        }
        return makeGuard(Description.POST_CONDITION, cond);
    }
    
    private TransitionGuard endCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.END_CONDITION)) {
            ilExprCache.put(Description.END_CONDITION, 
                    toIL(myNode.getEndCondition()));
        }
        return makeGuard(Description.END_CONDITION, cond);
    }
    
    private TransitionGuard exitCondition(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.EXIT_CONDITION)) {
            ilExprCache.put(Description.EXIT_CONDITION, 
                    toIL(myNode.getExitCondition()));
        }
        return makeGuard(Description.EXIT_CONDITION, cond);
    }
    
    private TransitionGuard ancestorEndsDisjoined(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.ANCESTOR_ENDS_DISJOINED)) {
            if (parent != null) {
                ilExprCache.put(Description.ANCESTOR_ENDS_DISJOINED, 
                        parent.getThisOrAncestorsEnds());
            } else {
                ilExprCache.put(Description.ANCESTOR_ENDS_DISJOINED,
                        new RootAncestorEndExpr());
            }
        }
        return makeGuard(Description.ANCESTOR_ENDS_DISJOINED, cond);
    }
    
    private TransitionGuard ancestorExitsDisjoined(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.ANCESTOR_EXITS_DISJOINED)) {
            if (parent != null) {
                ilExprCache.put(Description.ANCESTOR_EXITS_DISJOINED, 
                        parent.getThisOrAncestorsExits());
            } else {
                ilExprCache.put(Description.ANCESTOR_EXITS_DISJOINED, 
                        new RootAncestorExitExpr());
            }
        }
        return makeGuard(Description.ANCESTOR_EXITS_DISJOINED, cond);
    }
    
    private TransitionGuard ancestorInvariantsConjoined(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.ANCESTOR_INVARIANTS_CONJOINED)) {
            if (parent != null) {
                ilExprCache.put(Description.ANCESTOR_INVARIANTS_CONJOINED, 
                        getThisAndAncestorsInvariants());
            } else {
                ilExprCache.put(Description.ANCESTOR_INVARIANTS_CONJOINED, 
                        new RootAncestorInvariantExpr());
            }
        }
        return makeGuard(Description.ANCESTOR_INVARIANTS_CONJOINED, cond);
    }

    private TransitionGuard parentIsFinished(Condition cond) {
        return getParentIsInState(NodeState.FINISHED, Description.PARENT_FINISHED, cond);
    }

    private TransitionGuard parentIsExecuting(Condition cond) {
        return getParentIsInState(NodeState.EXECUTING, Description.PARENT_EXECUTING, cond);
    }

    private TransitionGuard parentIsWaiting(Condition cond) {
        return getParentIsInState(NodeState.WAITING, Description.PARENT_WAITING, cond);
    }

    private TransitionGuard getParentIsInState(NodeState state, Description d, Condition cond) {
        if ( ! ilExprCache.containsKey(d) ) {
            if (parent == null) {
                ilExprCache.put(d, 
                        Operation.eq(
                                new RootParentStateExpr(),
                                new PValueExpression(state)
                        ));
            } else {
                ilExprCache.put(d,
                        Operation.eq(
                                parent.getState(), 
                                new PValueExpression(state)));
            }

        }
        return makeGuard(d, cond);
    }

    private TransitionGuard failureTypeIsParentFailed(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.FAILURE_IS_PARENT_FAIL)) {
            ilExprCache.put(Description.FAILURE_IS_PARENT_FAIL,
                    Operation.eq(
                            getFailure(), 
                            new PValueExpression(NodeFailureType.PARENT_FAILED)));
        }
        
        return makeGuard(Description.FAILURE_IS_PARENT_FAIL, cond);
    }

    private TransitionGuard failureTypeIsParentExited(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.FAILURE_IS_PARENT_EXIT)) {
            ilExprCache.put(Description.FAILURE_IS_PARENT_EXIT,
                    Operation.eq(
                            getFailure(), 
                            new PValueExpression(NodeFailureType.PARENT_EXITED)));
        }
        
        return makeGuard(Description.FAILURE_IS_PARENT_EXIT, cond);
    }
    
    
    private TransitionGuard commandHandleKnown(Condition cond) {
        return new TransitionGuard(Description.COMMAND_ACCEPTED, 
                Operation.isKnown(getCommandHandle()), cond);
    }

    private TransitionGuard allChildrenWaitingOrFinished(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.ALL_CHILDREN_WAITING_OR_FINISHED)) {
            List<Expression> clauses = new ArrayList<Expression>();
            for (NodeToIL child : children) {
                clauses.add(
                        Operation.or(
                                Operation.eq(child.getState(), new PValueExpression(NodeState.WAITING)),
                                Operation.eq(child.getState(), new PValueExpression(NodeState.FINISHED))
                            )
                        );
            }
            ilExprCache.put(Description.ALL_CHILDREN_WAITING_OR_FINISHED, Operation.and(clauses));
        }
        return makeGuard(Description.ALL_CHILDREN_WAITING_OR_FINISHED, cond);
    }
    
    private TransitionGuard libraryChildWaitingOrFinished(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.ALL_CHILDREN_WAITING_OR_FINISHED)) {
            ilExprCache.put(Description.ALL_CHILDREN_WAITING_OR_FINISHED, 
                    Operation.or(
                            Operation.eq(getLibraryHandle(), new PValueExpression(NodeState.WAITING)),
                            Operation.eq(getLibraryHandle(), new PValueExpression(NodeState.FINISHED))
                    ));
        }
        return makeGuard(Description.ALL_CHILDREN_WAITING_OR_FINISHED, cond);
        
    }
    
    private TransitionGuard abortComplete(Condition cond) {
        if ( ! ilExprCache.containsKey(Description.COMMAND_ABORT_COMPLETE)) {
            ilExprCache.put(Description.COMMAND_ABORT_COMPLETE, 
                    Operation.eq(getCommandHandle(), new PValueExpression(CommandHandleState.COMMAND_ABORTED)));
        }
        return makeGuard(Description.COMMAND_ABORT_COMPLETE, cond);
    }
}
