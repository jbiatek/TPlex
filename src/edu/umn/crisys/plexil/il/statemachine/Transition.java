package edu.umn.crisys.plexil.il.statemachine;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeConstant;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeEval;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExpr;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeOperation;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeOperation.NativeOp;

public class Transition implements Comparable<Transition> {
	
    public String description; 
    /**
     * #1 goes first, then #2, then #3, etc. 
     */
	public int priority;
	public State start;
	public State end;
	public NativeExpr guard;
	public List<PlexilAction> actions = new LinkedList<PlexilAction>() ; 
	
	public Transition(String description, int priority, State start, State end, 
			NativeExpr... guards) {
	    this.description = description;
		this.priority = priority;
		this.start = start;
		if (guards.length == 0) {
			this.guard = NativeConstant.TRUE;
		} else if (guards.length == 1) {
			this.guard = guards[0];
		} else {
			this.guard = new NativeOperation(NativeOp.AND, guards);
		}
		this.end = end;
	}
	
	public boolean isNeverTaken() {
	    Optional<Boolean> result = guard.accept(new NativeEval(), null);
	    return result.isPresent() && result.get() == false;
	}
	
	public boolean isAlwaysTaken() {
	    Optional<Boolean> result = guard.accept(new NativeEval(), null);
	    return result.isPresent() && result.get();
	}
	
	public Transition addAction(PlexilAction action) {
	    actions.add(action);
	    return this;
	}
	
	/**
	 * Add an additional guard. This will be ANDed to the existing guard(s).
	 * @param newGuard
	 */
	public void addGuard(NativeExpr newGuard) {
	    if (guard instanceof NativeOperation) {
	    	NativeOperation guardAsOperation = (NativeOperation) guard;
	    	if (guardAsOperation.getOperation() == NativeOp.AND) {
	    		guardAsOperation.addClause(newGuard);
	    		return;
	    	}
	    }
		this.guard = new NativeOperation(NativeOp.AND, this.guard, newGuard);
	}
	
	@Override
	public String toString() {
		String ret = "("+start+") priority "+priority+" ----> \n";
		ret += description + "\n";
		ret += guard.toString() + "\n";
		for (PlexilAction action : actions) {
		    ret += "[ " +action.toString()+" ]\n";
		}
		
		return ret + " ----> ("+end+")";
	}

	@Override
	public int compareTo(Transition o) {
		if (start == o.start) {
			return priority - o.priority;
		}
		return start.getIndex() - o.start.getIndex();
	}
	
}