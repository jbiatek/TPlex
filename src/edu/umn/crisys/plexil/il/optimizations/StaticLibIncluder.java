package edu.umn.crisys.plexil.il.optimizations;

import java.util.Map;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast2il.NodeToIL;

public class StaticLibIncluder {

	
	public static void optimize(NodeToIL node, Map<String,PlexilPlan> libs) {
		if (node.hasLibraryHandle()) {
			// If we've got one to put here, let's do it.
			for (String name : libs.keySet()) {
				String rootID = libs.get(name).getRootNode().getPlexilID().orElse("<empty");
				if (node.getLibraryHandle().getLibraryPlexilID().equals(rootID)) {
					// Success!
					System.out.println("Statically including "+rootID);
					node.convertLibraryToList(libs.get(name).getRootNode());
					break;
				}
			}
		} else {
			for (NodeToIL child : node.getChildren()) {
				optimize(child, libs);
			}
		}
	}
	
	private StaticLibIncluder() {}
}
