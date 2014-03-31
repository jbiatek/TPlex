package edu.umn.crisys.plexil.il.optimize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umn.crisys.plexil.ast.core.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.util.Pair;

public class PruneUnusedTimepoints {
	
	private PruneUnusedTimepoints() {}

	public static void optimize(Plan ilPlan) {
	    Set<ILExpression> safeList = new HashSet<ILExpression>();
	    
	    // Save any that are being read in a guard or action
	    // (assignment, command, or update could reference it)
	    for (NodeStateMachine sm : ilPlan.getMachines()) {
	        for (Transition t : sm.transitions) {
	            for (TransitionGuard g : t.guards) {
	                addAllMatchingInExpressionTo(g.getExpression(), safeList);
	            }
	            for (PlexilAction a : t.actions) {
	                pruneTimepointActionHelper(a, safeList);
	            }
	        }
	        for (State s : sm.states) {
	            for (PlexilAction a : s.entryActions) {
	                pruneTimepointActionHelper(a, safeList);
	            }
	            for (PlexilAction a : s.inActions) {
	                pruneTimepointActionHelper(a, safeList);
	            }
	        }
	    }
	    
	    // Okay, everything not on the list is being told to leave itself out.
	    
	    for ( ILVariable v : ilPlan.getVariables()) {
	        if (isTimepoint(v) && ! safeList.contains(v) ) {
	            ((SimpleVar) v).markAsUnused();
	        }
	    }
	    
	}
	
	private static boolean isTimepoint(ILExpression e) {
		if (e instanceof SimpleVar) {
			SimpleVar v = (SimpleVar) e;
			return v.getName().endsWith(".START") || v.getName().endsWith(".END");
		}
		return false;
	}

	private static void pruneTimepointActionHelper(PlexilAction a, Set<ILExpression> safeList) {
        if (a instanceof AssignAction) {
            addAllMatchingInExpressionTo(((AssignAction) a).getRHS(), safeList);
        } else if (a instanceof CommandAction) {
            addAllMatchingInExpressionTo(((CommandAction) a).getArgs(), safeList);
        } else if (a instanceof UpdateAction) {
            for (Pair<String, ILExpression> p : ((UpdateAction) a).getUpdates()) {
                addAllMatchingInExpressionTo(p.second, safeList);
            }
        }

	}

	private static void addAllMatchingInExpressionTo(List<ILExpression> es, Set<ILExpression> s) {
	    for (ILExpression e : es) {
	        addAllMatchingInExpressionTo(e, s);
	    }
	}
	
	private static void addAllMatchingInExpressionTo(ILExpression e, Set<ILExpression> s) {
	    if (isTimepoint(e)) {
	        s.add(e);
	        return;
	    }
	    
	    if (e instanceof CompositeExpr) {
	        CompositeExpr comp = (CompositeExpr) e;
	        for (Expression arg : comp.getArguments()) {
	            addAllMatchingInExpressionTo((ILExpression) arg, s);
	        }    
	    }
	}

	
}
