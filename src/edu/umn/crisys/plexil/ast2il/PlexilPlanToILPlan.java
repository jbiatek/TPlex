package edu.umn.crisys.plexil.ast2il;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.Plan;

public class PlexilPlanToILPlan {

    private PlexilPlanToILPlan() {}
    
    public static Plan translate(PlexilPlan p) {
        Plan ilPlan = new Plan(p.getName());
        ilPlan.setCommandDecls(p.getCommandDeclarations());
        ilPlan.setStateDecls(p.getStateDeclarations());
        ilPlan.setLibraryDecls(p.getLibraryDeclarations());
        NodeToIL rootTranslator = new NodeToIL(p.getRootNode());
        rootTranslator.translate(ilPlan);
        ilPlan.setOriginalHierarchy(new OriginalHierarchy(rootTranslator));
        ilPlan.setRootPlexilInterface(p.getRootNode().getInterface());
        return ilPlan;
    }
}
