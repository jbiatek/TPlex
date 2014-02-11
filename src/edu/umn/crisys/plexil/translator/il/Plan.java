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
import edu.umn.crisys.plexil.ast.core.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.LibraryInterface;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.translator.il.action.AssignAction;
import edu.umn.crisys.plexil.translator.il.action.CommandAction;
import edu.umn.crisys.plexil.translator.il.action.PlexilAction;
import edu.umn.crisys.plexil.translator.il.action.UpdateAction;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;
import edu.umn.crisys.plexil.translator.il.vars.NodeStateReference;
import edu.umn.crisys.plexil.translator.il.vars.NodeTimepointReference;
import edu.umn.crisys.util.Pair;

public class Plan {

    public static boolean PRUNE_TIMEPOINTS = true;
    
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
        
        // Eliminate any start or end timepoints that aren't even read
        if (PRUNE_TIMEPOINTS) {
            pruneTimepoints();
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
	
	private void pruneTimepoints() {
	    Set<ILExpression> safeList = new HashSet<ILExpression>();
	    
	    Filter<ILExpression> timeFilter = new Filter<ILExpression>() {

            @Override
            public boolean accept(ILExpression obj) {
                return obj instanceof NodeTimepointReference;
            }};
            
	    // Save any that are being read in a guard or action
	    // (assignment, command, or update could reference it)
	    for (NodeStateMachine sm : stateMachines) {
	        for (Transition t : sm.transitions) {
	            for (TransitionGuard g : t.guards) {
	                addAllMatchingInExpressionTo(g.getExpression(), safeList, timeFilter);
	            }
	            for (PlexilAction a : t.actions) {
	                pruneTimepointActionHelper(a, safeList);
	            }
	        }
	        for (State s : sm.states) {
	            for (PlexilAction a : s.entryActions) {
	                pruneTimepointActionHelper(a, safeList);
	            }
	            for (PlexilAction a : s.inActions) {
	                pruneTimepointActionHelper(a, safeList);
	            }
	        }
	    }
	    
	    // Okay, everything not on the list is being told to leave itself out.
	    for (IntermediateVariable v : variables) {
	        if (v instanceof NodeTimepointReference && ! safeList.contains(v) ) {
	            ((NodeTimepointReference) v).markAsUnused();
	        }
	    }
	}

	private void pruneTimepointActionHelper(PlexilAction a, Set<ILExpression> safeList) {
	    Filter<ILExpression> timeFilter = new Filter<ILExpression>() {

	        @Override
	        public boolean accept(ILExpression obj) {
	            return obj instanceof NodeTimepointReference;
	        }};
	        
        if (a instanceof AssignAction) {
            addAllMatchingInExpressionTo(((AssignAction) a).getRHS(), safeList, timeFilter);
        } else if (a instanceof CommandAction) {
            addAllMatchingInExpressionTo(((CommandAction) a).getArgs(), safeList, timeFilter);
        } else if (a instanceof UpdateAction) {
            for (Pair<String, ILExpression> p : ((UpdateAction) a).getUpdates()) {
                addAllMatchingInExpressionTo(p.second, safeList, timeFilter);
            }
        }

	}

	private static interface Filter<T> {
	    public boolean accept(T obj);
	}
	
	private void addAllMatchingInExpressionTo(List<ILExpression> es, Set<ILExpression> s, Filter<ILExpression> f) {
	    for (ILExpression e : es) {
	        addAllMatchingInExpressionTo(e, s, f);
	    }
	}
	
	private void addAllMatchingInExpressionTo(ILExpression e, Set<ILExpression> s, Filter<ILExpression> f) {
	    if (f.accept(e)) {
	        s.add(e);
	        return;
	    }
	    
	    if (e instanceof CompositeExpr) {
	        CompositeExpr comp = (CompositeExpr) e;
	        for (Expression arg : comp.getArguments()) {
	            addAllMatchingInExpressionTo((ILExpression) arg, s, f);
	        }    
	    }
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
