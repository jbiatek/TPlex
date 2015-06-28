package edu.umn.crisys.plexil.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.ast.globaldecl.PlexilInterface;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.ast.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.nodebody.UpdateBody;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ast.DefaultEndExpr;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;

public class Node {
	
    private Optional<Node> parent = Optional.empty();
    private Optional<String> plexilId = Optional.empty();

    private PlexilPlan thePlan;
    private NodeBody body = new NodeBody();
    // Node conditions:
    private Expression startCondition = BooleanValue.get(true);
    private Expression skipCondition = BooleanValue.get(false);
    private Expression preCondition = BooleanValue.get(true);
    private Expression invariantCondition = BooleanValue.get(true);
    private Expression repeatCondition = BooleanValue.get(false);
    private Expression postCondition = BooleanValue.get(true);
    private Expression endCondition = DefaultEndExpr.get();
    private Expression exitCondition = BooleanValue.get(false);
    
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
    }
    
    /** Construct a Node with a parent.
     * 
     * @param parent
     */
    public Node(Node parent) {
        this.parent = Optional.of(parent);
        this.thePlan = parent.getPlan();
    }
    
    /**
	 * @return this node's parent
	 */
	public Optional<Node> getParent() {
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
    public Optional<String> getPlexilID() {
        return plexilId;
    }
    public void setPlexilID(String id) { plexilId = Optional.of(id); }
    
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
    public void setStartCondition(Expression e) { startCondition = e; }
    public void setSkipCondition(Expression e) { skipCondition = e; }
    public void setPreCondition(Expression e) { preCondition = e; }
    public void setInvariantCondition(Expression e) { invariantCondition = e; }
    public void setRepeatCondition(Expression e) { repeatCondition = e; }
    public void setPostCondition(Expression e) { postCondition = e; }
    public void setEndCondition(Expression e) { endCondition = e; }
    public void setExitCondition(Expression e) { exitCondition = e; }
    
    public Expression getRepeatCondition() { return repeatCondition; }
    public Expression getStartCondition() { return startCondition; }
    public Expression getSkipCondition() { return skipCondition; }
    public Expression getPreCondition() { return preCondition; }
    public Expression getInvariantCondition() { return invariantCondition; }
    public Expression getPostCondition() { return postCondition; }
    public Expression getEndCondition() { return endCondition; }
    public Expression getExitCondition() { return exitCondition; }


}
