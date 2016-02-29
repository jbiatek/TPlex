package edu.umn.crisys.plexil.il.statemachine;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILOperator;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class Transition implements Comparable<Transition> {
	
    public String description; 
    /**
     * #1 goes first, then #2, then #3, etc. 
     */
	public int priority;
	public State start;
	public State end;
	public ILExpr guard;
	public List<PlexilAction> actions = new LinkedList<PlexilAction>() ; 
	
	public Transition(String description, int priority, State start, State end, 
			ILExpr... guards) {
	    this.description = description;
		this.priority = priority;
		this.start = start;
		if (guards.length == 0) {
			this.guard = NativeBool.TRUE;
		} else if (guards.length == 1) {
			if (guards[0].getType().equals(ILType.NATIVE_BOOL)) {
				this.guard = guards[0];
			} else {
				throw new RuntimeException("Type error: Guard had type "
						+guards[0].getType());
			}
		} else {
			// This will type check for us
			this.guard = ILOperator.AND.expr(guards);
		}
		this.end = end;
	}
	
	public boolean isNeverTaken() {
	    Optional<PValue> result = guard.eval();
	    return result.isPresent() && result.get().equals(NativeBool.FALSE);
	}
	
	public boolean isAlwaysTaken() {
	    Optional<PValue> result = guard.eval();
	    return result.isPresent() && result.get().equals(NativeBool.TRUE);
	}
	
	public Transition addAction(PlexilAction action) {
	    actions.add(action);
	    return this;
	}
	
	/**
	 * Add an additional guard. This will be ANDed to the existing guard(s).
	 * @param newGuard
	 */
	public void addGuard(ILExpr newGuard) {
		this.guard = ILOperator.AND.expr(guard, newGuard);
	}
	
	@Override
	public String toString() {
		String ret = "("+start+") priority "+priority+" ----> \n";
		ret += "    "+description + "\n";
		ret += "    Guard: "+guard.toString() + "\n";
		for (PlexilAction action : actions) {
		    ret += "    Action: [ " +action.toString()+" ]\n";
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