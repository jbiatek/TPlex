package edu.umn.crisys.plexil.il2java;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.LibraryInterface;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.translator.il.Plan;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;

public class PlanToJava {

	private PlanToJava() {}
	
	public static JDefinedClass toJava(Plan p, JCodeModel cm, String pkg, boolean library) {
	    String realPkg = pkg.equals("") ? "" : pkg+".";
	    // Try to create a class for this Plan.
	    JDefinedClass clazz;
        try {
            String name = NameUtils.clean(p.planName);
            clazz = cm._class(realPkg + name);
        } catch (JClassAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        // Of course, we extend the JavaPlan class. 
        clazz._extends(cm.ref(JavaPlan.class));
        
        if (library) {
            // We're a library. Need to make some additions.
            JMethod constructor = clazz.constructor(JMod.PUBLIC);
            JVar inParent = constructor.param(cm.ref(LibraryInterface.class), "inParent");
            constructor.body().invoke("setInterface").arg(inParent);
            
            clazz.method(JMod.PUBLIC, cm.ref(ExternalWorld.class), "getWorld")
                .body()._return(JExpr.invoke("getInterface").invoke("getWorld"));
        }
        
        // Variables! They need to add themselves to the class.
        for (IntermediateVariable v : p.getVariables()) {
            v.addVarToClass(clazz);
        }
        
        // Put each node state machine into the class.
        for (NodeStateMachine nsm : p.getMachines()) {
            StateMachineToJava.addStateMachineToClass(nsm, clazz);
        }
        
        // Also make the doMicroStep method, using our root machine:
        JMethod doMicroStep = clazz.method(JMod.PUBLIC, cm.VOID, "doMicroStep"); 
        StateMachineToJava.callStepFunction(p.root, doMicroStep.body());
        
        clazz.method(JMod.PUBLIC, cm.ref(NodeOutcome.class), "getRootNodeOutcome").body()
            ._return(p.rootOutcome.rhs(cm));
        
        clazz.method(JMod.PUBLIC, cm.ref(NodeState.class), "getRootNodeState").body()
            ._return(p.rootState.rhs(cm));
        
	    return clazz;
	}
}
