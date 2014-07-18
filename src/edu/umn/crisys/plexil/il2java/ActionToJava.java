package edu.umn.crisys.plexil.il2java;

import java.util.HashSet;
import java.util.Set;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AlsoRunNodesAction;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.CompositeAction;
import edu.umn.crisys.plexil.il.action.EndMacroStep;
import edu.umn.crisys.plexil.il.action.ILActionVisitor;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.action.ResetNodeAction;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.expr.AliasExpr;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.ILVarVisitor;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.runtime.plx.CommandHandle;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.UpdateHandle;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.util.Pair;

public class ActionToJava implements ILActionVisitor<JBlock, Void>{
	
	private JCodeModel cm;
	private Plan ilPlan;
	
	public ActionToJava(JCodeModel cm, Plan ilPlan) {
		this.cm = cm;
		this.ilPlan = ilPlan;
	}
	
	@Override
	public Void visitAlsoRunNodes(AlsoRunNodesAction run, JBlock block) {
        Set<NodeStateMachine> machinesToRun = new HashSet<NodeStateMachine>();
        for (NodeUID child : run.getNodesToRun()) {
            machinesToRun.add(ilPlan.findMachineForNode(child));
        }
        
        for (NodeStateMachine m : machinesToRun) {
            StateMachineToJava.callStepFunction(m, block);
        }
		return null;
	}
	
	@Override
	public Void visitAssign(AssignAction assign, JBlock block) {
		// Debug statement
		if ( ! assign.getLHS().toString().startsWith(".") 
				&& StateMachineToJava.DEBUG_STATEMENTS) {
			block._if(cm.ref(JavaPlan.class).staticRef("DEBUG")) 
					._then().add(
						cm.ref(System.class).staticRef("out").invoke("println")
							.arg(JExpr.lit(assign.getLHS().toString()
									+": New value set to ")
									.plus(ILExprToJava.toJava(assign.getRHS(), cm))
									));
		}
								
		ILVariable varToCommit = addAssignment(block, assign.getLHS(), 
        		ILExprToJava.toJava(assign.getRHS(), cm), cm);
		if (varToCommit != null) {
			block.invoke("commitAfterMicroStep").arg(
					JExpr.ref(ILExprToJava.getFieldName(varToCommit)));
		}
		
		
        
		return null;
	}

    /**
     * Add an assignment to the block. The right hand side can be any Java
     * expression, technically, but of course it won't compile if you do 
     * something silly. 
     * 
     * The left hand side must be assignable. You'll probably also want to 
     * commit the variable afterward. This method will return the ILVariable
     * that was used for the assignment, if any, which you can then 
     * perform the commit on. 
     * 
     * @param block
     * @param lhs
     * @param rhs
     * @param cm
     */
    public static ILVariable addAssignment(JBlock block, ILExpression lhs, JExpression rhs, JCodeModel cm) {
    	if (lhs.isAssignable()) {
    		if (lhs instanceof SimpleVar) {
    			block.invoke(JExpr.ref(ILExprToJava.getFieldName((SimpleVar)lhs)), "setNext")
    				.arg(rhs);
    			return (SimpleVar)lhs;
    		} else if (lhs instanceof ArrayVar) {
    			// Must be a whole array assignment.
    			block.invoke(JExpr.ref(ILExprToJava.getFieldName((ArrayVar) lhs)), 
    					"arrayAssign").arg(rhs);
    			return (ArrayVar) lhs;
    		} else if (lhs instanceof ArrayIndexExpr) {
    			// Just an index. 
    			ArrayIndexExpr arrayIndex = (ArrayIndexExpr) lhs;
    			ArrayVar array = (ArrayVar) arrayIndex.getArray();
    			ILExpression index = (ILExpression) arrayIndex.getIndex();
    			
    			block.invoke(JExpr.ref(ILExprToJava.getFieldName(array)), "indexAssign")
    				.arg(ILExprToJava.toJava(index, cm)).arg(rhs);
    			
    			return array;
    		} else if (lhs instanceof AliasExpr) {
    			AliasExpr alias = (AliasExpr) lhs;
    			// Pass this through the interface for assignment.
    	        block.add( JExpr.invoke("getInterface").invoke("performAssignment")
    	        		.arg(alias.getName()).arg(rhs).arg(JExpr.lit(0)));

    	        // No variable to return, so return null.
    	        return null;
    		} else {
    			throw new RuntimeException(lhs+" claims to be assignable, but I don't know it.");
    		}
    		
    		
    		
    	} else {
    		throw new RuntimeException(lhs+" is not assignable!");
    	}
    	
    }

	
	@Override
	public Void visitCommand(CommandAction cmd, JBlock block) {
		// We need to wrap the SimpleCurrentNext in a CommandHandle interface.
		// That interface lets the ExternalWorld set the state of the handle,
		// as well as return a value.
		
		JClass wrapper = cm.ref(CommandHandle.class);
		if (cmd.getPossibleLeftHandSide() != null) {
			// Ah, there is an assignment. We actually need an anonymous class,
			// so that we can override the assignment method. 
			JDefinedClass wrapperClass = cm.anonymousClass(CommandHandle.class);
			ILExpression assignTo = cmd.getPossibleLeftHandSide();
			JMethod cmdReturnMethod = wrapperClass.method(JMod.PUBLIC, cm.VOID, "commandReturns");
			JVar value = cmdReturnMethod.param(cm.ref(PValue.class), "value");
			
			ILVariable varToCommit = addAssignment(cmdReturnMethod.body(), assignTo, value, cm);
			if (varToCommit != null) {
				// If it's a variable, it should be committed right away too.
				cmdReturnMethod.body().invoke(JExpr.ref(ILExprToJava.getFieldName(varToCommit)), "commit");
			}
			wrapper = wrapperClass;
		}
		
		
        //ex.command(new CommandHandle(directToHandleVar), name, args);
        JInvocation cmdCall = 
        block.invoke(JExpr.invoke("getWorld"), "command")
            .arg(JExpr._new(wrapper).arg(JExpr.ref(ILExprToJava.getFieldName(cmd.getHandle()))))
            .arg(ILExprToJava.toJava(cmd.getName(), cm));
        for (ILExpression arg : cmd.getArgs()) {
            cmdCall.arg(ILExprToJava.toJava(arg, cm));
        }
		return null;
	}

