package edu.umn.crisys.plexil.ast2il;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.umn.crisys.plexil.ast.Node;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBodyVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.NamedCondition;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.expr.common.Operation;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorEndExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorExitExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.PlexilExprDescription;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.NodeTimepoint;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;

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
 * <li> Creating the IL state machine. That's handled by 2 other classes, one
 * that knows the PLEXIL state transition diagrams, and one that takes the
 * NodeBody and adds the appropriate actions to the state machine.
 * 
 * @author jbiatek
 *
 */
public class NodeToIL {
    
	public static ExprType TIMEPOINT_TYPE = ExprType.REAL;
	
    //private static final String STATE = ".state";
    private static final String OUTCOME = ".outcome";
    private static final String FAILURE = ".failure";
    private static final String COMMAND_HANDLE = ".command_handle";
    private static final String LIBRARY_HANDLE = ".library_handle";
    private static final String UPDATE_HANDLE = ".update_handle";
    
    private Node myNode;
    private NodeUID myUid;
    private Optional<NodeToIL> parent;
    private ASTExprToILExpr exprToIL = new ASTExprToILExpr(this);
    
    private List<NodeToIL> children = new ArrayList<NodeToIL>();
    
    private Map<String, ILVariable> ilVars = new HashMap<String, ILVariable>();
    private Map<String, Expression> aliases = new HashMap<String, Expression>();
    
    public NodeToIL(Node node) {
        this(node, null);
    }
    
    public NodeToIL(Node node, NodeToIL parent) {
        this.myNode = node;
        this.parent = Optional.ofNullable(parent);
        
        if (this.parent.isPresent()) {
            myUid = new NodeUID(parent.myUid, node.getPlexilID());
        } else {
            myUid = new NodeUID(node.getPlexilID());
        }
        // We should create the IL variables now.
        createILVars();
        checkForChildren();
        
        if ( ! this.parent.isPresent()) {
            // Okay, now we can do this stuff.
        	myUid.shortenUniqueNames();
            parentsAreReady();
        }
    }
    
    private void createILVars() {
        // Internal vars for all nodes
        ilVars.put(OUTCOME, new SimpleVar(OUTCOME, myUid, ExprType.OUTCOME));
        ilVars.put(FAILURE, new SimpleVar(FAILURE, myUid, ExprType.FAILURE));
        // Node timepoints
        for (NodeState state : NodeState.values()) {
            for (NodeTimepoint tpt : NodeTimepoint.values()) {
            	String name = "."+state+"."+tpt;
            	
                ilVars.put(name, new SimpleVar(name, myUid, TIMEPOINT_TYPE));
            }
        }
        
        // Variables defined by the programmer in the node
        for (VariableDecl v : myNode.getAllVariables()) {
        	String varName = v.getName();
            ExprType type = v.getType();
            if (type.isArrayType()) {
                // Array variables.
                PValueList<?> init = null;
                
                if (v.getInitialValue().isPresent() ) { 
                	init = (PValueList<?>) v.getInitialValue().get();
                }
                int arraySize = v.getArraySize().get();
                
                ilVars.put(varName, new ArrayVar(varName, arraySize, type, myUid, init));
            } else {
                // Standard variables.
            	PValue init = null;
            	if (v.getInitialValue().isPresent()) {
            		init = (PValue) v.getInitialValue().get();
            	}
                ilVars.put(varName, new SimpleVar(varName, myUid, type, init));
            }
        }
        
        // Special variables based on the type of node this is.
        if (myNode.isCommandNode()) {
            ilVars.put(COMMAND_HANDLE, new SimpleVar(COMMAND_HANDLE, myUid, ExprType.COMMAND_HANDLE));
        } else if (myNode.isUpdateNode()) {
            ilVars.put(UPDATE_HANDLE, new SimpleVar(UPDATE_HANDLE, myUid, ExprType.BOOLEAN, BooleanValue.get(false)));
        } else if (myNode.isLibraryNode()) {
            LibraryBody lib = myNode.getLibraryBody();
            Map<String,Expression> aliases = new HashMap<String, Expression>();
            
            for (String alias : lib.getAliases()) {
                aliases.put(alias, toIL(lib.getAlias(alias)));
            }
            
            LibraryVar libRef = new LibraryVar(
                    lib.getNodeId(), getUID(), getState(), aliases);
            ilVars.put(LIBRARY_HANDLE, libRef);
            
            // NOTE: This Library variable still needs to be given its
            // ancestor conditions, so that it can tell the library what's
            // happening above it. But that has to happen *after* all nodes
            // have had a chance to initialize. An invariant, for example,
            // could refer to children not failing, but we're still putting
            // together the tree. 
        }
    }
    
