package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.pre;

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
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeEqual;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeOperation;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeOperation.NativeOp;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
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
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class ActionsToLustre implements ILActionVisitor<Expr, Void>{

	private Map<ILVariable, PlexilEquationBuilder> varNextValue = new HashMap<>();
	private PlexilEquationBuilder macroStepIsEnded
		= new PlexilEquationBuilder(LustreNamingConventions.MACRO_STEP_ENDED, LustreUtil.FALSE);
	
	private PlanToLustre translator;
	
	public ActionsToLustre(PlanToLustre translator) {
		this.translator = translator;
	}
	
	public void navigate(Plan p) {
		// Initialize variables
		for (ILVariable v : p.getVariables()) {
			varNextValue.put(v, new PlexilEquationBuilder(
					new IdExpr(LustreNamingConventions.getVariableId(v)), 
					translateInitial(v)));
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
	
	private Expr translateInitial(ILVariable v) {
		if (v instanceof SimpleVar) {
			return translator.toLustre(						
					((SimpleVar) v).getInitialValue(), v.getType());
		} else if (v instanceof ArrayVar) { 
			// Preliminary support for constant arrays
			System.out.println("WARNING: Array "+v+" is being initialized, but assignments to it are being skipped.");
			
			return translator.toLustre(((ArrayVar) v).getInitialValue(), v.getType());
		} else {
			throw new RuntimeException("Not supported yet: "+v.getClass());
		}
	}
	
	public void toLustre(NodeBuilder nb) {
		// Build out all these variables
		for (Entry<ILVariable, PlexilEquationBuilder> entry : varNextValue.entrySet()) {
			nb.addEquation(entry.getValue().buildEquation());
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
			
			// Add this assignment under this condition. The value should be
			// the RHS *before* variables are committed.
			varNextValue.get(v).addAssignment(actionCondition, 
					pre(translator.toLustre(assign.getRHS(), v.getType())));
		} else {
			throw new RuntimeException("Assigning to "+assign.getLHS().getClass()+" not supported in "+assign.toString());
		}
		return null;
	}

	@Override
	public Void visitCommand(CommandAction cmd, Expr actionCondition) {
		// We need a command handle input
		String rawId = translator.addRawCommandHandleInputFor(cmd.getHandle(),
				cmd.getNameAsConstantString());
		
		/*
		 * It kind of looks like handles should only change in EXECUTING, 
		 * FINISHING, and FAILING maybe. Before that, no command has been 
		 * issued, and afterward the node is all done and there have been some 
		 * checks to ensure that at least something happened with the command. 
		 * 
		 * In theory, in the PLEXIL exec, I don't think there's anything
		 * stopping an ill-behaved environment from doing weird things with
		 * command handles, like marking them as successful before they even
		 * start or changing their status after they finish. Maybe the exec
		 * guards against that, and we can make these guarantees. If not,
		 * it would probably be good to make this check optional in the
		 * translator, since one might want to make sure that properties hold
		 * even in the face of a badly behaved environment. 
		 */
		//TODO: Check with Plexil team and see if this is acceptable.
		NodeUID node = cmd.getHandle().getNodeUID();
		
		NativeEqual executing = new NativeEqual(NodeState.EXECUTING, new GetNodeStateExpr(node));
		NativeEqual finishing = new NativeEqual(NodeState.FINISHING, new GetNodeStateExpr(node));
		NativeEqual failing = new NativeEqual(NodeState.FAILING, new GetNodeStateExpr(node));
		NativeOperation inChangeableState = new NativeOperation(NativeOp.OR, executing, finishing, failing);
		// Need to be starting a new macro step, and in one of those states.
		Expr guard = translator.toLustre(inChangeableState);
		
		// If we're in any of those states between macro steps, take from the
		// raw input. And we want the final value, not the "pre" value.
		varNextValue.get(cmd.getHandle()).addAssignmentBetweenMacro(
				guard, 
				new IdExpr(rawId));
		
		if (cmd.getPossibleLeftHandSide().isPresent()) {
			System.err.println("WARNING: Variable assignments from commands not supported yet!");
		}
		if ( ! cmd.getArgs().isEmpty()) {
			System.err.println("WARNING: All args to command "+cmd+" are being ignored!");
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
