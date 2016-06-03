package edu.umn.crisys.plexil.il2java;

import java.util.HashSet;
import java.util.Optional;
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

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AlsoRunNodesAction;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.CompositeAction;
import edu.umn.crisys.plexil.il.action.EndMacroStep;
import edu.umn.crisys.plexil.il.action.ILActionVisitor;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.expr.AliasExpr;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILOperation;
import edu.umn.crisys.plexil.il.expr.vars.ArrayVar;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.runtime.plx.CommandHandle;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.UpdateHandle;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.util.Pair;

public class ActionToJava implements ILActionVisitor<JBlock, Void>{
	
	/**
	 * Add an assignment to the block. The right hand side can be any Java
	 * expression, technically, but of course it won't compile if you do 
	 * something silly. The left hand side must be assignable. 
	 * 
	 * You'll probably also want to 
	 * commit the variable afterward. This method will return the ILVariable
	 * that was used for the assignment, if any, which you can then 
	 * perform the commit on. 
	 * 
	 * @param block
	 * @param lhs
	 * @param rhs
	 * @param cm
	 */
	public static Optional<ILVariable> addAssignment(JBlock block, ILExpr lhs, JExpression rhs, JCodeModel cm) {
		if (lhs.isAssignable()) {
			if (lhs instanceof SimpleVar) {
				block.invoke(JExpr.ref(ILExprToJava.getFieldName((SimpleVar)lhs)), "setNext")
					.arg(rhs);
				return Optional.of((SimpleVar)lhs);
			} else if (lhs instanceof ArrayVar) {
				// Must be a whole array assignment.
				block.invoke(JExpr.ref(ILExprToJava.getFieldName((ArrayVar) lhs)), 
						"arrayAssign").arg(rhs);
				return Optional.of((ArrayVar) lhs);
			} else if (lhs instanceof ILOperation) {
				// Must be an index of an array. 
				ILOperation arrayIndex = (ILOperation) lhs;
				ILExpr theArray = arrayIndex.getBinaryFirst();
				ILExpr index = (ILExpr) arrayIndex.getBinarySecond();

				if (theArray instanceof ArrayVar) {
					ArrayVar localArray = (ArrayVar) arrayIndex.getBinaryFirst();
					
					block.invoke(JExpr.ref(ILExprToJava.getFieldName(localArray)), "indexAssign")
						.arg(ILExprToJava.toJava(index, cm)).arg(rhs);
					return Optional.of(localArray);
				} else if (theArray instanceof AliasExpr) {
					AliasExpr alias = (AliasExpr) theArray;
					// Pass this through the interface for assignment.
			        block.add( JExpr.invoke("getInterface").invoke("performAssignment")
			        		.arg(alias.getName())
			        		.arg(ILExprToJava.toJava(index, cm))
			        		.arg(rhs)
			        		.arg(JExpr.lit(0)));
		
			        // No variable to return, so return empty.
			        return Optional.empty();

					
				} else {
					throw new RuntimeException("Can't handle "+theArray.getClass()+" here,"
							+ " trying to assign to it with an array index.");
				}
				
				
			} else if (lhs instanceof AliasExpr) {
				AliasExpr alias = (AliasExpr) lhs;
				// Pass this through the interface for assignment.
		        block.add( JExpr.invoke("getInterface").invoke("performAssignment")
		        		.arg(alias.getName()).arg(rhs).arg(JExpr.lit(0)));
	
		        // No variable to return, so return empty.
		        return Optional.empty();
			} else {
				throw new RuntimeException(lhs+" claims to be assignable, but I don't know it.");
			}
			
			
			
		} else {
			throw new RuntimeException(lhs+" is not assignable!");
		}
		
	}

	public static void addImmediateCommit(JBlock block, ILVariable varToCommit) {
		block.invoke(JExpr.ref(ILExprToJava.getFieldName(varToCommit)), "commit");
	}
	
	public static void addCommitAfterMicroStep(JBlock block, ILVariable varToCommit) {
        block.invoke("commitAfterMicroStep").arg(JExpr.ref(ILExprToJava.getFieldName(varToCommit)));
	}
	
	public static void addCommitAfterMacroStep(JBlock block, ILVariable varToCommit) {
        block.invoke("commitAfterMacroStep").arg(JExpr.ref(ILExprToJava.getFieldName(varToCommit)));
	}
	
	
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
		
		// Assign and commit result after micro step
		addAssignment(block, assign.getLHS(), ILExprToJava.toJava(assign.getRHS(), cm), cm)
			.ifPresent((varToCommit) -> addCommitAfterMicroStep(block, varToCommit));
        
		return null;
	}

    @Override
	public Void visitCommand(CommandAction cmd, JBlock block) {
		// We need to wrap the SimpleCurrentNext in a CommandHandle interface.
		// That interface lets the ExternalWorld set the state of the handle,
		// as well as return a value.
		
		JClass wrapper = cm.ref(CommandHandle.class);
		if (cmd.getPossibleLeftHandSide().isPresent()) {
			// Ah, there is an assignment. We actually need an anonymous class,
			// so that we can override the assignment method. 
			JDefinedClass wrapperClass = cm.anonymousClass(CommandHandle.class);
			ILExpr assignTo = cmd.getPossibleLeftHandSide().get();
			JMethod cmdReturnMethod = wrapperClass.method(JMod.PUBLIC, cm.VOID, "commandReturns");
			JVar value = cmdReturnMethod.param(cm.ref(PValue.class), "value");
			
			// If it's a variable, it should be committed right away too.
			addAssignment(cmdReturnMethod.body(), assignTo, value, cm)
				.ifPresent((varToCommit) -> addImmediateCommit(cmdReturnMethod.body(), varToCommit)); 

			wrapper = wrapperClass;
		}
		
		
        //ex.command(new CommandHandle(directToHandleVar), name, args);
        JInvocation cmdCall = 
        block.invoke(JExpr.invoke("getWorld"), "command")
            .arg(JExpr._new(wrapper).arg(JExpr.ref(ILExprToJava.getFieldName(cmd.getHandle()))))
            .arg(ILExprToJava.toJava(cmd.getName(), cm));
        for (ILExpr arg : cmd.getArgs()) {
            cmdCall.arg(ILExprToJava.toJava(arg, cm));
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
        
        for (Pair<String, ILExpr> p : update.getUpdates()) {
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
