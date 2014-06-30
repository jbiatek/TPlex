package edu.umn.crisys.plexil.il.optimize;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.expr.common.Operation;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.AliasExpr;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.RealValue;
import edu.umn.crisys.plexil.java.values.StringValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;

public class AssumeTopLevelPlan implements ILExprVisitor<Void, ILExpression>{
	
	public static boolean looksLikeTopLevelPlan(PlexilPlan plan) {
		// Libraries usually have interfaces, so if there isn't one, it's
		// probably a top level plan. 
		return ! plan.getRootNode().getInterface().isDefined();
	}
	
	private AssumeTopLevelPlan() {}

	public static void optimize(Plan ilPlan) {
		AssumeTopLevelPlan visitor = new AssumeTopLevelPlan();
		
		for (NodeStateMachine nsm : ilPlan.getMachines()) {
			for (Transition t : nsm.getTransitions()) {
				for (TransitionGuard g : t.guards) {
					// Swap the guard out with whatever we say
					g.setExpression(g.getExpression().accept(visitor, null));
				}
			}
		}
	}

	@Override
	public ILExpression visitRootParentState(RootParentStateExpr state,
			Void param) {
		// Plexil root states are always EXECUTING.
		return NodeState.EXECUTING;
	}

	@Override
	public ILExpression visitRootParentExit(RootAncestorExitExpr ancExit,
			Void param) {
		// Their parent isn't executing.
		return BooleanValue.get(false);
	}

	@Override
	public ILExpression visitRootParentEnd(RootAncestorEndExpr ancEnd,
			Void param) {
		// Their parent isn't ending.
		return BooleanValue.get(false);
	}

	@Override
	public ILExpression visitRootParentInvariant(
			RootAncestorInvariantExpr ancInv, Void param) {
		// Parent's invariant hasn't failed.
		return BooleanValue.get(true);
	}
	
	private ILExpression visitComposite(CompositeExpr expr) {
		List<Expression> args = expr.getArguments();
		List<Expression> newArgs = new ArrayList<Expression>();
		for (Expression e : args) {
			newArgs.add(e.accept(this, null));
		}
		return expr.getCloneWithArgs(newArgs);
	}

	@Override
	public ILExpression visitArrayIndex(ArrayIndexExpr array, Void param) {
		return visitComposite(array);
	}

	@Override
	public ILExpression visitLookupNow(LookupNowExpr lookup, Void param) {
		return visitComposite(lookup);
	}

	@Override
	public ILExpression visitLookupOnChange(LookupOnChangeExpr lookup,
			Void param) {
		return visitComposite(lookup);
	}

	@Override
	public ILExpression visitOperation(Operation op, Void param) {
		return visitComposite(op);
	}

	@Override
	public ILExpression visitBooleanValue(BooleanValue bool, Void param) {
		return bool;
	}

	@Override
	public ILExpression visitIntegerValue(IntegerValue integer, Void param) {
		return integer;
	}

	@Override
	public ILExpression visitRealValue(RealValue real, Void param) {
		return real;
	}

	@Override
	public ILExpression visitStringValue(StringValue string, Void param) {
		return string;
	}

	@Override
	public ILExpression visitUnknownValue(UnknownValue unk, Void param) {
		return unk;
	}

	@Override
	public ILExpression visitPValueList(PValueList<?> list, Void param) {
		return list;
	}

	@Override
	public ILExpression visitCommandHandleState(CommandHandleState state,
			Void param) {
		return state;
	}

	@Override
	public ILExpression visitNodeFailure(NodeFailureType type, Void param) {
		return type;
	}

	@Override
	public ILExpression visitNodeOutcome(NodeOutcome outcome, Void param) {
		return outcome;
	}

	@Override
	public ILExpression visitNodeState(NodeState state, Void param) {
		return state;
	}

	@Override
	public ILExpression visitSimple(SimpleVar var, Void param) {
		return var;
	}

	@Override
	public ILExpression visitArray(ArrayVar array, Void param) {
		return array;
	}

	@Override
	public ILExpression visitLibrary(LibraryVar lib, Void param) {
		return lib;
	}

	@Override
	public ILExpression visitGetNodeState(GetNodeStateExpr state, Void param) {
		return state;
	}

	@Override
	public ILExpression visitAlias(AliasExpr alias, Void param) {
		return alias;
	}
	

}
