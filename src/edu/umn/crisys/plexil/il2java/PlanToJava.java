package edu.umn.crisys.plexil.il2java;

import java.util.Map;
import java.util.Optional;

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
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.ILVarVisitor;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.LibraryInterface;
import edu.umn.crisys.plexil.runtime.plx.SimplePArray;
import edu.umn.crisys.plexil.runtime.plx.SimplePValue;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import edu.umn.crisys.plexil.test.java.PlanState;
import edu.umn.crisys.plexil.test.java.PlexilTestable;

public class PlanToJava {

	private PlanToJava() {}
	
	private static void addLibEvalMethod(Class<?> returnType, String name, ILExpression ret, JDefinedClass libClass) {
		JMethod evalSomething = libClass.method(JMod.PUBLIC, libClass.owner().ref(returnType), name);
		// The eval methods can contain complex expressions, but since we're not
		// in a biased environment, we need to add a temp for short circuiting.
		ILExprToJava.insertShortCircuitHack(evalSomething.body(), libClass.owner());
		evalSomething.body()._return(ILExprToJava.toJava(ret, libClass.owner()));
	}
	
	private static void addAliasesToLibraryVar(JDefinedClass libClass, LibraryVar ilLib) {
		if (ilLib.getAliases().isEmpty()) return;
		
		JCodeModel cm = libClass.owner();
		
		// There are two methods that deal with aliases: one to just get the
		// value and one to perform assignments. 
        JMethod performAssignment = libClass.method(JMod.PUBLIC, cm.VOID, "performAssignment");
        JVar varNamePA = performAssignment.param(cm.ref(String.class), "varName");
        JVar value = performAssignment.param(cm.ref(PValue.class), "value");
        performAssignment.param(cm.INT, "priority"); // TODO: Is priority really used?

        JMethod getValue = libClass.method(JMod.PUBLIC, cm.ref(PValue.class), "getValue");
        JVar varNameGV = getValue.param(cm.ref(String.class), "varName");

        // There's a chain of if/else statements for both the getValue and
        // performAssignment methods. 
        JConditional condAssign = null;
        JConditional condGetter = null;

        // We're just doing both methods at once, since we have to go through
        // the whole set of aliases for each one anyway. 
        for (String alias : ilLib.getAliases().keySet()) {

            // First up, performAssignment. 

            ILExpression target = ilLib.getAliases().get(alias);
            if (target.isAssignable()) {
                // Add this one to the assignable list. 
                if (condAssign == null) {
                	// First one, use an if statement
                    condAssign = performAssignment.body()._if(
                    		varNamePA.invoke("equals").arg(JExpr.lit(alias)));
                } else {
                	// Not first, chain it on with an else if statement
                    condAssign = condAssign._elseif(
                    		varNamePA.invoke("equals").arg(JExpr.lit(alias)));
                }
                // Now we have a conditional, fill its body with the assignment
                Optional<ILVariable> varToCommit = ActionToJava.addAssignment(condAssign._then(), target, value, cm);
                if (varToCommit.isPresent()) {
                	// We need to commit this variable after we assign it.
                	condAssign._then().invoke("commitAfterMicroStep").arg(
                			JExpr.ref(ILExprToJava.getFieldName(varToCommit.get())));
                	condAssign._then().invoke("endMacroStep");
                }
            }

            // All aliases are readable, so let's do the same thing with the 
            // getter method. 
            if (condGetter == null) {
            	// First one, so use an if statement
                condGetter = getValue.body()._if(
                		varNameGV.invoke("equals").arg(JExpr.lit(alias)));
            } else {
            	// Not first, so chain it and use an else if
                condGetter = condGetter._elseif(
                		varNameGV.invoke("equals").arg(JExpr.lit(alias)));
            }
            // Fill the body with a return statement.
            condGetter._then()._return(
                    ILExprToJava.toJava(ilLib.getAliases().get(alias), cm));

        }
        // We've been through all aliases now. What if we didn't use condAssign
        // or condGetter? 
        if (condAssign == null) {
        	// Nothing was assignable, but this method is being asked to assign.
        	// We can just throw a descriptive expression. 
        	performAssignment.body()._throw(JExpr._new(cm.ref(RuntimeException.class))
        			.arg(JExpr.lit("No variables were specified as writable")));
        } else {
        	// We had some assignments. Let's cap off the if/else chain with
        	// a final else, handling the case where they asked for a name
        	// that we don't know about. 
        	condAssign._else()._throw(JExpr._new(cm.ref(RuntimeException.class))
        			.arg(JExpr.lit("I don't know where to assign for ").plus(varNamePA)));
        }
        // We can assume the getter isn't null, because there should
        // have been at least 1 alias. We just need to handle the else case,
        // just like above. 
        if (condGetter != null) {
        	condGetter._else()._throw(JExpr._new(cm.ref(RuntimeException.class))
        			.arg(JExpr.lit("I don't know about a var named ").plus(varNameGV)));
        } else {
        	throw new RuntimeException("There were no aliases?!");
        }

	}
	
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
        addLibEvalMethod(PBoolean.class, "evalAncestorInvariant", lib.getLibAndAncestorsInvariants(), anonClass);
        
