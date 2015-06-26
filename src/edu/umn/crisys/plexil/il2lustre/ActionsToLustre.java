package edu.umn.crisys.plexil.il2lustre;

import java.util.HashMap;
import java.util.Map;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.NamedType;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;
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
import edu.umn.crisys.plexil.il.expr.nativebool.NativeEqual;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeOperation;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeOperation.NativeOp;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class ActionsToLustre implements ILActionVisitor<Expr, Void>{

	private Map<ILVariable, Expr> varNextValue = 
			new HashMap<ILVariable, Expr>();
	private Expr macroStepIsEnded;
	
	private PlanToLustre translator;
	
	public ActionsToLustre(PlanToLustre translator) {
		this.translator = translator;
	}
	
	public void navigate(Plan p) {
		// For all variables, by default they don't change.
		for (ILVariable v : p.getVariables()) {
			varNextValue.put(v, translator.toLustre(v, v.getType()));
		}
		// macroStepStatus = <every state machine is unmoved>
		for (NodeStateMachine nsm : p.getMachines()) {
			Expr thisMachineDoesntMove = PlanToLustre.getStateIsUnchanged(nsm);
			if (macroStepIsEnded == null) {
				macroStepIsEnded = thisMachineDoesntMove;
			} else {
				macroStepIsEnded = new BinaryExpr(
						thisMachineDoesntMove, 
						BinaryOp.AND, 
						macroStepIsEnded);
			}
		}
		
		
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
	
	public void toLustre(NodeBuilder nb) {
		// Variable assignments:
		for (ILVariable v : varNextValue.keySet()) {
			// Create the equation for this variable.
			// Start it out with its initial value
			Expr init;
			if (v instanceof SimpleVar) {
				init = translator.toLustre(						
						((SimpleVar) v).getInitialValue(), v.getType());
			} else if (v instanceof ArrayVar) { 
				// Preliminary support for constant arrays
				System.out.println("WARNING: Array "+v+" is being initialized, but assignments to it are being skipped.");
				
				IdExpr id = new IdExpr(LustreNamingConventions.getVariableId(v));
				init = translator.toLustre(((ArrayVar) v).getInitialValue(), v.getType());
				// init -> pre(array);
				Expr arrayEq = new BinaryExpr(init, BinaryOp.ARROW, 
						new UnaryExpr(UnaryOp.PRE, id));
				nb.addEquation(new Equation(id, arrayEq));
				continue;
			} else {
				throw new RuntimeException("Not supported yet: "+v.getClass());
			}
			// After that, do the big if then else that we made.
			Expr fullThing = new BinaryExpr(init, BinaryOp.ARROW, varNextValue.get(v));
			nb.addEquation(new Equation(new IdExpr(LustreNamingConventions.getVariableId(v)),
					fullThing));
		}
		
		nb.addLocal(new VarDecl(LustreNamingConventions.MACRO_STEP_ENDED_ID, NamedType.BOOL));
		nb.addEquation(new Equation(new IdExpr(LustreNamingConventions.MACRO_STEP_ENDED_ID), 
				new BinaryExpr(new IdExpr("false"),  BinaryOp.ARROW, macroStepIsEnded)));
		
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
			
			// Do the assignment, or else whatever we had before
			varNextValue.put(v, new IfThenElseExpr(actionCondition, 
					translator.toLustre(assign.getRHS(), v.getType()), 
					varNextValue.get(v)));
		} else {
			throw new RuntimeException("Assigning to "+assign.getLHS().getClass()+" not supported in "+assign.toString());
		}
		return null;
	}

	@Override
	public Void visitCommand(CommandAction cmd, Expr actionCondition) {
		// We need a command handle input
		String rawId = translator.addRawCommandHandleInputFor(cmd.getHandle());
		
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
		Expr guard = new BinaryExpr(
				new UnaryExpr(UnaryOp.PRE, new IdExpr(
						LustreNamingConventions.MACRO_STEP_ENDED_ID)),
				BinaryOp.AND,
				translator.toLustre(inChangeableState));
		
		
		// If we're in any of those states, take from the raw input, else
		// return whatever we've already seen
		varNextValue.put(cmd.getHandle(), new IfThenElseExpr(
				guard,
				new IdExpr(rawId), 
				varNextValue.get(cmd.getHandle())));
		
		if (cmd.getPossibleLeftHandSide().isPresent()) {
			System.err.println("WARNING: Variable assignments from commands not supported yet!");
		}
		
		return null;
	}

	@Override
	public Void visitEndMacroStep(EndMacroStep end, Expr actionCondition) {
		macroStepIsEnded = new BinaryExpr(actionCondition, BinaryOp.OR, macroStepIsEnded);
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
