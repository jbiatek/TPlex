package edu.umn.crisys.plexil.il.optimize;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.il.Plan;

public class AssumeTopLevelPlan {
	
	public static boolean looksLikeTopLevelPlan(PlexilPlan plan) {
		// If it has an interface at the top, it's probably not a root. 
		return false;
	}
	
	private AssumeTopLevelPlan() {}

	public static void optimize(Plan ilPlan) {
		
	}
	

}
