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
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;

public class Node {
	
    private Optional<Node> parent = Optional.empty();
    private Optional<String> plexilId = Optional.empty();

    private PlexilPlan thePlan;
    private NodeBody body = new NodeBody();
    // Node conditions:
    private ILExpr startCondition = BooleanValue.get(true);
    private ILExpr skipCondition = BooleanValue.get(false);
    private ILExpr preCondition = BooleanValue.get(true);
    private ILExpr invariantCondition = BooleanValue.get(true);
    private ILExpr repeatCondition = BooleanValue.get(false);
    private ILExpr postCondition = BooleanValue.get(true);
    private Optional<ILExpr> endCondition = Optional.empty();
    private ILExpr exitCondition = BooleanValue.get(false);
    
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
    public void setStartCondition(ILExpr e) { startCondition = e; }
    public void setSkipCondition(ILExpr e) { skipCondition = e; }
    public void setPreCondition(ILExpr e) { preCondition = e; }
    public void setInvariantCondition(ILExpr e) { invariantCondition = e; }
    public void setRepeatCondition(ILExpr e) { repeatCondition = e; }
    public void setPostCondition(ILExpr e) { postCondition = e; }
    public void setEndCondition(ILExpr e) { endCondition = Optional.of(e); }
    public void setExitCondition(ILExpr e) { exitCondition = e; }
    
    public ILExpr getRepeatCondition() { return repeatCondition; }
    public ILExpr getStartCondition() { return startCondition; }
    public ILExpr getSkipCondition() { return skipCondition; }
    public ILExpr getPreCondition() { return preCondition; }
    public ILExpr getInvariantCondition() { return invariantCondition; }
    public ILExpr getPostCondition() { return postCondition; }
    public ILExpr getExitCondition() { return exitCondition; }

    public Optional<ILExpr> getEndCondition() { 
    	return endCondition; 
	}

}
