/**
 * 
 */
package edu.umn.crisys.plexil.il.statemachine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.java.values.NodeState;

/**
 * @author Whalen
 * 
 * The State class describes a state in the generalized
 * Plexil state machine used by the intermediate language.
 *
 */
public class State {
    public Map<NodeUID, NodeState> tags = new HashMap<NodeUID, NodeState>(); 
	public List<PlexilAction> entryActions = new LinkedList<PlexilAction>(); 
	public List<PlexilAction> inActions = new LinkedList<PlexilAction>();
    public NodeStateMachine stateMachine;
	
	public State(NodeUID nodeId, NodeState ns, NodeStateMachine nsm) {
		tags.put(nodeId, ns);
		stateMachine = nsm;
		nsm.addState(this);
	}
	
	@Override
	public String toString() {
	    return "State #"+getIndex();
	}
	
	public int getIndex() {
        return stateMachine.indexOf(this);
    }

    public void addEntryAction(PlexilAction a) {
		entryActions.add(a);
	}
	
	public void addInAction(PlexilAction a) {
		inActions.add(a);
	}
	
}
