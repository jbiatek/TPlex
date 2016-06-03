package edu.umn.crisys.plexil.runtime.plx;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;

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
    	if (isLibrary) {
    		parent.getParentPlan().notifyPrematureEnd();
    		return;
    	}
    	for (JavaPlanObserver obs : observers) {
    		obs.prematureEndOfMacroStep(this);
    	}
    }

    private void notifyQuiescence() {
    	if (isLibrary) {
    		parent.getParentPlan().notifyQuiescence();
    		return;
    	}
    	for (JavaPlanObserver obs : observers) {
    		obs.quiescenceReached(this);
    	}
    }
    
    private void notifyPlanEnded() {
    	for (JavaPlanObserver obs : observers) {
    		obs.endOfExecution(this);
    	}
    }
    
    private void notifyBeforeExecution() {
    	for (JavaPlanObserver obs : observers) {
    		obs.beforePlanExecution(this);
    	}
    }
    
    public void notifyBeforeMicroStep() {
    	for (JavaPlanObserver obs : observers) {
    		obs.beforeMicroStepRuns(this);
    	}
    }
    
    public void notifyBeforeMacroStep() {
    	for (JavaPlanObserver obs : observers) {
    		obs.beforeMacroStepRuns(this);
    	}
    }

    
    /**
     * Notify listeners that a micro step has ended. Only call this inside
     * of an implementation of doMicroStep() please.
     */
    public void notifyMicroStepBeforeCommit() {
    	if (isLibrary) {
    		parent.getParentPlan().notifyMicroStepBeforeCommit();
    		return;
    	}
    	for (JavaPlanObserver obs : observers) {
    		obs.endOfMicroStepBeforeCommit(this);
    	}
    }
    
    public void notifyMicroStepAfterCommit() {
    	if (isLibrary) {
    		parent.getParentPlan().notifyMicroStepAfterCommit();
    		return;
    	}
    	for (JavaPlanObserver obs : observers) {
    		obs.endOfMicroStepAfterCommit(this);
    	}
    }

    
    public int runPlanToCompletion() {
    	return runPlanToCompletion(-1);
    }
    
    /**
     * Run the plan to completion, or stop if more than maxMacroSteps occur.
     * If this number is negative, there will be no limit. 
     * 
     * @param maxMacroSteps
     * @return the number of steps actually taken. 
     */
    public int runPlanToCompletion(int maxMacroSteps) {
    	if ( ! observers.contains(world)) {
    		addObserver(world);
    	}
    	int steps = 0;
    	notifyBeforeExecution();
        while ( ! world.stop() && getRootNodeState() != NodeState.FINISHED) {
        	if (DEBUG) {
        		System.out.println("***************************** Macro step "+(steps+1));
        	}
            doMacroStep();
            steps++;
            if (maxMacroSteps > 0 && steps >= maxMacroSteps) {
            	break;
            }
        }
        notifyPlanEnded();
        return steps;

    }
    
    public abstract void doMicroStep();
    
    public abstract NodeOutcome getRootNodeOutcome();
    
    public abstract NodeState getRootNodeState();
    
    public abstract PlanState getSnapshot();
    
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
        int counter = 0;
        changeOccurred = true;
        endMacroStep = false;
        // Begin macro and micro step
        notifyBeforeMacroStep();
        while (changeOccurred && ! endMacroStep) { 
            if (DEBUG)
                System.out.println("----------------------- "+counter);
            
            // Do one micro step
            changeOccurred = false;
            notifyBeforeMicroStep();
            doMicroStep();
            
            // Commit variables
            notifyMicroStepBeforeCommit();
            commitMicroStepVars();
            notifyMicroStepAfterCommit();
            
            // Check for a timeout
            counter++;
            if (counter > QUIESCENCE_TIMEOUT) {
                throw new QuiescenceLimitExceeded(QUIESCENCE_TIMEOUT);
            }
        }
        // Debug message for end of macrostep
        if (endMacroStep) {
        	if (DEBUG) System.out.println("Premature end to macro step requested at microstep "+counter);
        	notifyPrematureEnd();
        } else {
        	if (DEBUG) System.out.println("Quiescence reached in "+counter+" microsteps.");
        	notifyQuiescence();
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
