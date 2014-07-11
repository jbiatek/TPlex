package edu.umn.crisys.plexil.runtime.plx;

import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;

public class LibraryInterface {

    public void performAssignment(String varName, PValue value, int priority) {
        throw new RuntimeException("Empty interface");
    }
    
    public PValue getValue(String varName) {
        throw new RuntimeException("Empty interface");
    }
    
    public ExternalWorld getWorld() {
        return getParentPlan().getWorld();
    }
    
    public JavaPlan getParentPlan() {
        return null;
    }
    
    public NodeState evalParentState() {
        // In PLEXIL, root nodes behave as if their parent is Executing.
        return NodeState.EXECUTING;
    }
    
    public PBoolean evalAncestorInvariant() {
        // By default, tell them that everything is fine.
        return BooleanValue.get(true);
    }
    
    public PBoolean evalAncestorExit() {
        // By default, tell them no one is exiting.
        return BooleanValue.get(false);
    }
    
    public PBoolean evalAncestorEnd() {
        // By default, tell them no one is ending.
        return BooleanValue.get(false);
    }
}
