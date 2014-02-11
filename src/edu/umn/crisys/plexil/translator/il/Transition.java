package edu.umn.crisys.plexil.translator.il;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;

import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.translator.il.action.PlexilAction;

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
	
	
	/**
     * Add this transition on to the previous one. If this is the first, 
     * pass in null.
     * 
     * @param block The block of code we're adding to
     * @param cm
     * @param prev The conditional returned by the last transition
     * @return The JConditional for this transition. Pass it to the next Transition.
     */
	public JConditional addTransition(JBlock block, JCodeModel cm, JFieldVar stateVar, JConditional prev) {
		// Is this our start state?
//	    JExpression condExp = JExpr.invoke("getState").eq(
//				cm.ref(NodeState.class).staticRef(start.toString()));
		
	    
	    
		// Is each of our guards satisfied?
	    JExpression condExp = null;
		for (TransitionGuard guard : guards) {
		    if (guard.isAlwaysActive()) {
		        // We can skip this, it's just going to be true
		        continue;
		    }
		    if (condExp != null) {
		        condExp = condExp.cand(guard.getJavaExpression(cm));
		    } else {
		        condExp = guard.getJavaExpression(cm);
		    }
		}
		
		// Figure out if we're doing if, else if, nothing at all...
		JConditional current;
		JBlock thenBlock;
		if (condExp == null ) {
		    // There are no guards, so we don't need an if statement.
		    if (prev == null) {
		        // No previous if statements either. We're the first and last
		        // transition. Let's just put it in directly then!
		        current = null;
		        thenBlock = block;
		    } else {
		        // There's a previous if statement. We'll add ourselves on
		        // as an else.
		        current = null;
		        thenBlock = prev._else();
		    }
		    
		} else if (prev == null) {
		    // We have guards, and we're first in line. We'll make an if:
			current = block._if(condExp);
			thenBlock = current._then().block();
		} else {
		    // We have guards, but we're not first. We'll daisy chain:
			current = prev._elseif(condExp);
			thenBlock = current._then().block();
		}
		
		// Now for the action, which goes into thenBlock.
		// Add a readable comment:
		thenBlock.directStatement("/*\n"+this.toString()+"\n*/");
		// Some debug code to print what's happening if desired
		thenBlock._if(cm.ref(JavaPlan.class).staticRef("DEBUG"))
			._then()
				.invoke(cm.ref(System.class).staticRef("out"), "println")
					.arg(JExpr.lit(description));
		
		// Perform the actions, if any, then move to the destination state.
		for (PlexilAction action : actions) {
		    action.addActionToBlock(thenBlock, cm);
		}

		end.addEntranceToBlock(cm, thenBlock, stateVar);
		
		// Return our if statement to be daisy chained off of
		return current;
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