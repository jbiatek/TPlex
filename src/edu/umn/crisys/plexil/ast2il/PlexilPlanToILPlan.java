package edu.umn.crisys.plexil.ast2il;

import edu.umn.crisys.plexil.ast.core.PlexilPlan;
import edu.umn.crisys.plexil.il.Plan;

public class PlexilPlanToILPlan {

    private PlexilPlanToILPlan() {}
    
    public static Plan translate(PlexilPlan p) {
        Plan ilPlan = new Plan(p.getName());
        NodeToIL rootTranslator = new NodeToIL(p.getRootNode());
        rootTranslator.translate(ilPlan);
        return ilPlan;
    }
}
