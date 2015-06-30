package edu.umn.crisys.plexil.il2lustre;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.util.Pair;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;

public class PlexilEquationBuilder {

	private IdExpr id;
	private Expr init;
	private Expr unconditionalStandard;
	private Expr unconditionalMacro;
	private List<Pair<Expr,Expr>> standardAssignments = new ArrayList<>(); 
	private List<Pair<Expr,Expr>> betweenMacroAssignments = new ArrayList<>();
	
	public PlexilEquationBuilder(String id, Expr init) {
		this(new IdExpr(id), init);
	}
	
	public PlexilEquationBuilder(IdExpr id, Expr init) {
		this.id = id;
		this.init = init;
		this.unconditionalStandard = new UnaryExpr(UnaryOp.PRE, id);
		this.unconditionalMacro = new UnaryExpr(UnaryOp.PRE, id);
	}

	/**
	 * Set an assignment which will happen if no other assignments pan out.
	 * By default, this is the previous value of the variable. 
	 * 
	 * @param result
	 */
	public void setDefaultAssignment(Expr result) {
		unconditionalStandard = result;
	}
	
	/**
	 * Add an assignment to this variable which will take place if this guard 
	 * is true. If multiple guards are true, the first one added takes 
	 * precedence. 
	 * @param guard
	 * @param result
	 */
	public void addAssignment(Expr guard, Expr result) {
		standardAssignments.add(new Pair<>(guard, result));
	}
	
	/**
	 * Set an assignment to this variable to occur between macro steps if no
	 * guarded assignment is active. By default, it's the previous value.
	 * 
	 * @param result
	 */
	public void setDefaultAssignmentBetweenMacro(Expr result) {
		unconditionalMacro = result;
	}
	
	/**
	 * Add an assignment to this variable to occur in between macro steps if
	 * the guard is met. If multiple guards are true, the first one added takes
	 * precedence. 
	 * 
	 * @param guard
	 * @param result
	 */
	public void addAssignmentBetweenMacro(Expr guard, Expr result) {
		betweenMacroAssignments.add(new Pair<>(guard, result));
	}
	
	public Equation buildEquation() {
		// v = init -> if (macrostep waiting period) then (that chain) else
		// (standard chain)
		
		return new Equation(id, 
				new BinaryExpr(
				init,
				BinaryOp.ARROW,
					new IfThenElseExpr(
						// If we're in the macrostep waiting period
						PlanToLustre.isInMacrostepWaitingPeriod(), 
						// then do those
						buildIfElseChain(betweenMacroAssignments, unconditionalMacro),
						// else do the standard assignments
						buildIfElseChain(standardAssignments, unconditionalStandard)
					)));
	}

	private Expr buildIfElseChain(List<Pair<Expr, Expr>> guarded,
			Expr last) {
		// Start with the unconditional one
		Expr ret = last;
		// Go backwards, adding conditions (that way, the first one added
		// is toward the front)
		for (int i = guarded.size()-1; i >= 0; i--) {
			ret = new IfThenElseExpr(guarded.get(i).first, 
					guarded.get(i).second, 
					ret);
		}
		return ret; 
	}
	
}
