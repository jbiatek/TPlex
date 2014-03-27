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
import edu.umn.crisys.plexil.ast.core.globaldecl.VariableDecl;
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
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.EndMacroStep;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.expr.AliasExpr;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.NodeTimepoint;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.PlexilType;
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
    
    //private static final String STATE = ".state";
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
    
    private Map<String, ILVariable> ilVars = 
        new HashMap<String, ILVariable>();
    
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
        ilVars.put(OUTCOME, new SimpleVar(OUTCOME, myUid, PlexilType.OUTCOME));
        ilVars.put(FAILURE, new SimpleVar(FAILURE, myUid, PlexilType.FAILURE));
        // Node timepoints
        for (NodeState state : NodeState.values()) {
            for (NodeTimepoint tpt : NodeTimepoint.values()) {
            	String name = "."+state+"."+tpt;
            	
                ilVars.put(name, new SimpleVar(name, myUid, PlexilType.REAL));
            }
        }
        
        // Variables defined by the programmer in the node
        for (VariableDecl v : myNode.getAllVariables()) {
        	String varName = v.getName();
            PlexilType type = v.getType();
            if (type.isArrayType()) {
                // Array variables.
                PValueList<?> init = null;
                if (v.hasInitialValue() ) { 
                	init = (PValueList<?>) v.getInitialValue();
                }
                int arraySize = v.getArraySize();
                
                // Get the initial value, if any. If not, just initialize to an
                // empty array. 
                if (init == null) {
                    init = new PValueList<PValue>(type);
                }
                
                ilVars.put(varName, new ArrayVar(varName, arraySize, type, myUid, init));
            } else {
                // Standard variables.
            	PValue init = null;
            	if (v.hasInitialValue()) {
            		init = (PValue) v.getInitialValue();
            	}
                ilVars.put(varName, new SimpleVar(varName, myUid, type, init));
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
        if (myNode.isCommandNode()) {
            ilVars.put(COMMAND_HANDLE, new SimpleVar(COMMAND_HANDLE, myUid, PlexilType.COMMAND_HANDLE));
        } else if (myNode.isUpdateNode()) {
            ilVars.put(UPDATE_HANDLE, new SimpleVar(UPDATE_HANDLE, myUid, PlexilType.BOOLEAN, BooleanValue.get(false)));
        } else if (myNode.isLibraryNode()) {
            LibraryBody lib = (LibraryBody) myNode.getNodeBody();
            Map<String,ILExpression> aliases = new HashMap<String, ILExpression>();
            
            for (String alias : lib.getAliases()) {
                aliases.put(alias, toIL(lib.getAlias(alias)));
            }
            
            LibraryVar libRef = new LibraryVar(
                    lib.getNodeId(), getUID(), getState(), aliases);
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
    
    public ILExpression getState() {
        return new GetNodeStateExpr(myUid);
    }
    
    public ILVariable getOutcome() {
        return ilVars.get(OUTCOME);
    }
    
    public ILVariable getFailure() {
        return ilVars.get(FAILURE);
    }
    
    public boolean hasCommandHandle() {
        return ilVars.containsKey(COMMAND_HANDLE);
    }
    
    public SimpleVar getCommandHandle() {
        if (! hasCommandHandle()) {
            throw new RuntimeException("Not a command node: "+myNode.getNodeBody());
        }
        return (SimpleVar) ilVars.get(COMMAND_HANDLE);
    }
    
    public boolean hasLibraryHandle() {
        return ilVars.containsKey(LIBRARY_HANDLE);
    }

    public LibraryVar getLibraryHandle() {
        if (! hasLibraryHandle()) {
            throw new RuntimeException("Not a library node: "+myNode.getNodeBody());
        }
        return (LibraryVar) ilVars.get(LIBRARY_HANDLE);
    }
    
    private boolean hasUpdateHandle() {
        return ilVars.containsKey(UPDATE_HANDLE);
    }
    
    public SimpleVar getUpdateHandle() {
        if (! hasUpdateHandle()) {
            throw new RuntimeException("Not an update node: "+myNode.getNodeBody());
        }
        return (SimpleVar) ilVars.get(UPDATE_HANDLE);
    }
    
    private ILVariable getPreviousValue() {
        if ( ! ilVars.containsKey(PREVIOUS_VALUE)) {
            throw new RuntimeException("Doesn't contain previous value: "+this);
        }
        
        return ilVars.get(PREVIOUS_VALUE);
    }

    public ILVariable getNodeTimepoint(NodeState state, NodeTimepoint time) {
        ILVariable tpt = ilVars.get("."+state+"."+time);
        if (tpt == null) {
            throw new RuntimeException("Went looking for "+time+" of "+state+", got "+tpt);
        }
        return tpt;
    }
    
    ILExpression getThisOrAncestorsExits() {
        if (myNode.getExitCondition() instanceof BooleanValue && 
        		((BooleanValue) myNode.getExitCondition()).isFalse()) {
        	// We don't even need it.
        	if (parent != null) return parent.getThisOrAncestorsExits();
        	else return new RootAncestorExitExpr();
        }
    	
        ILExpression myExit = toIL(myNode.getExitCondition());
        if (parent != null) {
            return Operation.or(myExit, parent.getThisOrAncestorsExits());
        } else {
            return Operation.or(myExit, new RootAncestorExitExpr());
        }
    }

    ILExpression getThisOrAncestorsEnds() {
        if (myNode.getEndCondition() instanceof BooleanValue && 
        		((BooleanValue) myNode.getEndCondition()).isFalse()) {
        	// We don't even need it.
        	if (parent != null) return parent.getThisOrAncestorsEnds();
        	else return new RootAncestorEndExpr();
        }

    	
        ILExpression myEnd = toIL(myNode.getEndCondition());
        if (parent != null) {
            return Operation.or(myEnd, parent.getThisOrAncestorsEnds());
        } else {
            return Operation.or(myEnd, new RootAncestorEndExpr());
        }
    }

    ILExpression getThisAndAncestorsInvariants() {
        if (myNode.getInvariantCondition() instanceof BooleanValue && 
        		((BooleanValue) myNode.getInvariantCondition()).isTrue()) {
        	// We don't even need it.
        	if (parent != null) return parent.getThisAndAncestorsInvariants();
        	else return new RootAncestorInvariantExpr();
        }

    	
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

    public ILVariable getVariable(String varName) {
        return ilVars.get(varName);
    }
    
    /**
     * Find the node containing an ILVariable with the given name, and return
     * that ILVariable. Nothing is done about aliases, but it does check the
     * Interfaces as it goes up. 
     * 
     * @param name
     * @param writing
     * @return
     */
    private ILVariable resolveVariableInternal(String name, boolean writing) {
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
            // Nope, we are the root node. It's either an alias or an error.
        	return null;
        } else {
            // This is their problem now.
            return parent.resolveVariableInternal(name, writing);
        }
    }

    /**
     * Given a variable name, resolves it into an ILExpression. Don't use this
     * if you're going to be assigning to it! 
     * 
     * @param name
     * @param type
     * @return
     */
    public ILExpression resolveVariable(String name, PlexilType type) {
        ILExpression expr = resolveVariableInternal(name, false);
        if (expr == null) {
        	// We went all the way up to the root, and no one claimed this
        	// variable as their own. It could be that this PLEXIL plan is a 
        	// library, and this variable is a part of our Interface.
        	// It could also be the case that someone messed up. 
        	
        	ILExpression alias = createAlias(name, false);
        	
        	// Since it's an alias, we should cast it if we know the type.
            if (type == PlexilType.BOOLEAN) {
                return Operation.castToBoolean(alias);
            } else if (type.isNumeric()) {
                return Operation.castToNumeric(alias);
            } else if (type == PlexilType.STRING) {
                return Operation.castToString(alias);
            } else {
            	return alias;
            }

        	
        }
        return expr;
    }
    
    private ILExpression createAlias(String name, boolean writeable) {
    	// Since a variable typo is indistinguishable from an alias
    	// when there isn't an explicit Interface, we want to do some basic
    	// checking, and print a warning if they probably didn't mean it. 
    	// Let's go back up the parent tree, and see if someone has an
    	// Interface. 
    	NodeToIL pointer = this;
    	boolean foundInterface = false;
    	while (pointer != null) {
    		if (pointer.myNode.getInterface().isDefined() &&
    				pointer.myNode.getInterface().isReadable(name)) {
    			if (writeable && ! pointer.myNode.getInterface().isWritable(name)) {
    				throw new RuntimeException("Trying to write to read-only alias: "+name);
    			}
    			// Here we go. They probably didn't typo this, so don't bug 
    			// them.
    			foundInterface = true;
    			break;
    		} else {
    			pointer = pointer.parent;
    		}
    	}
    	if ( ! foundInterface) {
    		// No interfaces explicitly mention this variable. Technically,
    		// this is still valid PLEXIL code. But it's not written well,
    		// and it's possible that this is a variable that got typo'd.
    		System.err.println("Warning: Variable "+name+" is not clearly defined in node "+myNode.getPlexilID()+".");
    		System.err.println("If this plan isn't used as a library inside of a plan that defines "+name+", it will crash.");
    	}
    	
    	return new AliasExpr(name, PlexilType.UNKNOWN, writeable);
    }
    
    public ILExpression resolveVariableforWriting(Expression e) {
    	if ( ! e.isAssignable()) {
    		throw new RuntimeException(e+" is not a valid LHS");
    	}
    	
        if (e instanceof UnresolvedVariableExpr) {
            UnresolvedVariableExpr var = (UnresolvedVariableExpr) e;
            ILVariable expr = resolveVariableInternal(var.getName(), true);
            if (expr == null) {
            	// Must be an alias then. Or a typo.
            	return createAlias(var.getName(), true);
            }
            return expr;
        } else if (e instanceof ArrayIndexExpr) {
            ArrayIndexExpr arr = (ArrayIndexExpr) e;
            return new ArrayIndexExpr(
                    (ArrayVar) resolveVariableforWriting(arr.getArray()), toIL(arr.getIndex()));
        }
        throw new RuntimeException(e+" says it's assignable, but I don't know how to read it.");
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
            Expression lhsUntranslated = body.getLeftHandSide();
            ILExpression lhsExpr = resolveVariableforWriting(lhsUntranslated);
            ILExpression rhs = toIL(body.getRightHandSide());
            AssignAction assign = new AssignAction(lhsExpr, rhs, myNode.getPriority());
            // Add the previous value now that we have the IL left hand side
            PlexilType type = lhsUntranslated.getType();
            if (type == PlexilType.UNKNOWN) {
            	// If it's an array element, the type info isn't stored in the
            	// original XML (at least, not here.) Let's try the translated
            	// version:
            	if (lhsExpr.getType() != PlexilType.UNKNOWN) {
            		// Here we go. 
            		type = lhsExpr.getType();
            	} else if (rhs.getType() != PlexilType.UNKNOWN) {
            		type = rhs.getType();
            	} else {
            		throw new RuntimeException("Find the type of this assignment."); 
            	}
            }
            if (type.isArrayType()) {
            	// We need an initial value for it, but it'll be overwritten anyway.
            	// An empty array should be just fine.
            	ilVars.put(PREVIOUS_VALUE, new SimpleVar(PREVIOUS_VALUE, getUID(), type, new PValueList<PValue>(type)));
            } else {
            	// Just create an UNKNOWN one. 
            	ilVars.put(PREVIOUS_VALUE, new SimpleVar(PREVIOUS_VALUE, getUID(), type));
            }
            ilPlan.addVariable(getPreviousValue());
            
            AssignAction capture = new AssignAction(getPreviousValue(), lhsExpr, myNode.getPriority());
            AssignAction revert = new AssignAction(lhsExpr, getPreviousValue(), myNode.getPriority());
            
            map.get(NodeState.EXECUTING).addEntryAction(assign);
            map.get(NodeState.EXECUTING).addEntryAction(capture);
            map.get(NodeState.EXECUTING).addEntryAction(EndMacroStep.get());
            map.get(NodeState.FAILING).addEntryAction(revert);
            map.get(NodeState.FAILING).addEntryAction(EndMacroStep.get());
            
        } else if (myNode.isCommandNode()) {
            CommandBody body = (CommandBody) myNode.getNodeBody();
            ILExpression name = toIL(body.getCommandName());
            ILExpression returnTo = null;
            if (body.getVarToAssign() != null) {
            	returnTo = resolveVariableforWriting(body.getVarToAssign());
            }
            List<ILExpression> args = toIL(body.getCommandArguments());
            CommandAction issueCmd = new CommandAction(getCommandHandle(), name, args, returnTo);
            map.get(NodeState.EXECUTING).addEntryAction(issueCmd);
            map.get(NodeState.EXECUTING).addEntryAction(EndMacroStep.get());
            
            // TODO: Implement aborting commands.
//            AbortCommandAction abort = new AbortCommandAction(getCommandHandle());
//            map.get(NodeState.FAILING).addEntryAction(abort);
            
        } else if (myNode.isUpdateNode()) {
            UpdateBody update = (UpdateBody) myNode.getNodeBody();
            UpdateAction doUpdate = new UpdateAction(getUpdateHandle(), myNode.getPlexilID());
            for ( Pair<String, ASTExpression> pair : update.getUpdates()) {
                doUpdate.addUpdatePair(pair.first, toIL(pair.second));
            }
            map.get(NodeState.EXECUTING).addEntryAction(doUpdate);
            map.get(NodeState.EXECUTING).addEntryAction(EndMacroStep.get());
            
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
