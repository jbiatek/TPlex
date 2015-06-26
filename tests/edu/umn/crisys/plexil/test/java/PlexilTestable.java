package edu.umn.crisys.plexil.test.java;

import edu.umn.crisys.plexil.runtime.values.NodeOutcome;

public interface PlexilTestable {
    
    public PlanState getSnapshot();

    public void doMacroStep();
    
    public NodeOutcome getRootNodeOutcome();
    
    public void commitMicroStepVars();
}
