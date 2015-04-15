package edu.umn.crisys.plexil.il2lustre;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;
import jkind.lustre.builders.NodeBuilder;
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
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.SimpleVar;

public class ActionsToLustre implements ILActionVisitor<Expr, Void>{

	private Map<ILVariable, Expr> varNextValue = 
			new HashMap<ILVariable, Expr>();
	private Expr macroStepStatus;
	
	public void navigate(Plan p) {
		// For all variables, by default they don't change.
		for (ILVariable v : p.getVariables()) {
			varNextValue.put(v, new UnaryExpr(UnaryOp.PRE, ILExprToLustre.toLustre(v, v.getType())));
		}
		// macroStepStatus = <every state machine is unmoved>

		// Now we find each action, and pass along the condition in which it
		// should fire. 
		for (NodeStateMachine nsm : p.getMachines()) {
			for (Transition t : nsm.getTransitions()) {
				for (PlexilAction a : t.actions) {
					a.accept(this, PlanToLustre.getGuardForThisSpecificTransition(t, nsm));
				}
			}
			// State actions
			for (State s : nsm.getStates()) {
				for (PlexilAction a : s.entryActions) {
					// expr = pre(id) != state and id=state
				}
				for (PlexilAction a : s.inActions) {
					// expr = id = state
				}
			}
		}
		
		// Then traverse and add conditions
	}
	
	public void toLustre(NodeBuilder nb) {
		// Variable assignments:
		for (ILVariable v : varNextValue.keySet()) {
			// Create the equation for this variable.
			// Start it out with its initial value
			Expr init;
			if (v instanceof SimpleVar) {
				init = ILExprToLustre.toLustre(((SimpleVar) v).getInitialValue(), v.getType());
			} else {
				throw new RuntimeException("Not supported yet: "+v.getClass());
			}
			// After that, do the big if then else that we made.
			Expr fullThing = new BinaryExpr(init, BinaryOp.ARROW, varNextValue.get(v));
			nb.addEquation(new Equation(new IdExpr(ILExprToLustre.getVariableId(v)),
					fullThing));
		}
		// TODO: Write in quiescence semantics. 
		// Transitions which end the macro step
		// All state machines are the same
		
	}

	@Override
	public Void visitAlsoRunNodes(AlsoRunNodesAction run, Expr param) {
		// Nothing to be done here, we don't use this in Lustre. 
		return null;
	}

	@Override
	public Void visitAssign(AssignAction assign, Expr actionCondition) {
		if (assign.getLHS() instanceof ILVariable) {
			ILVariable v = (ILVariable) assign.getLHS();
			
			// Do the assignment, or else whatever we had before
			varNextValue.put(v, new IfThenElseExpr(actionCondition, 
					ILExprToLustre.toLustre(assign.getRHS(), v.getType()), 
					varNextValue.get(v)));
		} else {
			throw new RuntimeException("Assigning to "+assign.getLHS().getClass()+" not supported");
		}
		return null;
	}

	@Override
	public Void visitCommand(CommandAction cmd, Expr param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitEndMacroStep(EndMacroStep end, Expr param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitRunLibraryNode(RunLibraryNodeAction lib, Expr param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitComposite(CompositeAction composite, Expr param) {
		for (PlexilAction a : composite.getActions()) {
			a.accept(this, param);
		}
		return null;
	}

	@Override
	public Void visitUpdate(UpdateAction update, Expr param) {
		// TODO Auto-generated method stub
		return null;
	}


}
