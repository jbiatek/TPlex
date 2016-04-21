package edu.umn.crisys.plexil.ast2il;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.globaldecl.CommandDecl;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.il.Plan;

public class StaticLibIncluder {

	/**
	 * Statically include the given PlexilPlans if there are any Library
	 * nodes that refer to them. The given NodeToIL translator will be 
	 * modified to include any libraries found. The IL Plan object will also 
	 * be augmented with any global declarations found in included libraries.
	 * 
	 * (This optimization basically has to happen before translation to the 
	 * IL -- the library will need ancestor conditions built in to its state
	 * machine, so doing this at the IL level requires some reverse translation
	 * back to pure PLEXIL. )
	 * 
	 * @param rootTranslator
	 * @param ilDestination
	 * @param staticLibs
	 */
	public static void optimize(NodeToIL rootTranslator, Plan ilDestination,
			Set<PlexilPlan> staticLibs) {
    	Set<PlexilPlan> includedLibs = recurseOverNodes(rootTranslator, staticLibs);
    	// Make sure that we tell the IL plan about any new command or 
    	// lookup declarations. (Including library declarations kind of 
    	// seems dumb, so I'm punting that until we actually need it.)
    	for (PlexilPlan lib : includedLibs) {
    		for (CommandDecl cmd : lib.getCommandDeclarations()) {
    			if (ilDestination.getCommandDecls().stream()
    					.noneMatch(c -> c.getName().equals(cmd.getName()))){
    				ilDestination.getCommandDecls().add(cmd);
    			}
    		}
    		for (LookupDecl look : lib.getStateDeclarations()) {
    			if (ilDestination.getStateDecls().stream()
    					.noneMatch(c -> c.getName().equals(look.getName()))){
    				ilDestination.getStateDecls().add(look);
    			}
    		}
    	}
    	// Re-shorten node UIDs
    	rootTranslator.getUID().shortenUniqueNames();
	}
	
	/**
	 * Descend down the node hierarchy looking for Library nodes. If one is 
	 * found, try to find the parsed plan in `libs` that has the matching
	 * ID. If that's there too, replace the Library node with the contents of
	 * that parsed plan. 
	 * 
	 * Note that this is a node-level change: the libraries are stuck into the
	 * translation in place of the library node, as if someone had copy-pasted
	 * the root node of the library over it. It doesn't handle things like 
	 * lookup and command declarations. Instead, the libraries that actually
	 * get included are returned so that Someone Else can do it. 
	 * 
	 * @param root The root node to search through for Libraries
	 * @param libs The possible libraries to use as replacements.
	 * @return All libraries that were actually included. 
	 */
	private static Set<PlexilPlan> recurseOverNodes(NodeToIL root, Set<PlexilPlan> libs) {
		if (libs.isEmpty()) {
			return Collections.emptySet();
		}
		Set<PlexilPlan> ret = new HashSet<>();
		if (root.hasLibraryHandle()) {
			// If we've got one to put here, let's do it.
			String lookingFor = root.getLibraryHandle().getLibraryPlexilID(); 
			boolean found = false;
			for (PlexilPlan lib : libs) {
				String rootID = lib.getRootNode().getPlexilID().orElse("<empty>");
				if (lookingFor.equals(rootID)) {
					// Success!
					root.convertLibraryToList(lib.getRootNode());
					ret.add(lib);
					found = true;
					break;
				}
			}
			if ( ! found) {
				System.err.println("Warning: Tried to statically include "+
						lookingFor +" but it wasn't found in lib path.");
			}
		}
		// Library or not, traverse down the tree. 
		for (NodeToIL child : root.getChildren()) {
			ret.addAll(recurseOverNodes(child, libs));
		}
		return ret;
	}
	
	private StaticLibIncluder() {}
}
