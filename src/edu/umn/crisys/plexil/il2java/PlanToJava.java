package edu.umn.crisys.plexil.il2java;

import java.util.Map;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.ILVarVisitor;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.LibraryInterface;
import edu.umn.crisys.plexil.java.plx.SimplePArray;
import edu.umn.crisys.plexil.java.plx.SimplePValue;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.test.java.PlanState;
import edu.umn.crisys.plexil.test.java.PlexilTestable;

public class PlanToJava {

	private PlanToJava() {}
	
	private static void addLibraryVarToClass(JDefinedClass clazz, LibraryVar lib, Map<String,String> idToClassName) {
        JCodeModel cm = clazz.owner();
        // We're actually adding the entire node, and creating the interface
        // for aliases, if any.
        JDefinedClass anonClass = cm.anonymousClass(cm.ref(LibraryInterface.class));
        
        // getWorld()
        anonClass.method(JMod.PUBLIC, cm.ref(JavaPlan.class), "getParentPlan")
            // http://stackoverflow.com/questions/56974/keyword-for-the-outer-class-from-an-anonymous-inner-class
            .body()._return(clazz.staticRef("this"));

        // evalParentState() needs to return our library node's state
        anonClass.method(JMod.PUBLIC, cm.ref(NodeState.class), "evalParentState")
            .body()._return(ILExprToJava.toJava(lib.getLibraryNodeState(), cm));
        
        // evalAncestorInvariant() returns our + ancestor's invariants
        JMethod evalInv = anonClass.method(JMod.PUBLIC, cm.ref(PBoolean.class), "evalAncestorInvariant");
        evalInv.body().decl(cm.ref(PBoolean.class), "temp");
        evalInv.body()._return(ILExprToJava.toJava(lib.getLibAndAncestorsInvariants(), cm));
        
        // evalAncestorEnd() returns our + ancestor's ends
        JMethod evalEnd = anonClass.method(JMod.PUBLIC, cm.ref(PBoolean.class), "evalAncestorEnd");
        evalEnd.body().decl(cm.ref(PBoolean.class), "temp");
        evalEnd.body()._return(ILExprToJava.toJava(lib.getLibOrAncestorsEnds(), cm));
        
        // evalAncestorExit() returns our + ancestor's exits
        JMethod evalExit = anonClass.method(JMod.PUBLIC, cm.ref(PBoolean.class), "evalAncestorExit");
        evalExit.body().decl(cm.ref(PBoolean.class), "temp");
        evalExit.body()._return(ILExprToJava.toJava(lib.getLibOrAncestorsExits(), cm));
        
        
        if ( ! lib.getAliases().isEmpty()) {
            // More work to handle aliases
            JMethod performAssignment = anonClass.method(JMod.PUBLIC, cm.VOID, "performAssignment");
            JVar varNamePA = performAssignment.param(cm.ref(String.class), "varName");
            JVar value = performAssignment.param(cm.ref(PValue.class), "value");
            JVar priority = performAssignment.param(cm.INT, "priority");

            JMethod getValue = anonClass.method(JMod.PUBLIC, cm.ref(PValue.class), "getValue");
            JVar varNameGV = getValue.param(cm.ref(String.class), "varName");

            JConditional condAssign = null;
            JConditional condGetter = null;

            for (String alias : lib.getAliases().keySet()) {

                // Do we need to make this assignable, or just readable?

                ILExpression target = lib.getAliases().get(alias);
                if (target.isAssignable()) {
                    // Need to add this to both methods
                    if (condAssign == null) {
                        condAssign = performAssignment.body()
                        ._if(varNamePA.invoke("equals").arg(JExpr.lit(alias)));
                    } else {
                        condAssign = condAssign
                        ._elseif(varNamePA.invoke("equals").arg(JExpr.lit(alias)));
                    }
                    ILVariable varToCommit = ActionToJava.addAssignment(condAssign._then(), target, value, cm);
                    if (varToCommit != null) {
                    	// We need to commit this variable after we assign it.
                    	condAssign._then().invoke("commitAfterMicroStep").arg(
                    			JExpr.ref(ILExprToJava.getFieldName(varToCommit)));
                    	condAssign._then().invoke("endMacroStep");
                    }
                }

                if (condGetter == null) {
                    condGetter = getValue.body()
                    ._if(varNameGV.invoke("equals").arg(JExpr.lit(alias)));
                } else {
                    condGetter = condGetter
                    ._elseif(varNameGV.invoke("equals").arg(JExpr.lit(alias)));
                }
                condGetter._then()._return(
                        ILExprToJava.toJava(lib.getAliases().get(alias), cm));

            }
            if (condAssign == null) {
            	// We never set one, so make the body just an exception throw.
            	performAssignment.body()._throw(JExpr._new(cm.ref(RuntimeException.class))
            			.arg(JExpr.lit("No variables were specified as writable")));
            } else {
            	condAssign._else()._throw(JExpr._new(cm.ref(RuntimeException.class))
            			.arg(JExpr.lit("I don't know where to assign for ").plus(varNamePA)));
            }
            // That shouldn't be a problem here, though, since there must have
            // been at least 1 alias.
            condGetter._else()._throw(JExpr._new(cm.ref(RuntimeException.class))
                    .arg(JExpr.lit("I don't know about a var named ").plus(varNameGV)));
        }

        String className = NameUtils.clean(lib.getLibraryPlexilID());
        if (idToClassName != null && idToClassName.containsKey(lib.getLibraryPlexilID())) {
        	className = idToClassName.get(lib.getLibraryPlexilID());
        }
        
        clazz.field(JMod.PRIVATE, clazz.owner().ref(JavaPlan.class), 
                ILExprToJava.getLibraryFieldName(lib.getNodeUID()),
                JExpr._new(cm.directClass(className))
                .arg(JExpr._new(anonClass))); 
    }
	
	
	public static JDefinedClass toJava(Plan p, JCodeModel cm, String pkg, final Map<String,String> idToClassName) {
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
        
        // JavaPlan has two constructors to override.
        JMethod basicConstructor = clazz.constructor(JMod.PUBLIC);
        basicConstructor.param(cm.ref(ExternalWorld.class), "world");
        basicConstructor.body().invoke("super").arg(JExpr.ref("world"));
        
        // If we're being used as a library, this one will be invoked. It contains
        // an interface for talking to our parent.
        JMethod libConstructor = clazz.constructor(JMod.PUBLIC);
        JVar inParent = libConstructor.param(cm.ref(LibraryInterface.class), "inParent");
        libConstructor.body().invoke("super").arg(inParent);
        
        // Variables! We need to add them to the class.
        for (ILVariable v : p.getVariables()) {
        	ILVarVisitor<JDefinedClass, Void> adder = new ILVarVisitor<JDefinedClass, Void>() {

				@Override
				public Void visitSimple(SimpleVar var, JDefinedClass clazz) {
			        JCodeModel cm = clazz.owner();
			        // Get SimplePValue<Whatever>
			        JClass varContainerClass = cm.ref(SimplePValue.class).narrow(var.getType().getTypeClass());

			        // Prepare the initial value. There should always be one, because
			        // SimpleVar creates one if there isn't. 
			        JExpression initExpr = ILExprToJava.toJava(var.getInitialValue(), cm);

			        
			        // Now we can create the field
			        // private SimplePValue<Whatever> varId_ = new SimplePValue<Whatever>(init, type);
			        clazz.field(JMod.PRIVATE, varContainerClass, ILExprToJava.getFieldName(var),
			                JExpr._new(varContainerClass)
			                	.arg(initExpr)
			                	.arg(ILExprToJava.plexilTypeAsJava(var.getType(), cm)));
			        
			        return null;
				}

				@Override
				public Void visitArray(ArrayVar array, JDefinedClass clazz) {
			        JCodeModel cm = clazz.owner();
			        
			        // Create JClass for SimplePArray<PBooleanOrPIntOrPWhatever>:
			        JClass parameterized = cm.ref(SimplePArray.class).narrow(
			        		array.getType().elementType().getTypeClass());
			        
			        // Create the initializing expression:
			        // new SimplePArray<Type>(type, numElements, T... init):
			        JInvocation init = JExpr._new(parameterized)
			        		.arg(ILExprToJava.plexilTypeAsJava(array.getType(), cm))
			        		.arg(JExpr.lit(array.getMaxSize()));
			        for (PValue item : array.getInitialValue()) {
			        	init.arg(ILExprToJava.toJava(item, cm));
			        }

			        // That's all the pieces! Let's make the field:
			        //JFieldVar jvar = 
			        clazz.field(JMod.PRIVATE, parameterized, ILExprToJava.getFieldName(array), init);
			        
			        return null;
				}

				@Override
				public Void visitLibrary(LibraryVar lib, JDefinedClass clazz) {
					addLibraryVarToClass(clazz, lib, idToClassName);
					return null;
				}
        		
        	};
        	v.accept(adder, clazz);
        }
        
        // Put each node state machine into the class.
        for (NodeStateMachine nsm : p.getMachines()) {
            StateMachineToJava.addStateMachineToClass(nsm, clazz);
        }
        
        // Also make the doMicroStep method, using our root machine:
        JMethod doMicroStep = clazz.method(JMod.PUBLIC, cm.VOID, "doMicroStep"); 
        StateMachineToJava.callStepFunction(p.root, doMicroStep.body());
        doMicroStep.body().invoke("notifyMicroStep");
        
        clazz.method(JMod.PUBLIC, cm.ref(NodeOutcome.class), "getRootNodeOutcome").body()
            ._return(ILExprToJava.toJava(p.rootOutcome, cm));
        
        clazz.method(JMod.PUBLIC, cm.ref(NodeState.class), "getRootNodeState").body()
            ._return(ILExprToJava.toJava(p.rootState, cm));
        
	    return clazz;
	}
	
	

