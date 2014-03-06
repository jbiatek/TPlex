package edu.umn.crisys.plexil.il.statemachine;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;

import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.java.plx.JavaPlan;

public class Transition implements Comparable<Transition> {
	
    public String description; 
	public int priority;
	public State start;
	public State end;
	public List<TransitionGuard> guards;
	public List<PlexilAction> actions = new LinkedList<PlexilAction>() ; 
	
	public Transition(String description, int priority, State start, State end, 
			TransitionGuard... guards) {
	    this.description = description;
		this.priority = priority;
		this.start = start;
		this.guards = new LinkedList<TransitionGuard>(Arrays.asList(guards));
		this.end = end;
	}
	
	public boolean isNeverTaken() {
	    for (TransitionGuard guard : guards) {
	        if ( guard.isNeverActive()) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public boolean isAlwaysTaken() {
	    for (TransitionGuard guard : guards) {
	        if ( ! guard.isAlwaysActive()) {
	            return false;
	        }
	    }
	    return true;
	}
	
	public Transition addAction(PlexilAction action) {
	    actions.add(action);
	    return this;
	}
	
	public void addGuard(TransitionGuard guard) {
	    guards.add(guard);
	}
	
	@Override
	public String toString() {
		String ret = "("+start+") priority "+priority+" ----> \n";
		ret += description + "\n";
		for (TransitionGuard guard : guards) {
		    ret += guard.toString() + "\n";
		}
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