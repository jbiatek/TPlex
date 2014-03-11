package edu.umn.crisys.plexil.ast.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.core.globaldecl.PlexilInterface;
import edu.umn.crisys.plexil.ast.core.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.core.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.core.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.core.nodebody.UpdateBody;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class Node {
	
    private Node parent = null;
    private String plexilId;

    private NodeBody body = new NodeBody();
    // Node conditions:
    private ASTExpression startCondition = BooleanValue.get(true);
    private ASTExpression skipCondition = BooleanValue.get(false);
    private ASTExpression preCondition = BooleanValue.get(true);
    private ASTExpression invariantCondition = BooleanValue.get(true);
    private ASTExpression repeatCondition = BooleanValue.get(false);
    private ASTExpression postCondition = BooleanValue.get(true);
    private ASTExpression endCondition = DefaultEndExpr.get();
    private ASTExpression exitCondition = BooleanValue.get(false);
    
    private int priority = Integer.MAX_VALUE;
    // Variables declared in this node
    private Map<String, PlexilType> vars = new HashMap<String, PlexilType>();
    private Map<String, PValue> varInitial = new HashMap<String, PValue>();
    private Map<String, Integer> arraySizes = new HashMap<String, Integer>();

    // Variable interface
    private PlexilInterface iface = new PlexilInterface();
    
    
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

	public void addVar(String name, PlexilType type, PValue init) {
		if (type.isArrayType()) {
			throw new RuntimeException(type+" is an array, it needs a max size");
		}
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

    public void addArray(String name, PlexilType t, int maxSize, PValueList<?> init) {
        addArray(name, t, maxSize);
        varInitial.put(name, init);
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
    

    public PValueList<?> getInitArray(String arrayName) {
        return (PValueList<?>) varInitial.get(arrayName);
    }
    public PValue getInitVariable(String varName) { 
        return varInitial.get(varName); 
    }

    public Set<String> getVarNames() { 
        return vars.keySet(); 
    }

    public PlexilType getVarType(String name) { 
        return vars.get(name); 
    }


    public NodeBody getNodeBody() { return body; }

	public void setNodeBody(NodeBody b) { body = b; }
    
	public PlexilInterface getInterface() { return iface; }

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
