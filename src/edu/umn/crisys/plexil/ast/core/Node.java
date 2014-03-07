package edu.umn.crisys.plexil.ast.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.ast.core.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.core.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.core.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.core.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.core.nodebody.UpdateBody;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class Node {
	
    private Node parent = null;
    private String plexilId;

    private NodeBody body = new NodeBody();
    // Node conditions:
    private ASTExpression startCondition = PValueExpression.TRUE;
    private ASTExpression skipCondition = PValueExpression.FALSE;
    private ASTExpression preCondition = PValueExpression.TRUE;
    private ASTExpression invariantCondition = PValueExpression.TRUE;
    private ASTExpression repeatCondition = PValueExpression.FALSE;
    private ASTExpression postCondition = PValueExpression.TRUE;
    private ASTExpression endCondition = DefaultEndExpr.get();
    private ASTExpression exitCondition = PValueExpression.FALSE;
    
    private int priority = Integer.MAX_VALUE;
    // Variables declared in this node
    private Map<String, PlexilType> vars = new HashMap<String, PlexilType>();
    private Map<String, PValueExpression> varInitial = new HashMap<String, PValueExpression>();
    private Map<String, ArrayLiteralExpr> arrayInitial = new HashMap<String, ArrayLiteralExpr>();
    private Map<String, Integer> arraySizes = new HashMap<String, Integer>();

    // Variable interface
    private boolean hasDefinedInterface = false;
    private Map<String, PlexilType> varsIn = new HashMap<String, PlexilType>();
    private Map<String, PlexilType> varsInOut = new HashMap<String, PlexilType>();

    /**
     * Construct a root Node.
     */
    public Node() {
        this(null);
    }
    
    /** Construct a Node with a parent.
     * 
     * @param parent
     */
    public Node(Node parent) {
        this.parent = parent;
    }
    
    
    /**
	 * @return this node's parent, or null if it is the root node
	 */
	public Node getParent() {
	    return parent;
	}

	public void addVar(String name, PlexilType type) { 
	    if (type.isArrayType()) {
	        throw new RuntimeException("Must include size for array type");
	    }
	    vars.put(name, type); 
	}

	public void addVar(String name, PlexilType type, PValueExpression init) {
	    addVar(name, type);
	    varInitial.put(name, init);
	}

	public void addArray(String name, PlexilType t, int maxSize) {
        if (! t.isArrayType()) {
            throw new RuntimeException(t+" is not an array type");
        }
        if (maxSize < 0) { 
            throw new RuntimeException("Array cannot have negative size: "+maxSize);
        }
        vars.put(name, t);
        arraySizes.put(name, maxSize);
    }

    public void addArray(String name, PlexilType t, int maxSize, ArrayLiteralExpr init) {
        addArray(name, t, maxSize);
        arrayInitial.put(name, init);
    }
    
    public boolean containsVar(String name) {
        return vars.containsKey(name); 
    }

    public int getArraySize(String name) {
        if (! arraySizes.containsKey(name)) {
            throw new RuntimeException(name+" is not an array");
        }
        return arraySizes.get(name);
    }
    

    public ArrayLiteralExpr getInitArray(String arrayName) {
        return arrayInitial.get(arrayName);
    }
    public PValueExpression getInitVariable(String varName) { 
        return varInitial.get(varName); 
    }

    public Set<String> getVarNames() { 
        return vars.keySet(); 
    }

    public PlexilType getVarType(String name) { 
        return vars.get(name); 
    }

    /**
     * If true, access to non-local variables is restricted. There is a 
     * whitelist, as returned by getInterfaceReadOnlyVars() and 
     * getInterfaceWriteableVars(). Access to other variables above this node
     * is not allowed. 
     * @return
     */
    public boolean hasInterface() {
        return hasDefinedInterface;
    }
    
    /**
     * Enable this Node's interface. The default PLEXIL interface allows all
     * variables through, this turns the interface into a whitelist. The only
     * accessible variables will be the ones that are added to it.
     */
    public void restrictInterface() {
        hasDefinedInterface = true;
    }

    public void addToInterfaceReadOnly(String varName, PlexilType t) {
	    hasDefinedInterface = true;
	    varsIn.put(varName, t);
	}

	public void addToInterfaceWriteable(String varName, PlexilType t) {
	    hasDefinedInterface = true;
	    varsInOut.put(varName, t);
	}

	/**
     * If this node hasInterface(), this is the list of variables that are 
     * allowed through that interface as read only. Trying to assign to any of
     * these should be an error. 
     */
    public Set<String> getInterfaceReadOnlyVars() { return varsIn.keySet(); }
    
    /**
     * If this node hasInterface(), this is the list of variables that are 
     * allowed through that interface as readable and writeable. 
     */
    public Set<String> getInterfaceWriteableVars() { return varsInOut.keySet(); }

    
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
            return getInterfaceReadOnlyVars().contains(varName)
                    || getInterfaceWriteableVars().contains(varName);
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
            return getInterfaceWriteableVars().contains(varName);
        }
        return true;
    }


    public NodeBody getNodeBody() { return body; }

	public void setNodeBody(NodeBody b) { body = b; }
    

    public boolean isEmptyNode() {
        return getNodeBody().getClass().equals(NodeBody.class);
    }
    
    public boolean isAssignmentNode() {
        return getNodeBody() instanceof AssignmentBody;
    }
    
    public boolean isUpdateNode() {
        return getNodeBody() instanceof UpdateBody;
    }
    
    public boolean isCommandNode() {
        return getNodeBody() instanceof CommandBody;
    }

    public boolean isListNode() {
        return getNodeBody() instanceof NodeListBody;
    }
    
    public boolean isLibraryNode() {
        return getNodeBody() instanceof LibraryBody;
    }

	
    
    /**
     * @return The native Plexil node ID. Can be null.
     */
    public String getPlexilID() {
        return plexilId;
    }
    public void setPlexilID(String id) { plexilId = id; }

    public void setPriority(int p) { priority = p; }
    public int getPriority() { return priority; }
    
    // Condition getters and setters.
    public void setStartCondition(ASTExpression e) { startCondition = e; }
    public void setSkipCondition(ASTExpression e) { skipCondition = e; }
    public void setPreCondition(ASTExpression e) { preCondition = e; }
    public void setInvariantCondition(ASTExpression e) { invariantCondition = e; }
    public void setRepeatCondition(ASTExpression e) { repeatCondition = e; }
    public void setPostCondition(ASTExpression e) { postCondition = e; }
    public void setEndCondition(ASTExpression e) { endCondition = e; }
    public void setExitCondition(ASTExpression e) { exitCondition = e; }
    
    public ASTExpression getRepeatCondition() { return repeatCondition; }
    public ASTExpression getStartCondition() { return startCondition; }
    public ASTExpression getSkipCondition() { return skipCondition; }
    public ASTExpression getPreCondition() { return preCondition; }
    public ASTExpression getInvariantCondition() { return invariantCondition; }
    public ASTExpression getPostCondition() { return postCondition; }
    public ASTExpression getEndCondition() { return endCondition; }
    public ASTExpression getExitCondition() { return exitCondition; }


}
