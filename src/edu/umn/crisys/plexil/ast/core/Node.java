package edu.umn.crisys.plexil.ast.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.ast.core.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeBody;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class Node {

    /**
     * @return this node's parent, or null if it is the root node
     */
    public Node getParent() {
        return parent;
    }
    private Node parent = null;

    /**
     * @return The native Plexil node ID. Can be null.
     */
    public String getPlexilID() {
        return plexilId;
    }
    public void setPlexilID(String id) { plexilId = id; }
    private String plexilId;


    private NodeBody body = new NodeBody();
    public NodeBody getNodeBody() { return body; }
    public void setNodeBody(NodeBody b) { body = b; }

    // Node conditions:
    private ASTExpression startCondition = PValueExpression.TRUE;
    public ASTExpression getStartCondition() { return startCondition; }
    public void setStartCondition(ASTExpression e) { startCondition = e; }

    private ASTExpression skipCondition = PValueExpression.FALSE;
    public ASTExpression getSkipCondition() { return skipCondition; }
    public void setSkipCondition(ASTExpression e) { skipCondition = e; }

    private ASTExpression preCondition = PValueExpression.TRUE;
    public ASTExpression getPreCondition() { return preCondition; }
    public void setPreCondition(ASTExpression e) { preCondition = e; }

    private ASTExpression invariantCondition = PValueExpression.TRUE;
    public ASTExpression getInvariantCondition() { return invariantCondition; }
    public void setInvariantCondition(ASTExpression e) { invariantCondition = e; }

    private ASTExpression repeatCondition = PValueExpression.FALSE;
    public ASTExpression getRepeatCondition() { return repeatCondition; }
    public void setRepeatCondition(ASTExpression e) { repeatCondition = e; }

    private ASTExpression postCondition = PValueExpression.TRUE;
    public ASTExpression getPostCondition() { return postCondition; }
    public void setPostCondition(ASTExpression e) { postCondition = e; }

    private ASTExpression endCondition = DefaultEndExpr.get();
    public ASTExpression getEndCondition() { return endCondition; }
    public void setEndCondition(ASTExpression e) { endCondition = e; }

    private ASTExpression exitCondition = PValueExpression.FALSE;
    public ASTExpression getExitCondition() { return exitCondition; }
    public void setExitCondition(ASTExpression e) { exitCondition = e; }

    private int priority = Integer.MAX_VALUE;
    public int getPriority() { return priority; }
    public void setPriority(int p) { priority = p; }

    // Variables declared in this node
    private Map<String, PlexilType> vars = new HashMap<String, PlexilType>();
    private Map<String, PValueExpression> varInitial = new HashMap<String, PValueExpression>();
    private Map<String, ArrayLiteralExpr> arrayInitial = new HashMap<String, ArrayLiteralExpr>();
    private Map<String, Integer> arraySizes = new HashMap<String, Integer>();

    public Set<String> getVarNames() { 
        return vars.keySet(); 
    }
    public PlexilType getVarType(String name) { 
        return vars.get(name); 
    }
    public PValueExpression getInitVariable(String varName) { 
        return varInitial.get(varName); 
    }
    public ArrayLiteralExpr getInitArray(String arrayName) {
        return arrayInitial.get(arrayName);
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

    // Variable interface
    private boolean hasDefinedInterface = false;
    private Map<String, PlexilType> varsIn = new HashMap<String, PlexilType>();
    private Map<String, PlexilType> varsInOut = new HashMap<String, PlexilType>();

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
     * If this node hasInterface(), this is the list of variables that are 
     * allowed through that interface as read only. Trying to assign to any of
     * these should be an error.
     */
    public Set<String> getInterfaceReadOnlyVars() { return varsIn.keySet(); }

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
     * allowed through that interface as readable and writeable. 
     */
    public Set<String> getInterfaceWriteableVars() { return varsInOut.keySet(); }

    /** Construct a Node with a parent.
     * 
     * @param parent
     */
    public Node(Node parent) {
        this.parent = parent;
    }

    /**
     * Construct a root Node.
     */
    public Node() {
        this(null);
    }

}