    public static void addGetSnapshotMethod(Plan ilPlan, NodeToIL translator,
            JDefinedClass javaCode) {
        
        javaCode._implements(PlexilTestable.class);
        
        JCodeModel cm = javaCode.owner();
        JMethod m = javaCode.method(JMod.PUBLIC, cm.ref(PlanState.class), "getSnapshot");
        
        // For every Plexil node, we need to grab its variables and put them
        // into a PlanState node.
        
        JVar root = addSnapshotInfo(ilPlan, translator, null, m.body(), cm);
        
        m.body()._return(root);
        
    }
    
    /**
     * Add Java code to the given JBlock that creates a local PlanState 
     * variable and fills it in with the correct variable and child information. 
     * @param translator The node to capture the info of
     * @param parent The parent PlanState var, or null if it's the root.
     * @param b The block of code to add to.
     * @param cm
     * @return the local variable that was made.
     */
    private static JVar addSnapshotInfo(Plan ilPlan, NodeToIL translator, JVar parent, JBlock b,
            JCodeModel cm) {
        JInvocation planStateInit = JExpr._new(cm.ref(PlanState.class))
            .arg(translator.getUID().getShortName());
        if (parent != null) {
            planStateInit.arg(parent);
        } 
        JVar ps = b.decl(cm.ref(PlanState.class), translator.getUID().toCleanString(), planStateInit);
        
        // State variables aren't IL variables, so add that specifically
        b.invoke(ps, "addVariable").arg(".state").arg(ILExprToJava.toJava(translator.getState(), cm));
        
        for (String varName : translator.getAllVariables()) {
            ILVariable v = translator.getVariable(varName);
            if ( ! ilPlan.getVariables().contains(v)) {
            	// This variable didn't make it into the final version.
            	continue;
            }
            
            
            String name = v.getName();
            
//            if (name.startsWith(".")) {
//            	// Internal variables are named "ShortName.command_handle", etc.
//            	name = v.getNodeUID().getShortName()+name;
//            }
            
            b.invoke(ps, "addVariable").arg(name).arg(ILExprToJava.toJava(v, cm));
        }
        
        for (NodeToIL child : translator.getChildren()) {
            JVar childVar = addSnapshotInfo(ilPlan, child, ps, b, cm);
            b.invoke(ps, "addChild").arg(childVar);
        }
        
        if (translator.hasLibraryHandle()) {
        	// We need direct access to the field here.
            b.invoke(ps, "addChild").arg(
                    JExpr.invoke(JExpr.cast(cm.ref(PlexilTestable.class), 
                    JExpr.ref(ILExprToJava.getLibraryFieldName(translator.getUID()))), "getSnapshot"));

        }
        
        return ps;
        
    }

}
