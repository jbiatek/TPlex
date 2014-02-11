package edu.umn.crisys.plexil.il.optimize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umn.crisys.plexil.ast.core.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.translator.il.NodeStateMachine;
import edu.umn.crisys.plexil.translator.il.Plan;
import edu.umn.crisys.plexil.translator.il.State;
import edu.umn.crisys.plexil.translator.il.Transition;
import edu.umn.crisys.plexil.translator.il.TransitionGuard;
import edu.umn.crisys.plexil.translator.il.action.AssignAction;
import edu.umn.crisys.plexil.translator.il.action.CommandAction;
import edu.umn.crisys.plexil.translator.il.action.PlexilAction;
import edu.umn.crisys.plexil.translator.il.action.UpdateAction;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;
import edu.umn.crisys.plexil.translator.il.vars.NodeTimepointReference;
import edu.umn.crisys.util.Pair;

public class PruneUnusedTimepoints {
	
	private PruneUnusedTimepoints() {}

	public static void optimize(Plan ilPlan) {
	    Set<ILExpression> safeList = new HashSet<ILExpression>();
	    
	    Filter<ILExpression> timeFilter = new Filter<ILExpression>() {

            @Override
            public boolean accept(ILExpression obj) {
                return obj instanceof NodeTimepointReference;
            }};
            
	    // Save any that are being read in a guard or action
	    // (assignment, command, or update could reference it)
	    for (NodeStateMachine sm : ilPlan.getMachines()) {
	        for (Transition t : sm.transitions) {
	            for (TransitionGuard g : t.guards) {
	                addAllMatchingInExpressionTo(g.getExpression(), safeList, timeFilter);
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
	    for (IntermediateVariable v : ilPlan.getVariables()) {
	        if (v instanceof NodeTimepointReference && ! safeList.contains(v) ) {
	            ((NodeTimepointReference) v).markAsUnused();
	        }
	    }
	}

	private static void pruneTimepointActionHelper(PlexilAction a, Set<ILExpression> safeList) {
	    Filter<ILExpression> timeFilter = new Filter<ILExpression>() {

	        @Override
	        public boolean accept(ILExpression obj) {
	            return obj instanceof NodeTimepointReference;
	        }};
	        
        if (a instanceof AssignAction) {
            addAllMatchingInExpressionTo(((AssignAction) a).getRHS(), safeList, timeFilter);
        } else if (a instanceof CommandAction) {
            addAllMatchingInExpressionTo(((CommandAction) a).getArgs(), safeList, timeFilter);
        } else if (a instanceof UpdateAction) {
            for (Pair<String, ILExpression> p : ((UpdateAction) a).getUpdates()) {
                addAllMatchingInExpressionTo(p.second, safeList, timeFilter);
            }
        }

	}

	private static interface Filter<T> {
	    public boolean accept(T obj);
	}
	
	private static void addAllMatchingInExpressionTo(List<ILExpression> es, Set<ILExpression> s, Filter<ILExpression> f) {
	    for (ILExpression e : es) {
	        addAllMatchingInExpressionTo(e, s, f);
	    }
	}
	
	private static void addAllMatchingInExpressionTo(ILExpression e, Set<ILExpression> s, Filter<ILExpression> f) {
	    if (f.accept(e)) {
	        s.add(e);
	        return;
	    }
	    
	    if (e instanceof CompositeExpr) {
	        CompositeExpr comp = (CompositeExpr) e;
	        for (Expression arg : comp.getArguments()) {
	            addAllMatchingInExpressionTo((ILExpression) arg, s, f);
	        }    
	    }
	}
	
	
	
}