	@Override
	public Void visitResetNode(ResetNodeAction reset, JBlock block) {
        for (ILVariable var : reset.getVars()) {
        	// TODO: Minor refactoring. Can't SimpleVar and ArrayVar share this in common?
        	// Library should probably implement an interface but not share a superclass with these guys.
        	ILVarVisitor<JBlock, Void> visitor = new ILVarVisitor<JBlock, Void>() {

				@Override
				public Void visitSimple(SimpleVar var, JBlock block) {
					addAssignment(block, var, ILExprToJava.toJava(var.getInitialValue(), cm), cm);
		            block.invoke("commitAfterMicroStep").arg(JExpr.ref(ILExprToJava.getFieldName(var)));
					return null;
				}

				@Override
				public Void visitArray(ArrayVar array, JBlock block) {
					addAssignment(block, array, ILExprToJava.toJava(array.getInitialValue(), cm), cm);
		            block.invoke("commitAfterMicroStep").arg(JExpr.ref(ILExprToJava.getFieldName(array)));
					return null;
				}

				@Override
				public Void visitLibrary(LibraryVar lib, JBlock block) {
					return null;
				}
			};
			
			var.accept(visitor, block);
        }
		return null;
	}

	@Override
	public Void visitRunLibraryNode(RunLibraryNodeAction lib, JBlock block) {
        block.add(JExpr.ref(
        		ILExprToJava.getLibraryFieldName(lib.getLibNode().getNodeUID()))
        		.invoke("doMicroStep"));
		return null;
	}

	@Override
	public Void visitUpdate(UpdateAction update, JBlock block) {
        //ExternalWorld w = null;
        //w.update(new UpdateHandle(flag, "name"), key, value);
		
		JVar wrapper = 
			block.decl(cm.ref(UpdateHandle.class), "___handle_wrapper___", 
				JExpr._new(cm.ref(UpdateHandle.class))
					.arg(JExpr.ref(ILExprToJava.getFieldName(update.getHandle())))
					.arg(JExpr.lit(update.getShortName())));
        
        for (Pair<String, ILExpression> p : update.getUpdates()) {
            block.invoke(JExpr.invoke("getWorld"), "update")
                .arg(wrapper)
                .arg(JExpr.lit(p.first))
                .arg(ILExprToJava.toJava(p.second, cm));
        }
		return null;
	}

	@Override
	public Void visitComposite(CompositeAction composite, JBlock param) {
		for (PlexilAction action : composite.getActions()) {
			action.accept(this, param);
		}
		return null;
	}

	@Override
	public Void visitEndMacroStep(EndMacroStep end, JBlock block) {
		// Super easy. 
		block.invoke("endMacroStep");
		return null;
	}

}