        // evalAncestorEnd() returns our + ancestor's ends
        addLibEvalMethod(PBoolean.class, "evalAncestorEnd", lib.getLibOrAncestorsEnds(), anonClass);
        
        // evalAncestorExit() returns our + ancestor's exits
        addLibEvalMethod(PBoolean.class, "evalAncestorExit", lib.getLibOrAncestorsExits(), anonClass);
        
        // More work to handle aliases, if any
        addAliasesToLibraryVar(anonClass, lib);

        String className = NameUtils.clean(lib.getLibraryPlexilID());
        if (idToClassName != null && idToClassName.containsKey(lib.getLibraryPlexilID())) {
        	className = idToClassName.get(lib.getLibraryPlexilID());
        }
        
        // Finally, add the library field to the main class
        clazz.field(JMod.PRIVATE, clazz.owner().ref(JavaPlan.class), 
                ILExprToJava.getLibraryFieldName(lib.getNodeUID()),
                JExpr._new(cm.directClass(className))
                .arg(JExpr._new(anonClass))); 
    }
	
	private static void addVariable(ILVariable v, JDefinedClass clazz, Map<String,String> idToClassName) {
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
	
	public static JDefinedClass toJava(Plan p, JCodeModel cm, String pkg, 
			final Map<String,String> idToClassName) {
	    // Try to create a class for this Plan.
		String fqcn = fullyQualifyName(pkg, NameUtils.clean(p.getPlanName()));
	    JDefinedClass clazz = createJavaPlanClass(fqcn, !p.isTopLevelPlan(), cm);
        
        // Variables! We need to add them to the class.
	    for (ILVariable v : p.getVariables()) {
	    	addVariable(v, clazz, idToClassName);
	    }
	    
        // Put each node state machine into the class.
        for (NodeStateMachine nsm : p.getMachines()) {
            StateMachineToJava.addStateMachineToClass(nsm, clazz);
        }
        
        // Also make the abstract methods that JavaPlan uses:
        implementAbstractMethods(clazz, p);
        
	    return clazz;
	}
	
	private static void implementAbstractMethods(JDefinedClass clazz, Plan p) {
		JCodeModel cm = clazz.owner();
		
        JMethod doMicroStep = clazz.method(JMod.PUBLIC, cm.VOID, "doMicroStep"); 
        StateMachineToJava.callStepFunction(p.getRootMachine(), doMicroStep.body());
        
        clazz.method(JMod.PUBLIC, cm.ref(NodeOutcome.class), "getRootNodeOutcome").body()
            ._return(ILExprToJava.toJava(p.getRootNodeOutcome(), cm));
        
        clazz.method(JMod.PUBLIC, cm.ref(NodeState.class), "getRootNodeState").body()
            ._return(ILExprToJava.toJava(p.getRootNodeState(), cm));
	}

    private static String fullyQualifyName(String pkg, String className) {
		String realPkg = pkg.equals("") ? "" : pkg+".";
		return realPkg + className;
	}

	/**
	 * @param fullyQualifiedName
	 * @param cm
	 * @return an empty JavaPlan class
	 */
	private static JDefinedClass createJavaPlanClass(String fullyQualifiedName, boolean couldBeLibrary, JCodeModel cm) {
	    JDefinedClass clazz;
	    try {
	        clazz = cm._class(fullyQualifiedName);
	    } catch (JClassAlreadyExistsException e) {
	        throw new RuntimeException(e);
	    }
	    // Of course, we extend the JavaPlan class. 
	    clazz._extends(cm.ref(JavaPlan.class));
	    
	    // There's a simple basic constructor
	    JMethod basicConstructor = clazz.constructor(JMod.PUBLIC);
	    basicConstructor.param(clazz.owner().ref(ExternalWorld.class), "world");
	    basicConstructor.body().invoke("super").arg(JExpr.ref("world"));
	    
	    // And there's also a library constructor
	    JMethod libConstructor = clazz.constructor(JMod.PUBLIC);
	    JVar inParent = libConstructor.param(cm.ref(LibraryInterface.class), "inParent");
	    if (couldBeLibrary) {
	    	// The "real" constructor, passing info to JavaPlan.class
	    	libConstructor.body().invoke("super").arg(inParent);
	    } else {
	    	// A stub constructor which just throws an exception
	    	libConstructor.body().invoke("super").arg(JExpr.cast(cm.ref(LibraryInterface.class), JExpr._null()));
	    	libConstructor.body()._throw(JExpr._new(cm.ref(IllegalArgumentException.class))
	    			.arg(JExpr.lit(
	    					"This PLEXIL file didn't look like a library, so library support isn't"
	    					+ " included. Please either add an interface to the root node, or "
	    					+ "use the \"--libs\" option when translating.")));
	    }
	    
	    // Those are the basics!
	    return clazz;
	}
	
	

	public static void addGetSnapshotMethod(Plan ilPlan, JDefinedClass javaCode) {
        
        javaCode._implements(PlexilTestable.class);
        
        JCodeModel cm = javaCode.owner();
        JMethod m = javaCode.method(JMod.PUBLIC, cm.ref(PlanState.class), "getSnapshot");
        
    	// Some variables might have been removed along the way due to 
    	// optimizations and whatnot, so make sure that those are removed first
    	ilPlan.getOriginalHierarchy().removeDeletedVariables(ilPlan);
        
        // For every Plexil node, we need to grab its variables and put them
        // into a PlanState node.
        JVar root = addSnapshotInfo(ilPlan, ilPlan.getOriginalHierarchy(), 
        		Optional.empty(), m.body(), cm);
        
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
    private static JVar addSnapshotInfo(Plan ilPlan, OriginalHierarchy node, 
    		Optional<JVar> parent, JBlock b, JCodeModel cm) {
        JInvocation planStateInit = JExpr._new(cm.ref(PlanState.class))
            .arg(node.getUID().getShortName());
        parent.ifPresent(var -> planStateInit.arg(var));
        JVar ps = b.decl(cm.ref(PlanState.class), node.getUID().toCleanString(), planStateInit);
        
        for (String varName : node.getVariables().keySet()) {
            ILExpression v = node.getVariables().get(varName);
            b.invoke(ps, "addVariable").arg(varName).arg(ILExprToJava.toJava(v, cm));
        }
        
        for (OriginalHierarchy child : node.getChildren()) {
            JVar childVar = addSnapshotInfo(ilPlan, child, Optional.of(ps), b, cm);
            b.invoke(ps, "addChild").arg(childVar);
        }
        
        if (node.getLibraryChild().isPresent()) {
        	// We need direct access to the field here.
            b.invoke(ps, "addChild").arg(
                    JExpr.invoke(JExpr.cast(cm.ref(PlexilTestable.class), 
                    JExpr.ref(ILExprToJava.getLibraryFieldName(node.getUID()))), "getSnapshot"));

        }
        
        return ps;
        
    }

}
