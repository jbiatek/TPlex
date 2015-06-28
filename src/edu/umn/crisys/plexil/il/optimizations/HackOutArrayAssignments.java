package edu.umn.crisys.plexil.il.optimizations;

import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.ILActionFilter;

public class HackOutArrayAssignments {

	public static void hack(Plan ilPlan) {
		ilPlan.filterActions(new ILActionFilter<Void>() {
			@Override
			public Boolean visitAssign(AssignAction assign, Void param) {
				if (assign.getLHS() instanceof ArrayVar) {
					System.out.println("WARNING: Array assignments are getting mindlessly deleted. This might break everything.");
					return false;
				}
				return true;
			}
		}, null);
	}

	
}
