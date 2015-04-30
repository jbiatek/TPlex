package edu.umn.crisys.plexil.il.optimizations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.umn.crisys.plexil.ast.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.CompositeAction;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeConstant;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeEqual;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExpr;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExprVisitor;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeOperation;
import edu.umn.crisys.plexil.il.expr.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.util.Pair;

public class PruneUnusedVariables {
	
	private PruneUnusedVariables() {}

	/**
	 * Search through the plan for variables that appear in transition guards,
	 * actions, and library node calls. Any variable that we don't find is
	 * removed from the Plan, and any assignment to it is removed as well 
	 * (that variable is written to but never read, commonly node timepoints.)
	 * 
	 * @param ilPlan
	 */
	public static void optimize(Plan ilPlan) {
	    Set<ILExpression> safeList = new HashSet<ILExpression>();
	    
	    // Save any that are being read in a guard or action
	    // (assignment, command, or update could reference it)
	    safeList.add(ilPlan.getRootNodeOutcome());
	    safeList.add(ilPlan.getRootNodeState());
	    for (NodeStateMachine sm : ilPlan.getMachines()) {
	        for (Transition t : sm.getTransitions()) {
	            saveAllVariablesInNative(t.guard, safeList);
	            for (PlexilAction a : t.actions) {
	                scanAllExpressionsInAction(a, safeList);
	            }
	        }
	        for (State s : sm.getStates()) {
	            for (PlexilAction a : s.entryActions) {
	                scanAllExpressionsInAction(a, safeList);
	            }
	            for (PlexilAction a : s.inActions) {
	                scanAllExpressionsInAction(a, safeList);
	            }
	        }
	    }
	    // Library variables also have some stuff that needs taking care of.
	    for (ILVariable v : ilPlan.getVariables()) {
	    	if (v instanceof LibraryVar) {
	    		saveAllVariablesInExpression(v, safeList);
	    	}
	    }
	    
	    // Okay, every variable not on the list is gone.
	    Iterator<ILVariable> iter = ilPlan.getVariables().iterator();
	    Set<ILVariable> killed = new HashSet<ILVariable>();
	    while (iter.hasNext()) {
	    	ILVariable v = iter.next();
	        if (removable(v) && ! safeList.contains(v) ) {
	            iter.remove();
	            killed.add(v);
	        }
	    }
	    // Now go through and remove any actions that use these dead variables.
	    for (NodeStateMachine sm : ilPlan.getMachines()) {
	        for (Transition t : sm.getTransitions()) {
	        	removeActions(killed, t.actions.iterator());
	        }
	        for (State s : sm.getStates()) {
	        	removeActions(killed, s.entryActions.iterator());
	        	removeActions(killed, s.inActions.iterator());
	        }
	    }
	    
	}
	
	private static void removeActions(Set<ILVariable> killedVars, Iterator<PlexilAction> iter) {
		while (iter.hasNext()) {
			PlexilAction a = iter.next();
			if (a instanceof AssignAction) {
				AssignAction assign = (AssignAction) a;
				if (killedVars.contains(assign.getLHS())) {
						iter.remove();
				}
			} else if (a instanceof CompositeAction) {
				CompositeAction composite = (CompositeAction) a;
				removeActions(killedVars, composite.getActions().iterator());
			}
		}
	}
	
	private static boolean removable(ILExpression e) {
		if (e instanceof ILVariable) {
			return true;
		}
		return false;
	}

	private static void scanAllExpressionsInAction(PlexilAction a, Set<ILExpression> safeList) {
        if (a instanceof AssignAction) {
            saveAllVariablesInExpression(((AssignAction) a).getRHS(), safeList);
        } else if (a instanceof CommandAction) {
        	saveAllVariablesInExpression(((CommandAction) a).getPossibleLeftHandSide().orElse(null), safeList);
        	saveAllVariablesInExpression(((CommandAction)a).getName(), safeList);
            saveAllVariablesInExpressions(((CommandAction) a).getArgs(), safeList);
        } else if (a instanceof UpdateAction) {
            for (Pair<String, ILExpression> p : ((UpdateAction) a).getUpdates()) {
                saveAllVariablesInExpression(p.second, safeList);
            }
        }

	}
	
	private static void saveAllVariablesInNative(NativeExpr expr, Set<ILExpression> safeList) {
		expr.accept(new NativeExprVisitor<Set<ILExpression>, Void>() {

			@Override
			public Void visitNativeOperation(NativeOperation op,
					Set<ILExpression> param) {
				op.getArgs().forEach((arg) -> arg.accept(this, param));
				return null;
			}

			@Override
			public Void visitPlexilExprToNative(PlexilExprToNative pen,
					Set<ILExpression> param) {
				saveAllVariablesInExpression(pen.getPlexilExpr(), param);
				return null;
			}

			@Override
			public Void visitNativeEqual(NativeEqual e, Set<ILExpression> param) {
				saveAllVariablesInExpression(e.getLeft(), param);
				saveAllVariablesInExpression(e.getRight(), param);
				return null;
			}
			
			@Override
			public Void visitNativeConstant(NativeConstant c,
					Set<ILExpression> param) {
				// No need to do anything
				return null;
			}

		}, safeList);
	}

	private static void saveAllVariablesInExpressions(List<ILExpression> es, Set<ILExpression> s) {
	    for (ILExpression e : es) {
	        saveAllVariablesInExpression(e, s);
	    }
	}
	
	private static void saveAllVariablesInExpression(ILExpression e, Set<ILExpression> s) {
		if (e == null) {
			return;
		}

	    if (removable(e) && ! s.contains(e)) {
	        s.add(e);
		    if (e instanceof LibraryVar) {
		    	// These have some extra stuff
		    	LibraryVar lib = (LibraryVar) e;
		    	saveAllVariablesInExpression(lib.getLibAndAncestorsInvariants(), s);
		    	saveAllVariablesInExpression(lib.getLibOrAncestorsEnds(), s);
		    	saveAllVariablesInExpression(lib.getLibOrAncestorsExits(), s);
		    	for (String alias : lib.getAliases().keySet()) {
		    		saveAllVariablesInExpression(lib.getAliases().get(alias), s);
		    	}
		    }
	        return;
	    }
	    if (e instanceof CompositeExpr) {
	        CompositeExpr comp = (CompositeExpr) e;
	        for (Expression arg : comp.getArguments()) {
	            saveAllVariablesInExpression((ILExpression) arg, s);
	        }    
	    }
	}

	
}