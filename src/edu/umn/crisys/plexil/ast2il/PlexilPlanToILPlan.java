package edu.umn.crisys.plexil.ast2il;

import java.util.Collections;
import java.util.Set;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.globaldecl.CommandDecl;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.runtime.values.NodeTimepoint;

public class PlexilPlanToILPlan {

    private PlexilPlanToILPlan() {}
    
    public static Plan translate(PlexilPlan p) {
    	return translate(p, Collections.emptySet());
    }
    
    public static Plan translate(PlexilPlan p, Set<PlexilPlan> staticLibs) {
        Plan ilPlan = new Plan(p.getName());
        ilPlan.setCommandDecls(p.getCommandDeclarations());
        ilPlan.setStateDecls(p.getStateDeclarations());
        ilPlan.setLibraryDecls(p.getLibraryDeclarations());
        NodeToIL rootTranslator = new NodeToIL(p.getRootNode());
        
        // Static include, if any. 
        StaticLibIncluder.optimize(rootTranslator, ilPlan, staticLibs);

        // If no one has said what "time" is yet, we'll set that now. 
        ilPlan.setTimeIfNotPresent(NodeToIL.TIMEPOINT_TYPE);

        // All done! Translate nodes. 
        rootTranslator.translate(ilPlan);
        ilPlan.setOriginalHierarchy(new OriginalHierarchy(rootTranslator));
        ilPlan.setRootPlexilInterface(p.getRootNode().getInterface());
        return ilPlan;
    }
}
