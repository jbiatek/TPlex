package edu.umn.crisys.plexil.ast2il;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.ast.core.Node;
import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.ast.core.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.core.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.core.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.core.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.core.nodebody.UpdateBody;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AlsoRunNodesAction;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CaptureCurrentValueAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.SetVarToPreviousValueAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.NodeTimepoint;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PValueList;
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
                PValueList<?> init = myNode.getInitArray(varName);
                int arraySize = myNode.getArraySize(varName);
                
                // Get the initial value, if any. If not, just initialize to an
                // empty array. 
                if (init == null) {
                    init = new PValueList<PValue>(type);
                }
                
                ilVars.put(varName, new ArrayReference(myUid, varName, type, arraySize, init));
            } else {
                // Standard variables.
                PValue init = myNode.getInitVariable(varName);
                ilVars.put(varName, new VariableReference(myUid, varName, type, init));
            }
        }
    }
    
    private void checkForChildren() {
        if (myNode.isListNode()) {
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
        if (myNode.isCommandNode()) {
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
        } else if (myNode.isUpdateNode()) {
            ilVars.put(UPDATE_HANDLE, new UpdateHandleReference(myUid));
        } else if (myNode.isLibraryNode()) {
            LibraryBody lib = (LibraryBody) myNode.getNodeBody();
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
        } 
        
        for (NodeToIL child : children) {
            child.parentsAreReady();
        }
    }
    
    /**
     * @param e
     * @return the expression, translated in this context
     */
    ILExpression toIL(Expression e) {
        return e.accept(exprToIL, null);
    }
    
    List<ILExpression> toIL(List<? extends Expression> list) {
        List<ILExpression> ret = new ArrayList<ILExpression>();
        for (Expression e : list) {
            ret.add(toIL(e));
        }
        return ret;
    }
    
    public NodeBody getASTNodeBody() {
    	return myNode.getNodeBody();
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
    
    ILExpression getThisOrAncestorsExits() {
        ILExpression myExit = toIL(myNode.getExitCondition());
        if (parent != null) {
            return Operation.or(myExit, parent.getThisOrAncestorsExits());
        } else {
            return Operation.or(myExit, new RootAncestorExitExpr());
        }
    }

    ILExpression getThisOrAncestorsEnds() {
        ILExpression myEnd = toIL(myNode.getEndCondition());
        if (parent != null) {
            return Operation.or(myEnd, parent.getThisOrAncestorsEnds());
        } else {
            return Operation.or(myEnd, new RootAncestorEndExpr());
        }
    }

    ILExpression getThisAndAncestorsInvariants() {
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
        if (! myNode.getInterface().isReadable(name)) {
            // There's no such thing as a "write-only" variable, so we don't
            // have access to this variable at all. 
            throw new RuntimeException("Variable "+name+" does not exist " +
                    "in the interface of "+getUID());
        } else if (writing && ! myNode.getInterface().isWritable(name)) {
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
        
        if (parent != null) {
            List<NodeToIL> siblings = parent.getChildren();
            // This also includes ourself, and since siblings must have unique
            // names, we don't have to worry about doing us first.
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
        
        StateMachineBuilder builder = new StateMachineBuilder(this, myNode);
        builder.addInactiveTransitions(nsm, map);
        builder.addWaitingTransitions(nsm, map);
        builder.addExecutingTransitions(nsm, map);
        builder.addFinishingTransitions(nsm, map);
        builder.addFailingTransitions(nsm, map);
        builder.addIterationEndedTransitions(nsm, map);
        builder.addFinishedTransitions(nsm, map);

        // And of course, node actions. 
        
        if (myNode.isAssignmentNode()) {
            AssignmentBody body = (AssignmentBody) myNode.getNodeBody();
            IntermediateVariable lhs = resolveVariableforWriting(body.getLeftHandSide());
            AssignAction assign = new AssignAction(lhs, 
                    toIL(body.getRightHandSide()), myNode.getPriority());
            // Add the previous value now that we have the IL left hand side
            ilVars.put(PREVIOUS_VALUE, new PreviousValueReference(getUID(), lhs.getType()));
            ilPlan.addVariable(getPreviousValue());
            
            CaptureCurrentValueAction capture = new CaptureCurrentValueAction(lhs, getPreviousValue());
            map.get(NodeState.EXECUTING).addEntryAction(assign);
            map.get(NodeState.EXECUTING).addEntryAction(capture);
            
            SetVarToPreviousValueAction revert = new SetVarToPreviousValueAction(lhs, getPreviousValue(), myNode.getPriority());
            map.get(NodeState.FAILING).addEntryAction(revert);
        } else if (myNode.isCommandNode()) {
            CommandBody body = (CommandBody) myNode.getNodeBody();
            ILExpression name = toIL(body.getCommandName());
            List<ILExpression> args = toIL(body.getCommandArguments());
            CommandAction issueCmd = new CommandAction(getCommandHandle(), name, args);
            map.get(NodeState.EXECUTING).addEntryAction(issueCmd);
            
            // TODO: Implement aborting commands.
//            AbortCommandAction abort = new AbortCommandAction(getCommandHandle());
//            map.get(NodeState.FAILING).addEntryAction(abort);
            
        } else if (myNode.isUpdateNode()) {
            UpdateBody update = (UpdateBody) myNode.getNodeBody();
            UpdateAction doUpdate = new UpdateAction(getUpdateHandle());
            for ( Pair<String, ASTExpression> pair : update.getUpdates()) {
                doUpdate.addUpdatePair(pair.first, toIL(pair.second));
            }
            map.get(NodeState.EXECUTING).addEntryAction(doUpdate);
        } else if (myNode.isListNode()) {
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
        } else if (myNode.isLibraryNode()) {
            // Similar deal here, we need to run the library node when not INACTIVE.
            RunLibraryNodeAction runLib = new RunLibraryNodeAction(getLibraryHandle());
            for (NodeState state : NodeState.values()) {
                if (state == NodeState.INACTIVE) continue;
                map.get(state).addInAction(runLib);
            }

        }
        
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
    
}
