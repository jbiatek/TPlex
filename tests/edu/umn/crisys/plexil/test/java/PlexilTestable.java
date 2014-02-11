package edu.umn.crisys.plexil.test.java;

import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.world.ExternalWorld;

public interface PlexilTestable {
    
    public PlanState getSnapshot();

    public void setWorld(ExternalWorld world);

    public void doMacroStep();
    
    public NodeOutcome getRootNodeOutcome();
}
