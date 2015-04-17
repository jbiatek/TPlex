package edu.umn.crisys.plexil.ast2il;

import java.util.Set;

import edu.umn.crisys.plexil.ast.PlexilPlan;

public class StaticLibIncluder {

	/**
	 * Descend down the node hierarchy looking for Library nodes. If one is 
	 * found, try to find the parsed plan in `libs` that has the matching
	 * ID. If that's there too, replace the Library node with the contents of
	 * that parsed plan. 
	 * 
	 * @param root The root node to search through for Libraries
	 * @param libs The possible libraries to use as replacements.
	 */
	public static void optimize(NodeToIL root, Set<PlexilPlan> libs) {
		if (root.hasLibraryHandle()) {
			// If we've got one to put here, let's do it.
			for (PlexilPlan lib : libs) {
				String rootID = lib.getRootNode().getPlexilID().orElse("<empty");
				if (root.getLibraryHandle().getLibraryPlexilID().equals(rootID)) {
					// Success!
					System.out.println("Statically including "+rootID);
					root.convertLibraryToList(lib.getRootNode());
					break;
				}
			}
		} else {
			for (NodeToIL child : root.getChildren()) {
				optimize(child, libs);
			}
		}
	}
	
	private StaticLibIncluder() {}
}
