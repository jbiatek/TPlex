package edu.umn.crisys.plexil.il.action;

import java.util.LinkedList;
import java.util.List;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;

public class AlsoRunNodesAction implements PlexilAction {

    private List<NodeUID> nodesToRun = new LinkedList<NodeUID>();
    
    /**
     * Create an action causing the given nodes to take a step as well. Needs
     * to be given access to the Plan so that it can look up what machine
     * holds what Node.
     * @param nodes
     * @param p
     */
    public AlsoRunNodesAction(List<NodeUID> nodes, Plan p) {
        this.nodesToRun = nodes;
    }
    
    public List<NodeUID> getNodesToRun() {
    	return nodesToRun;
    }

    @Override
    public String toString() {
        return "Run these state machines as well: "+nodesToRun;
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitAlsoRunNodes(this, param);
	}
    
}
