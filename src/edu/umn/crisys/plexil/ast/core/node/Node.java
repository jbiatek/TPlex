package edu.umn.crisys.plexil.ast.core.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.ast.core.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.core.globaldecl.PlexilInterface;
import edu.umn.crisys.plexil.ast.core.globaldecl.VariableDecl;
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
    
    // Our variable interface
    private PlexilInterface iface = new PlexilInterface();

    // Variables declared in this node
    private List<VariableDecl> vars = new ArrayList<VariableDecl>();

    public List<VariableDecl> getVariableList() {
        return vars;
    }
    
    public VariableDecl getVariable(String name) {
    	for (VariableDecl v : vars) {
    		if (v.getName().equals(name)) {
    			return v;
    		}
    	}
    	return null;
    }
    
    @Deprecated
    public PlexilType getVarType(String name) { 
        return getVariable(name).getType(); 
    }
    
    @Deprecated
    public PValueExpression getInitVariable(String varName) { 
    	if ( ! getVariable(varName).hasInitialValue()) { return null; }
    	
        return (PValueExpression) getVariable(varName).getInitialValue(); 
    }
    @Deprecated
    public ArrayLiteralExpr getInitArray(String arrayName) {
    	if ( ! getVariable(arrayName).hasInitialValue()) { return null; }
    	
        return (ArrayLiteralExpr) getVariable(arrayName).getInitialValue();
    }
    
    @Deprecated
    public boolean containsVar(String name) {
        return getVariable(name) != null; 
    }

    @Deprecated
    public int getArraySize(String name) {
    	return getVariable(name).getArraySize();
    }

    @Deprecated
    public void addVar(String name, PlexilType type) { 
        if (type.isArrayType()) {
            throw new RuntimeException("Must include size for array type");
        }
        vars.add(new VariableDecl(name, type)); 
    }
    
    @Deprecated
    public void addVar(String name, PlexilType type, PValueExpression init) {
    	vars.add(new VariableDecl(name, type, init));
    }
    @Deprecated
    public void addArray(String name, PlexilType t, int maxSize) {
        if (! t.isArrayType()) {
            throw new RuntimeException(t+" is not an array type");
        }
        if (maxSize < 0) { 
            throw new RuntimeException("Array cannot have negative size: "+maxSize);
        }
        vars.add(new VariableDecl(name, maxSize, t));
    }

    @Deprecated
    public void addArray(String name, PlexilType t, int maxSize, ArrayLiteralExpr init) {
    	vars.add(new VariableDecl(name, maxSize, t, init));
    }

    /**
     * If true, access to non-local variables is restricted. There is a 
     * whitelist, as returned by getInterfaceReadOnlyVars() and 
     * getInterfaceWriteableVars(). Access to other variables above this node
     * is not allowed. 
     * @return
     */
    @Deprecated
    public boolean hasInterface() {
        return iface.isDefined();
    }
    /**
     * If this node hasInterface(), this is the list of variables that are 
     * allowed through that interface as read only. Trying to assign to any of
     * these should be an error.
     */
    @Deprecated
    public Set<String> getInterfaceReadOnlyVars() { 
    	return iface.getInterfaceReadOnlyVars(); 
    }

    @Deprecated
    public void restrictInterface() {
    	iface.restrictInterface();
    }
    
    @Deprecated
    public void addToInterfaceReadOnly(String varName, PlexilType t) {
    	iface.addInVariable(varName, t);
    }

    @Deprecated
    public void addToInterfaceWriteable(String varName, PlexilType t) {
    	iface.addInOutVariable(varName, t);
    }

    /**
     * If this node hasInterface(), this is the list of variables that are 
     * allowed through that interface as readable and writeable. 
     */
    @Deprecated
    public Set<String> getInterfaceWriteableVars() { 
    	return iface.getInterfaceWriteableVars();
    }

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