    private void checkForChildren() {
        if (myNode.isListNode()) {
            for (Node child : myNode.getNodeListBody()) {
                children.add(new NodeToIL(child, this));
            }
        }
    }
    
    private void parentsAreReady() {
        // This is called after construction when our parent nodes have finished
        // constructing themselves. We should do what we need to do, then pass
        // the message along to our children.
        
    	if (this.hasLibraryHandle()) {
            // We have to set these after everyone is initialized. 
            getLibraryHandle().setLibAndAncestorsInvariants(getThisAndAncestorsInvariants());
            getLibraryHandle().setLibOrAncestorsEnds(getThisOrAncestorsEnds());
            getLibraryHandle().setLibOrAncestorsExits(getThisOrAncestorsExits());
        } 
        
        for (NodeToIL child : children) {
            child.parentsAreReady();
        }
    }
    
    /**
     * If this NodeToIL object represents a Library node, this method will take
     * the given PLEXIL node and insert it as this node's child. This node
     * will become a List node with a single child instead. Use this to 
     * statically include libraries in the PLEXIL plan at compile time instead
     * of doing it dynamically at runtime. 
     */
    public void convertLibraryToList(Node library) {
    	if ( ! library.getPlexilID().equals(getLibraryHandle().getLibraryPlexilID())) {
    		throw new RuntimeException(
    				"Statically including the wrong library? The given one is "
    						+library.getPlexilID()+" but the library node looks for "
    						+getLibraryHandle().getLibraryPlexilID());
    	}
    	// Okay, we are about to throw out the Library body, so steal their 
    	// aliases first. 
    	aliases = getLibraryHandle().getAliases();
    	ilVars.remove(LIBRARY_HANDLE);
    	
    	// Now we simulate the constructor process.
    	NodeToIL libChild = new NodeToIL(library, this);
    	children.add(libChild);
    	libChild.parentsAreReady();
    }
    
    /**
     * @param e
     * @return the expression, translated in this context
     */
    Expression toIL(Expression e) {
        return e.accept(exprToIL, null);
    }
    
    List<Expression> toIL(List<? extends Expression> list) {
        List<Expression> ret = new ArrayList<Expression>();
        for (Expression e : list) {
            ret.add(toIL(e));
        }
        return ret;
    }
    
    public void translateConditions(Map<PlexilExprDescription, Expression> map) {
    	map.put(PlexilExprDescription.START_CONDITION, 
    			toIL(myNode.getStartCondition()));
    	map.put(PlexilExprDescription.SKIP_CONDITION, 
    			toIL(myNode.getSkipCondition()));
    	map.put(PlexilExprDescription.PRE_CONDITION, 
    			toIL(myNode.getPreCondition()));
    	map.put(PlexilExprDescription.INVARIANT_CONDITION, 
    			toIL(myNode.getInvariantCondition()));
    	map.put(PlexilExprDescription.REPEAT_CONDITION, 
    			toIL(myNode.getRepeatCondition()));
    	map.put(PlexilExprDescription.POST_CONDITION, 
    			toIL(myNode.getPostCondition()));
    	map.put(PlexilExprDescription.END_CONDITION, 
    			toIL(myNode.getEndCondition()));
    	map.put(PlexilExprDescription.EXIT_CONDITION, 
    			toIL(myNode.getExitCondition()));
    }
    
    public int getPriority() {
    	return myNode.getPriority();
    }
    
    public Optional<String> getPlexilID() {
    	return myNode.getPlexilID();
    }
    
    NodeBody getASTNodeBody() {
    	return myNode.getNodeBody();
    }

    public Optional<NodeToIL> getParent() {
    	return parent;
    }
    
