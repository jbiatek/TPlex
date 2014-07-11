package edu.umn.crisys.plexil.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.globaldecl.PlexilInterface;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.ast.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.nodebody.UpdateBody;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;

public class Node {
	
    private Node parent = null;
    private String plexilId;

    private PlexilPlan thePlan;
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
    private List<VariableDecl> vars = new ArrayList<VariableDecl>();

    // Variable interface
    private PlexilInterface iface = new PlexilInterface();
    
    
    /**
     * Construct a root Node.
     */
    public Node(PlexilPlan plan) {
    	this.thePlan = plan;
    	this.parent = null;
    }
    
    /** Construct a Node with a parent.
     * 
     * @param parent
     */
    public Node(Node parent) {
        this.parent = parent;
        this.thePlan = parent.getPlan();
    }
    
    /**
	 * @return this node's parent, or null if it is the root node
	 */
	public Node getParent() {
	    return parent;
	}

	public void addVar(VariableDecl var) {
		vars.add(var);
	}

	public void addArray(VariableDecl array) {
		vars.add(array);
	}

    public boolean containsVar(String name) {
    	for (VariableDecl v : vars) { 
    		if (v.getName().equals(name)) {
    			return true;
    		}
    	}
        return false; 
    }

    public VariableDecl getVariableInfo(String name) {
    	for (VariableDecl v : vars) { 
    		if (v.getName().equals(name)) {
    			return v;
    		}
    	}
        throw new RuntimeException("Variable not found in this node: "+name); 
    }
    
    public List<VariableDecl> getAllVariables() {
    	return Collections.unmodifiableList(vars);
    }

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
    
    public PlexilPlan getPlan() { return thePlan; }

    public void setPriority(int p) { priority = p; }
    public int getPriority() { return priority; }
    
    public NodeBody getNodeBody() { return body; }
    public AssignmentBody getAssignmentBody() { return (AssignmentBody) body; }
    public UpdateBody getUpdateBody() { return (UpdateBody) body; }
    public CommandBody getCommandBody() { return (CommandBody) body; }
    public NodeListBody getNodeListBody() { return (NodeListBody) body; }
    public LibraryBody getLibraryBody() { return (LibraryBody) body; }
	public void setNodeBody(NodeBody b) { body = b; }

	public void setInterface(PlexilInterface iface) { this.iface = iface; }
	public PlexilInterface getInterface() { return iface; }

    
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
