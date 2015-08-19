package edu.umn.crisys.plexil.il2lustre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.util.Pair;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;

/**
 * Build an equation using some standard conventions. Add pairs of guards
 * and values to have this variable take the given value if the guard is true. 
 * The order used to pass these in is maintained, so you do not have to worry
 * about, say, checking that a higher priority transition is active as long as
 * you add them in order. If no guard holds, there is a default assignment
 * which is unguarded. By default, this is simply pre(id). 
 * 
 * <p>You may also choose to specify a second set of guards and values to be
 * used in the last micro step of the current macro step. This can be used to
 * have a variable change before the next macro step begins. Since almost every
 * reference to a variable is actually looking at the pre() value, this has the
 * effect of changing the variable "between" macro steps. 
 * 
 * <p>If any of the "between macro" methods are
 * used, an entirely separate set of guards and values will be used in that
 * special case. Again, if no guard holds, the "between macro" unguarded value 
 * is used, which is again simply pre(). If you never use these methods to add a  
 * special case, the default rules will be used regardless of the macro step.  
 * 
 * @author jbiatek
 *
 */
public class PlexilEquationBuilder {

	private IdExpr id;
	private Expr init;
	private Expr unconditionalStandard;
	private Optional<Expr> unconditionalMacro;
	private List<Pair<Expr,Expr>> standardAssignments = new ArrayList<>(); 
	private List<Pair<Expr,Expr>> betweenMacroAssignments = new ArrayList<>();
	
	public PlexilEquationBuilder(String id, Expr init) {
		this(new IdExpr(id), init);
	}
	
	public PlexilEquationBuilder(IdExpr id, Expr init) {
		this.id = id;
		this.init = init;
		this.unconditionalStandard = new UnaryExpr(UnaryOp.PRE, id);
		this.unconditionalMacro = Optional.empty();
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
		unconditionalMacro = Optional.of(result);
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
	
	private boolean hasSpecialMacroStepRules() {
		return (! betweenMacroAssignments.isEmpty()) || unconditionalMacro.isPresent(); 
	}
	
	public Equation buildEquation() {
		if (hasSpecialMacroStepRules()) {
			// v = init -> if (new macrostep) then (that chain) else
			// (standard chain)
			
			// Use pre(id) if they didn't specify anything.
			Expr realUnconditionalMacro = unconditionalMacro
					.orElse(new UnaryExpr(UnaryOp.PRE, id));
			
			return new Equation(id, 
					new BinaryExpr(
					init,
					BinaryOp.ARROW,
						new IfThenElseExpr(
							// If we're beginning a new macro step next time
							PlanToLustre.isCurrentlyEndOfMacroStep(), 
							// then do those
							buildIfElseChain(betweenMacroAssignments, realUnconditionalMacro),
							// else do the standard assignments
							buildIfElseChain(standardAssignments, unconditionalStandard)
						)));
		} else {
			// v = init -> if (guard) then v else ... else unguarded
			// No special macro step rules. Just do the standard ones.
			return new Equation(id, 
					new BinaryExpr(
							init,
							BinaryOp.ARROW,
							buildIfElseChain(standardAssignments, 
									unconditionalStandard)));
		}
		
		
		
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
