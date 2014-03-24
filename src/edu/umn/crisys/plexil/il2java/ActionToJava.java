package edu.umn.crisys.plexil.il2java;

import java.util.HashSet;
import java.util.Set;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AbortCommandAction;
import edu.umn.crisys.plexil.il.action.AlsoRunNodesAction;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CaptureCurrentValueAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.ILActionVisitor;
import edu.umn.crisys.plexil.il.action.ResetNodeAction;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.SetOutcomeAction;
import edu.umn.crisys.plexil.il.action.SetTimepointAction;
import edu.umn.crisys.plexil.il.action.SetVarToPreviousValueAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.translator.il.vars.ArrayReference;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;
import edu.umn.crisys.plexil.translator.il.vars.VariableReference;
import edu.umn.crisys.util.Pair;

public class ActionToJava implements ILActionVisitor<JBlock, Void>{
	
	private JCodeModel cm;
	private Plan ilPlan;
	
	public ActionToJava(JCodeModel cm, Plan ilPlan) {
		this.cm = cm;
		this.ilPlan = ilPlan;
	}

	@Override
	public Void visitAbortCommand(AbortCommandAction abort, JBlock block) {
        block._throw(JExpr._new(cm.ref(RuntimeException.class)).arg("Aborting commands isn't supported yet"));
        return null;
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
        addAssignmentToBlock(block, assign.getLHS(), 
        		ILExprToJava.toJava(assign.getRHS(), cm), 
        		JExpr.lit(assign.getPriority()), cm);
		return null;
	}

    public static void addAssignmentToBlock(JBlock block, IntermediateVariable lhs, 
            JExpression rhs, JExpression priority, JCodeModel cm) {
        //block.decl(cm.ref(PBoolean.class), "temp", JExpr._null());
        lhs.addAssignment(rhs, priority, block, cm);
        JExpression direct = lhs.directReference(cm);
        if (direct != null) {
            block.invoke("commitAfterMacroStep").arg(lhs.directReference(cm));
            block.invoke("endMacroStep");
        }

    }

	
	@Override
	public Void visitCaptureCurrentValue(CaptureCurrentValueAction capture,
			JBlock block) {
        block.assign(capture.getPreviousRef().directReference(cm), 
        		capture.getVar().rhs(cm));
		return null;
	}

	@Override
	public Void visitCommand(CommandAction cmd, JBlock block) {
        //ex.command(caller, name, args);
        JInvocation cmdCall = 
        block.invoke(JExpr.invoke("getWorld"), "command")
            .arg(cmd.getHandle().directReference(cm))
            .arg(ILExprToJava.toJava(cmd.getName(), cm));
        block.invoke("endMacroStep");
        for (ILExpression arg : cmd.getArgs()) {
            cmdCall.arg(ILExprToJava.toJava(arg, cm));
        }
		return null;
	}

	@Override
	public Void visitResetNode(ResetNodeAction reset, JBlock block) {
        for (IntermediateVariable var : reset.getVars()) {
            var.reset(block, cm);
            if (var instanceof VariableReference 
                    || var instanceof ArrayReference) {
                block.invoke("commitAfterMicroStep").arg(var.directReference(cm));
            }
        }
		return null;
	}

	@Override
	public Void visitRunLibraryNode(RunLibraryNodeAction lib, JBlock block) {
        block.add(lib.getLibNode().directReference(cm).invoke("doMicroStep"));
		return null;
	}

	@Override
	public Void visitSetOutcome(SetOutcomeAction outcome, JBlock block) {
		VariableReference outcomeVar = outcome.getOutcomeVar();
		VariableReference failureVar = outcome.getFailureVar();
		NodeFailureType theFailure = outcome.getFailure();
		NodeOutcome theOutcome = outcome.getOutcome();
		
        outcomeVar.addAssignment(cm.ref(NodeOutcome.class).staticRef(theOutcome.toString()), JExpr.lit(1), block, cm);
        block.invoke("commitAfterMicroStep").arg(outcomeVar.directReference(cm));
        if (theFailure != NodeFailureType.UNKNOWN) {
            failureVar.addAssignment(cm.ref(NodeFailureType.class).staticRef(theFailure.toString()), JExpr.lit(1), block, cm);
            block.invoke("commitAfterMicroStep").arg(failureVar.directReference(cm));
        }
		return null;
	}

	@Override
	public Void visitSetTimepoint(SetTimepointAction timepoint, JBlock block) {
        if (timepoint.getTimepoint().isUsed()) {
            timepoint.getTimepoint().addAssignment(
            		ILExprToJava.toJava(Operation.castToNumeric(new LookupNowExpr("time")), cm),
                    JExpr.lit(0), block, cm);
        }
		return null;
	}

	@Override
	public Void visitSetVarToPreviousValue(SetVarToPreviousValueAction revert,
			JBlock block) {
		
		
        revert.getVar().addAssignment(revert.getPrev().directReference(cm), 
        		JExpr.lit(revert.getPriority()), block, cm);
        if (revert.getVar().directReference(cm) != null) {
            block.invoke("commitAfterMacroStep").arg(revert.getVar().directReference(cm));
            block.invoke("endMacroStep");
        }
		return null;
	}

	@Override
	public Void visitUpdate(UpdateAction update, JBlock block) {
        //ExternalWorld w = null;
        //w.update(node, key, value);
        
        for (Pair<String, ILExpression> p : update.getUpdates()) {
            block.invoke(JExpr.invoke("getWorld"), "update")
                .arg(update.getHandle().directReference(cm))
                .arg(JExpr.lit(p.first))
                .arg(ILExprToJava.toJava(p.second, cm));
        }
        block.invoke("endMacroStep");
		return null;
	}

}
