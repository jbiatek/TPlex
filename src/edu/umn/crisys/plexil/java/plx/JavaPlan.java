package edu.umn.crisys.plexil.java.plx;


import java.util.LinkedList;
import java.util.List;

import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.world.ExternalWorld;

public abstract class JavaPlan {
    
    public static final int QUIESCENCE_TIMEOUT = 1000;
    public static boolean DEBUG = false;

    private ExternalWorld world;
    private LibraryInterface parent = new LibraryInterface();
    private boolean isLibrary = false;
    private boolean askedForCommitAfterMicro = false;
    private boolean askedForCommitAfterMacro = false;
    
    
    public boolean changeOccurred = true;
    public boolean endMacroStep = false;
    @SuppressWarnings("rawtypes")
    private List<Variable> commitVarsAfterMicroStep = new LinkedList<Variable>();
    @SuppressWarnings("rawtypes")
    private List<Variable> commitVarsAfterMacroStep = new LinkedList<Variable>();
    @SuppressWarnings("rawtypes")
    private List<VariableArray> commitArraysAfterMicroStep = new LinkedList<VariableArray>();
    @SuppressWarnings("rawtypes")
    private List<VariableArray> commitArraysAfterMacroStep = new LinkedList<VariableArray>();
    
    private List<SimpleCurrentNext<?>> commitSimpleAfterMicroStep = 
        new LinkedList<SimpleCurrentNext<?>>();
    
    private List<JavaPlan> commitLibraryAfterMicroStep = new LinkedList<JavaPlan>();
    private List<JavaPlan> commitLibraryAfterMacroStep = new LinkedList<JavaPlan>();
    
    public JavaPlan(ExternalWorld world) {
    	this.world = world;
    }
    
    public JavaPlan(LibraryInterface parentInterface) {
    	this.parent = parentInterface;
    	this.isLibrary = true;
    }
    
    public NodeOutcome runPlanToCompletion() {
        while ( ! world.stop() && getRootNodeOutcome().isUnknown()) {
            doMacroStep();
            world.waitForNextEvent();
        }
        return getRootNodeOutcome();
    }
    
    public abstract void doMicroStep();
    
    public abstract NodeOutcome getRootNodeOutcome();
    
    public abstract NodeState getRootNodeState();
    
    private void askForCommitAfterMicro() {
        if (askedForCommitAfterMicro) return;
        if (isLibrary) {
            parent.getParentPlan().commitAfterMicroStep(this);
        }
        askedForCommitAfterMicro = true; // Don't do it again until commit
    }
    
    private void askForCommitAfterMacro() {
        if (askedForCommitAfterMacro) return;
        if (isLibrary) {
            parent.getParentPlan().commitAfterMacroStep(this);
        }
        askedForCommitAfterMacro = true; // Don't do it again until commit
    }
    
    public void doMacroStep() {
        doMacroStepCount();
    }
    
    public int doMacroStepCount() {
        int counter = 0;
        changeOccurred = true;
        endMacroStep = false;
        // Begin macro and micro step
        while (changeOccurred && ! endMacroStep) { 
            if (DEBUG)
                System.out.println("----------------------- "+counter);
            
            // Do one micro step
            changeOccurred = false;
            doMicroStep();
            
            commitMicroStepVars();
            
            // Check for a timeout
            counter++;
            if (counter > QUIESCENCE_TIMEOUT) {
                throw new RuntimeException("Quiesence took more than "+
                        QUIESCENCE_TIMEOUT+" microsteps, timed out");
            }
        }
        // Debug message for end of macrostep
        if (DEBUG) {
            if (endMacroStep) {
                System.out.println("Premature end to macro step requested at microstep "+counter);
            } else {
                System.out.println("Quiescence reached in "+counter+" microsteps.");
            }
        }
        
        commitMacroStepVars();
        return counter;
        
    }

    @SuppressWarnings("rawtypes")
    public void commitMicroStepVars() {
        for (Variable v : commitVarsAfterMicroStep) {
            v.commit();
        }
        for (VariableArray a : commitArraysAfterMicroStep) {
            a.commit();
        }
        for (SimpleCurrentNext<?> s : commitSimpleAfterMicroStep) {
            s.commit();
        }
        for (JavaPlan lib : commitLibraryAfterMicroStep) {
            lib.commitMicroStepVars();
        }
        commitVarsAfterMicroStep.clear();
        commitArraysAfterMicroStep.clear();
        commitSimpleAfterMicroStep.clear();
        askedForCommitAfterMicro = false;
    }
    
    @SuppressWarnings("rawtypes")
    public void commitMacroStepVars() {
        for (Variable v : commitVarsAfterMacroStep) {
            v.commit();
        }
        for (VariableArray a : commitArraysAfterMacroStep) {
            a.commit();
        }
        for (JavaPlan lib : commitLibraryAfterMacroStep) {
            lib.commitMacroStepVars();
        }
        commitVarsAfterMacroStep.clear();
        commitArraysAfterMacroStep.clear();
        askedForCommitAfterMacro = false;
    }
    
    public ExternalWorld getWorld() {
    	if (isLibrary) {
    		// We are part of a larger plan, so we will use whatever they
    		// want us to use.
    		return parent.getWorld();
    	}
    	// We are the root plan, so we must have the actual one to use.
        return world;
    }
    
    public LibraryInterface getInterface() {
        return parent;
    }
    
    public void endMacroStep() {
        endMacroStep = true;
    }
    
    public void changeOccurred() {
        changeOccurred = true;
        
        if (isLibrary) {
            // Notify our parent that a change occurred too.
            parent.getParentPlan().changeOccurred();
        }
    }
    
    @SuppressWarnings("rawtypes")
    public void commitAfterMicroStep(Variable v) {
        askForCommitAfterMicro();
        commitVarsAfterMicroStep.add(v);
    }

    @SuppressWarnings("rawtypes")
    public void commitAfterMacroStep(Variable v) {
        askForCommitAfterMacro();
        commitVarsAfterMacroStep.add(v);
    }
    
    @SuppressWarnings("rawtypes")
    public void commitAfterMicroStep(VariableArray a) {
        askForCommitAfterMicro();
        commitArraysAfterMicroStep.add(a);
    }
    
    @SuppressWarnings("rawtypes")
    public void commitAfterMacroStep(VariableArray a) {
        askForCommitAfterMacro();
        commitArraysAfterMacroStep.add(a);
    }
    
    public void commitAfterMicroStep(JavaPlan lib) {
        askForCommitAfterMicro();
        commitLibraryAfterMicroStep.add(lib);
    }
    
    public void commitAfterMacroStep(JavaPlan lib) {
        askForCommitAfterMacro();
        commitLibraryAfterMacroStep.add(lib);
    }
    
    public void commitAfterMicroStep(SimpleCurrentNext<?> v) {
        askForCommitAfterMicro();
        commitSimpleAfterMicroStep.add(v);
    }
    
}
