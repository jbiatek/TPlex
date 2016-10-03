package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.LustreUtil;
import jkind.lustre.NamedType;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;
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
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILOperator;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.vars.ArrayVar;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class ActionsToLustre implements ILActionVisitor<Expr, Void>{

	private Map<ILVariable, PlexilEquationBuilder> varNextValue = new HashMap<>();
	private Map<ILVariable, PlexilEquationBuilder> varKnown = new HashMap<>();
	private PlexilEquationBuilder macroStepIsEnded
		= new PlexilEquationBuilder(LustreNamingConventions.MACRO_STEP_ENDED, LustreUtil.FALSE);
	
	private PlanToLustre translator;
	
	public ActionsToLustre(PlanToLustre translator) {
		this.translator = translator;
	}
	
	public void navigate(Plan p) {
		// Initialize variables
		for (ILVariable v : p.getVariables()) {
			if (LustreNamingConventions.hasValueAndKnownSplit(v)) {
				// There are two equations to build here
				varNextValue.put(v, new PlexilEquationBuilder(
						new IdExpr(LustreNamingConventions.getNumericVariableValueId(v)), 
						translateInitialValue(v)));
				varKnown.put(v, new PlexilEquationBuilder(
						new IdExpr(LustreNamingConventions.getNumericVariableKnownId(v)),
						translateInitialKnownComponent(v)));
			} else {
				varNextValue.put(v, new PlexilEquationBuilder(
						new IdExpr(LustreNamingConventions.getVariableId(v)), 
						translateInitialValue(v)));
			}
		}
		// Set up the macro step ended variable:
		// Get the default macro step assignment (quiescence reached) 
		Expr quiescenceReached = p.getMachines().stream()
				.map(PlanToLustre::getStateIsUnchanged)
				.reduce((l,r) -> new BinaryExpr(l, BinaryOp.AND, r))
				.orElseThrow(() -> new RuntimeException("No state machines?!"));
		// During a step, that's what should end a macro step if nothing else does
		macroStepIsEnded.setDefaultAssignment(quiescenceReached);
		
		// Now we find each action, and pass along the condition in which it
		// should fire. 
		for (NodeStateMachine nsm : p.getMachines()) {
			for (Transition t : nsm.getTransitions()) {
				for (PlexilAction a : t.actions) {
					a.accept(this, translator.getGuardForThisSpecificTransition(t, nsm));
				}
			}
			// State actions
			for (State s : nsm.getStates()) {
				for (PlexilAction a : s.entryActions) {
					// expr = pre(id) != state and id=state
					a.accept(this, PlanToLustre.getEnteringThisSpecificState(nsm, s));
				}
				for (PlexilAction a : s.inActions) {
					// expr = id = state
					a.accept(this, PlanToLustre.getCurrentlyInSpecificState(nsm, s));
				}
			}
		}
	}
	
	private Expr translateInitialValue(ILVariable v) {
		if (v instanceof SimpleVar) {
			Expr init = translator.toLustre(						
					((SimpleVar) v).getInitialValue(), v.getType());
			if (LustreNamingConventions.hasValueAndKnownSplit(v)) {
				// We just want the value part here.
				return ILExprToLustre.getValueComponent(init);
			} else {
				// These are fine to return as-is.
				return init;
			}
			
		} else if (v instanceof ArrayVar) { 
			// Preliminary support for constant arrays
			//System.out.println("WARNING: Array "+v+" is being initialized, but assignments to it are being skipped.");
			
			Expr init = translator.toLustre(((ArrayVar) v).getInitialValue(), v.getType());
			if (LustreNamingConventions.hasValueAndKnownSplit(v)) {
				// Same here, we just want the value component
				return ILExprToLustre.getValueComponent(init);
			} else {
				return init;
			}
		} else {
			throw new RuntimeException("Not supported yet: "+v.getClass());
		}
	}
	
	private Expr translateInitialKnownComponent(ILVariable v) {
		if (v instanceof SimpleVar) {
			SimpleVar simp = (SimpleVar) v;
			Expr init = translator.toLustre(simp.getInitialValue(), v.getType());
			return ILExprToLustre.getKnownComponent(init);
		} else if (v instanceof ArrayVar) {
			ArrayVar arr = (ArrayVar) v;
			Expr init = translator.toLustre(arr.getInitialValue(), v.getType());
			return ILExprToLustre.getKnownComponent(init);
		} else {
			throw new RuntimeException("No 'known' component for "+v.getClass());
		}
	}
	
	public void toLustre(NodeBuilder nb) {
		// Build out all these variables
		for (Entry<ILVariable, PlexilEquationBuilder> entry : varNextValue.entrySet()) {
			nb.addEquation(entry.getValue().buildEquation());
		}
		for (Entry<ILVariable, PlexilEquationBuilder> e : varKnown.entrySet()) {
			nb.addEquation(e.getValue().buildEquation());
		}
		
		nb.addLocal(new VarDecl(LustreNamingConventions.MACRO_STEP_ENDED_ID, 
				NamedType.BOOL));
		nb.addEquation(macroStepIsEnded.buildEquation());
		
	}

	@Override
	public Void visitAlsoRunNodes(AlsoRunNodesAction run, Expr actionCondition) {
		// Nothing to be done here, we don't use this in Lustre. 
		return null;
	}

	@Override
	public Void visitAssign(AssignAction assign, Expr actionCondition) {
		if (assign.getLHS() instanceof SimpleVar) {
			SimpleVar v = (SimpleVar) assign.getLHS();
			
			Expr lustreRHS = translator.toLustre(assign.getRHS(), v.getType());
			// Add this assignment under this condition. The value should be
			// the RHS *before* variables are committed.
			if (LustreNamingConventions.hasValueAndKnownSplit(v)) {
				// Split this up into the separate parts
				Expr value = ILExprToLustre.getValueComponent(lustreRHS);
				Expr known = ILExprToLustre.getKnownComponent(lustreRHS);
				varNextValue.get(v).addAssignment(actionCondition, 
						pre(value));
				varKnown.get(v).addAssignment(actionCondition, 
						pre(known));

			} else {
				varNextValue.get(v).addAssignment(actionCondition, 
						pre(lustreRHS));
			}
			
		} else {
			throw new RuntimeException("Assigning to "+assign.getLHS().getClass()+" not supported in "+assign.toString());
		}
		return null;
	}

	@Override
	public Void visitCommand(CommandAction cmd, Expr actionCondition) {
		// There's no actual need to send a "command" anywhere in Lustre. 
		// We just get "results" back from the environment. To do that, we
		// need the handle to be an input, not a regular variable. 
		
		ILVariable handle = cmd.getHandle();
		
		// In the Lustre translation, command handles are partially inputs.
		translator.addRawInputFor(handle);
		String rawIdName = LustreNamingConventions.getRawInputId(handle);
		Expr rawId = id(rawIdName);
		
		// But this input needs to have restrictions. In addition to the general
		// PLEXIL rule that inputs can't change mid-macrostep, we also want
		// to make sure that it doesn't do weird
		// things like change before the command is even issued.
		
		// In INACTIVE and WAITING, the only valid
		// value is UNKNOWN. The only way to get to those two states is the 
		// initial state of the plan and by resetting. 
		NodeUID node = handle.getNodeUID();
		
		ILExpr inactive = ILOperator.DIRECT_COMPARE.expr(NodeState.INACTIVE, new GetNodeStateExpr(node));
		ILExpr waiting = ILOperator.DIRECT_COMPARE.expr(NodeState.WAITING, new GetNodeStateExpr(node));
		// If the node was SKIPPED, it must have gone directly from WAITING to
		// FINISHED, with the command not being issued. Therefore, it should 
		// be UNKNOWN too.
		ILExpr skipped = ILOperator.DIRECT_COMPARE.expr(
				NodeOutcome.SKIPPED, 
				translator.getNodeOutcomeFor(node));
		// This is also true if the pre-condition failed.
		ILExpr preFail = ILOperator.DIRECT_COMPARE.expr(
				NodeFailureType.PRE_CONDITION_FAILED,
				translator.getFailureTypeFor(node));
		ILExpr handleIsModifiableIL = ILOperator.NOT.expr((ILOperator.OR.expr(
				inactive, waiting, skipped, preFail)));
		
		Expr handleIsModifiable = translator.toLustre(handleIsModifiableIL);
		Expr cmdUnknown = translator.toLustre(CommandHandleState.UNKNOWN, 
				ILType.COMMAND_HANDLE);
		
		// Use these guards for this variable's equation.
		PlexilEquationBuilder builder = varNextValue.get(handle);
		// If it's modifiable, and the new value isn't UNKNOWN, let it in.
		// The official interpreter dictates that the environment is not allowed
		// to set a handle to UNKNOWN. 
		builder.addAssignmentBetweenMacro(and(handleIsModifiable,
				new BinaryExpr(rawId, BinaryOp.NOTEQUAL, cmdUnknown)), 
				rawId);
		
		translator.getTranslationMap().addCommandHandleMapping(rawIdName, 
				cmd.getNameAsConstantString());
		 
		if (cmd.getPossibleLeftHandSide().isPresent()) {
			throw new RuntimeException("Variable assignments from commands not supported yet!");
		}
		
		return null;
	}

	@Override
	public Void visitEndMacroStep(EndMacroStep end, Expr actionCondition) {
		macroStepIsEnded.addAssignment(actionCondition, LustreUtil.TRUE);
		return null;
	}

	@Override
	public Void visitRunLibraryNode(RunLibraryNodeAction lib, Expr actionCondition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitComposite(CompositeAction composite, Expr actionCondition) {
		for (PlexilAction a : composite.getActions()) {
			a.accept(this, actionCondition);
		}
		return null;
	}

	@Override
	public Void visitUpdate(UpdateAction update, Expr actionCondition) {
		// TODO Auto-generated method stub
		return null;
	}


}
