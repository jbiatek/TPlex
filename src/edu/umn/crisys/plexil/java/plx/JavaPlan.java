package edu.umn.crisys.plexil.java.plx;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.world.ExternalWorld;

public abstract class JavaPlan {
    
    public static final int QUIESCENCE_TIMEOUT = 1000;
    public static boolean DEBUG = false;

    private ExternalWorld world;
    private List<JavaPlanObserver> observers = new ArrayList<JavaPlanObserver>();
    private LibraryInterface parent = new LibraryInterface();
    private boolean isLibrary = false;
    private boolean askedForCommitAfterMicro = false;
    
    
    public boolean changeOccurred = true;
    public boolean endMacroStep = false;
    private List<SimplePArray<?>> commitArraysAfterMicroStep = new LinkedList<SimplePArray<?>>();
    private List<SimpleCurrentNext<?>> commitSimpleAfterMicroStep = 
        new LinkedList<SimpleCurrentNext<?>>();
    private List<JavaPlan> commitLibraryAfterMicroStep = new LinkedList<JavaPlan>();
    
    public JavaPlan(ExternalWorld world) {
    	this.world = world;
    }
    
    public JavaPlan(LibraryInterface parentInterface) {
    	this.parent = parentInterface;
    	this.isLibrary = true;
    }
    
    public void setWorld(ExternalWorld newWorld) {
    	this.world = newWorld;
    }
    
    public void addObserver(JavaPlanObserver obs) {
    	observers.add(obs);
    }
    
    private void notifyPrematureEnd() {
    	world.prematureEndOfMacroStep(this);
    	for (JavaPlanObserver obs : observers) {
    		obs.prematureEndOfMacroStep(this);
    	}
    }

    private void notifyQuiescence() {
    	world.quiescenceReached(this);
    	for (JavaPlanObserver obs : observers) {
    		obs.quiescenceReached(this);
    	}
    }
    
    /**
     * Notify listeners that a micro step has ended. Only call this inside
     * of an implementation of doMicroStep() please.
     */
    public void notifyMicroStep() {
    	world.endOfMicroStep(this);
    	for (JavaPlanObserver obs : observers) {
    		obs.endOfMicroStep(this);
    	}
    }

    
    public NodeOutcome runPlanToCompletion() {
        while ( ! world.stop() && getRootNodeOutcome().isUnknown()) {
            doMacroStep();
            if (endMacroStep) {
            	notifyPrematureEnd();
            } else {
            	notifyQuiescence();
            }
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
    
    public void doMacroStep() {
        doMacroStepCount();
    }
    
    public int doMacroStepCount() {
    	if (DEBUG) {
    		System.out.println("***************************** New macro step");
    	}
    	
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
        return counter;
        
    }

    @SuppressWarnings("rawtypes")
    public void commitMicroStepVars() {
        for (SimplePArray a : commitArraysAfterMicroStep) {
            a.commit();
        }
        for (SimpleCurrentNext<?> s : commitSimpleAfterMicroStep) {
            s.commit();
        }
        for (JavaPlan lib : commitLibraryAfterMicroStep) {
            lib.commitMicroStepVars();
        }
        commitArraysAfterMicroStep.clear();
        commitSimpleAfterMicroStep.clear();
        askedForCommitAfterMicro = false;
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
    public void commitAfterMicroStep(SimplePArray a) {
        askForCommitAfterMicro();
        commitArraysAfterMicroStep.add(a);
    }
    
    public void commitAfterMicroStep(JavaPlan lib) {
        askForCommitAfterMicro();
        commitLibraryAfterMicroStep.add(lib);
    }
    
    public void commitAfterMicroStep(SimpleCurrentNext<?> v) {
        askForCommitAfterMicro();
        commitSimpleAfterMicroStep.add(v);
    }
    
}