    public GetNodeStateExpr getState() {
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

    public ILVariable getNodeTimepoint(NodeState state, NodeTimepoint time) {
        ILVariable tpt = ilVars.get("."+state+"."+time);
        if (tpt == null) {
            throw new RuntimeException("Went looking for "+time+" of "+state+", got "+tpt);
        }
        return tpt;
    }
    
    private Expression name(Expression e, PlexilExprDescription desc) {
    	return new NamedCondition(e, myUid, desc);
    }
    
    Expression getThisOrAncestorsExits() {
        Expression myExit = name(toIL(myNode.getExitCondition()),
        		PlexilExprDescription.EXIT_CONDITION);
        Expression parentExit = 
        		parent.map(p -> name(p.getThisOrAncestorsExits(),
        				PlexilExprDescription.ANCESTOR_EXITS_DISJOINED))
        			.orElse(new RootAncestorExitExpr());
        
        return Operation.or(myExit, parentExit);
    }

    Expression getThisOrAncestorsEnds() {
        Expression myEnd = name(toIL(myNode.getEndCondition()),
        		PlexilExprDescription.END_CONDITION);
        Expression parentEnd = 
        		parent.map(p -> name(p.getThisOrAncestorsEnds(),
        				PlexilExprDescription.ANCESTOR_ENDS_DISJOINED))
        			.orElse(new RootAncestorEndExpr());
        
        return Operation.or(myEnd, parentEnd);
    }

    Expression getThisAndAncestorsInvariants() {
        Expression myInv = name(toIL(myNode.getInvariantCondition()),
        		PlexilExprDescription.INVARIANT_CONDITION);
        Expression parentInv = 
        		parent.map(p -> name(p.getThisAndAncestorsInvariants(),
        				PlexilExprDescription.ANCESTOR_INVARIANTS_CONJOINED))
        			.orElse(new RootAncestorInvariantExpr());
        
        return Operation.and(myInv, parentInv);
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
    private Optional<Expression> resolveVariableInternal(String name, boolean writing) {
        // Variables have lexical scope, which mean they are visible only 
        // within the action and any descendants of the action. Scope can be 
        // explicitly limited using the Interface clause. 
        
        // Is it in this Node? That would be pretty easy.
        if (getAllVariables().contains(name)) {
            return Optional.of(getVariable(name));
        }
        
        // Or if we have it as an alias, that works too.
        if (aliases.containsKey(name)) {
        	return Optional.of(aliases.get(name));
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
        
        // We are allowed to look further. Ask our parent, otherwise we've
        // run out of options.
        return parent.map((parent) -> parent.resolveVariableInternal(name, writing))
        	.orElse(Optional.empty());
    }

    /**
     * Given a variable name, resolves it into an Expression. Don't use this
     * if you're going to be assigning to it! 
     * 
     * @param name
     * @param type
     * @return
     */
    public Expression resolveVariable(String name, ExprType type) {
        Optional<Expression> expr = resolveVariableInternal(name, false);
        if ( ! expr.isPresent()) {
        	// We went all the way up to the root, and no one claimed this
        	// variable as their own. It could be that this PLEXIL plan is a 
        	// library, and this variable is a part of our Interface.
        	// It could also be the case that someone messed up. 
        	
        	Expression alias = createAlias(name, false);
        	
        	// Since it's an alias, we should cast it if we know the type.
            if (type == ExprType.BOOLEAN) {
                return Operation.castToBoolean(alias);
            } else if (type.isNumeric()) {
                return Operation.castToNumeric(alias);
            } else if (type == ExprType.STRING) {
                return Operation.castToString(alias);
            } else {
            	return alias;
            }

        	
        }
        return expr.get();
    }
    
    private List<NodeToIL> getParents() {
    	List<NodeToIL> list = new ArrayList<NodeToIL>();
    	
    	Optional<NodeToIL> lastNode =  Optional.of(this);
    	while (lastNode.isPresent()) {
    		list.add(lastNode.get());
    		lastNode = lastNode.get().getParent();
    	}
    	return list;
    }
    
    private Expression createAlias(String name, boolean writeable) {
    	// Since a variable typo is indistinguishable from an alias
    	// when there isn't an explicit Interface, we want to do some basic
    	// checking, and print a warning if they probably didn't mean it. 
    	// Let's go back up the parent tree, and see if someone has an
    	// Interface. 
    	boolean foundInterface = getParents().stream()
    			.anyMatch((node) -> node.myNode.getInterface().isDefined()
    					&& node.myNode.getInterface().isReadable(name));
    	
    	if ( ! foundInterface) {
    		// No interfaces explicitly mention this variable. Technically,
    		// this is still valid PLEXIL code. But it's not written well,
    		// and it's possible that this is a variable that got typo'd.
    		System.err.println("Warning: Variable "+name+" is not clearly defined in node "+
    				myNode.getPlexilID().orElse("(anonymous)")+".");
    		System.err.println("If this plan isn't used as a library inside of a plan that defines "+name+", it will crash.");
    	}
    	
    	return new AliasExpr(name, ExprType.UNKNOWN, writeable);
    }
    
    public Expression resolveVariableForWriting(Expression e) {
    	if ( ! e.isAssignable()) {
    		throw new RuntimeException(e+" is not a valid LHS");
    	}
    	
        if (e instanceof UnresolvedVariableExpr) {
            UnresolvedVariableExpr var = (UnresolvedVariableExpr) e;
            Optional<Expression> expr = resolveVariableInternal(var.getName(), true);
            return expr.orElseGet( () -> createAlias(var.getName(), true));
        } else if (e instanceof ArrayIndexExpr) {
            ArrayIndexExpr arr = (ArrayIndexExpr) e;
            return new ArrayIndexExpr(
                    (ArrayVar) resolveVariableForWriting(arr.getArray()), toIL(arr.getIndex()));
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
    	
    	if (getPlexilID().isPresent() && getPlexilID().get().equals(plexilId)) {
    		// Well, that was easy.
    		return this;
    	}
        
    	// Expand the search to siblings.
        if (parent.isPresent()) {
            List<NodeToIL> siblings = parent.get().getChildren();
            for (NodeToIL sibling : siblings) {
                if (sibling.getPlexilID().isPresent() &&
                		sibling.getPlexilID().get().equals(plexilId)) {
                    return sibling;
                }
            }
        }
        
        // Children next.
        for (NodeToIL child : children) {
            if (child.getPlexilID().isPresent() && 
            		child.getPlexilID().get().equals(plexilId)) {
                return child;
            }
        }
        
        // Now ancestors. 
        for (NodeToIL parent : getParents()) {
        	if (parent.getPlexilID().isPresent() 
        			&& parent.getPlexilID().get().equals(plexilId)) {
        		return parent;
        	}
        }
        throw new RuntimeException("Plexil node ID not found: "+plexilId);
    }
    
    public ExprType getTypeOfLookup(String lookupName) {
    	return myNode.getPlan().getStateDeclarations().stream()
    	.filter(ld -> ld.getName().equals(lookupName))
    	.findFirst().map(ld -> ld.getReturnValue().map(vd -> vd.getType())
    			.orElse(ExprType.UNKNOWN))
    			.orElse(ExprType.UNKNOWN);
    }
    
    
    public void translate(final Plan ilPlan) {
        // The IL is basically a bag of variables and state machines.
        
        // We made the variables when we were constructed. 
        for (String variable : ilVars.keySet()) {
            ilPlan.addVariable(ilVars.get(variable));
        }
        
        // Now the big state machine. First, we need states.
        NodeStateMachine nsm = new NodeStateMachine(getUID(), ilPlan);
        final Map<NodeState,State> map = new HashMap<NodeState, State>();
        for (NodeState state : NodeState.values()) {
            map.put(state, new State(getUID(), state, nsm));
        }
        // Then we add the transitions. 
        StateMachineBuilder builder = new StateMachineBuilder(this, myNode);
        builder.addAllTransitions(nsm, map);
        
        
        // Finally, add actions based on our body type.
        NodeBodyVisitor<Void, Void> bodyTranslator = new NodeBodyToIL(this, map, ilPlan);
		myNode.getNodeBody().accept(bodyTranslator, null);
		
		
        // All done with the state machine, add it to the plan. 
        ilPlan.addStateMachine(nsm);
        // Are we root?
        if ( ! parent.isPresent()) {
            ilPlan.setRoot(nsm, getState(), getOutcome());
        }
        
        // Now our children should translate too.
        for (NodeToIL child : children) {
            child.translate(ilPlan);
        }
    }
    
}
