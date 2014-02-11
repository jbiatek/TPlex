/**
 * @author Whalen
 *
 */

package edu.umn.crisys.plexil.translator.il;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.il.optimize.PruneUnusedTimepoints;
import edu.umn.crisys.plexil.il.optimize.RemoveDeadTransitions;
import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.LibraryInterface;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;
import edu.umn.crisys.plexil.translator.il.vars.NodeStateReference;

public class Plan {

    public static boolean PRUNE_TIMEPOINTS = false;
    public static boolean REMOVE_IMPOSSIBLE_TRANSITIONS = false;

    
	private List<NodeStateMachine> stateMachines = new LinkedList<NodeStateMachine>(); 
	private Set<IntermediateVariable> variables = new HashSet<IntermediateVariable>();
	
    public Set<String> globalVarNameList = new HashSet<String>();
	
	private NodeStateMachine root;
	private IntermediateVariable rootOutcome;
	private NodeStateReference rootState;
    private String planName;
    public Plan(String planName) {
	    this.planName = planName;
	}
    
    public List<NodeStateMachine> getMachines() {
    	return stateMachines;
    }
    
    public Set<IntermediateVariable> getVariables() {
    	return variables;
    }
	
	public JDefinedClass toJava(JCodeModel cm, String pkg, boolean library) {
	    String realPkg = pkg.equals("") ? "" : pkg+".";
	    JDefinedClass clazz;
        try {
            String name = NameUtils.clean(planName);
            clazz = cm._class(realPkg + name);
        } catch (JClassAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        
        clazz._extends(cm.ref(JavaPlan.class));
        

	    if (REMOVE_IMPOSSIBLE_TRANSITIONS) {
	        RemoveDeadTransitions.optimize(this);
	    }
        
        // Eliminate any start or end timepoints that aren't even read
        if (PRUNE_TIMEPOINTS) {
            PruneUnusedTimepoints.optimize(this);
        }

        if (library) {
            // We're a library. Need to make some additions.
            JMethod constructor = clazz.constructor(JMod.PUBLIC);
            JVar inParent = constructor.param(cm.ref(LibraryInterface.class), "inParent");
            constructor.body().invoke("setInterface").arg(inParent);
            
            clazz.method(JMod.PUBLIC, cm.ref(ExternalWorld.class), "getWorld")
                .body()._return(JExpr.invoke("getInterface").invoke("getWorld"));
        }
        
        // Variables! They need to add themselves to the class.
        for (IntermediateVariable v : variables) {
            v.addVarToClass(clazz);
        }
        
        // Give each node state machine a pass over our new class
        for (NodeStateMachine nsm : stateMachines) {
            nsm.toJava(clazz);
        }
        
        // Also make the doMicroStep method, using our root machine:
        clazz.method(JMod.PUBLIC, cm.VOID, "doMicroStep").body()
            .invoke(root.getStepMethodName());
        
        clazz.method(JMod.PUBLIC, cm.ref(NodeOutcome.class), "getRootNodeOutcome").body()
            ._return(rootOutcome.rhs(cm));
        
        clazz.method(JMod.PUBLIC, cm.ref(NodeState.class), "getRootNodeState").body()
            ._return(rootState.rhs(cm));
        
	    return clazz;
	}
	

	public void addVariable(IntermediateVariable var) {
	    variables.add(var);
	}
	
	public void addVariables(List<? extends IntermediateVariable> vars) {
	    variables.addAll(vars);
	}
	
	public void addStateMachine(NodeStateMachine nsm) {
	    stateMachines.add(nsm);
	}
	
	public void setRoot(NodeStateMachine nsm, NodeStateReference state, IntermediateVariable outcome) {
	    root = nsm;
	    rootOutcome = outcome;
	    rootState = state;
	}
	
	public NodeStateMachine findMachineForNode(NodeUID nodeId) {
	    for (NodeStateMachine nsm : stateMachines) {
	        if (nsm.getNodeIds().contains(nodeId)) {
	            return nsm;
	        }
	    }
	    throw new RuntimeException("Node ID "+nodeId+" isn't claimed by any state machine.");
	}
	
}
