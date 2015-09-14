package edu.umn.crisys.plexil.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import simulation.LustreSimulator;
import testsuite.FillNullValues;
import concatenation.CreateHistoryVisitor;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il2lustre.LustrePropertyGenerator;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import edu.umn.crisys.plexil.jkind.results.JKindResultUtils;
import enums.Generation;
import enums.Simulation;
import jkind.JKindExecution;
import jkind.SolverOption;
import jkind.analysis.StaticAnalyzer;
import jkind.lustre.Node;
import jkind.lustre.Program;
import jkind.lustre.builders.NodeBuilder;
import jkind.lustre.builders.ProgramBuilder;
import jkind.translation.RemoveEnumTypes;
import jkind.translation.Translate;
import lustre.LustreTrace;

public class IncrementalJKindSearch {

	public static void generate(Plan il) {
		JKindExecution.timeout = 240;
		int times = 5;
		
		// Just try to do everything!
		System.out.println("Trying all nodes");
		Map<OriginalHierarchy, LustreTrace> results = tryForAllChildren(il);
		for (Entry<OriginalHierarchy, LustreTrace> entry : results.entrySet()) {
			if (entry.getValue() != null) {
				System.out.println("Got test for "+entry.getKey());
			}
		}
		
		// Now try to drill down. 
		Set<OriginalHierarchy> alreadyTried = new HashSet<OriginalHierarchy>();
		for (int i = 0; i < times; i++) {
			// Odds are we missed some though. 
			Set<OriginalHierarchy> tryAgain = new HashSet<>();
			results.entrySet().forEach(entry -> {
				// Did we get this one?
				if (entry.getValue() != null && 
						! alreadyTried.contains(entry.getKey())) {
					// Did we miss any of its children?
					if ( entry.getKey().getChildren().stream()
							.anyMatch(child -> results.get(child) == null)) {
						System.out.println("I want to try starting from "+entry.getKey());
						tryAgain.add(entry.getKey());
					}
				}
			});

			tryAgain.forEach(node -> {
				System.out.println("Trying for children of "+node.getUID()+ " using prefix: ");
				LustreTrace prefix = results.get(node);
				System.out.println(prefix.toString().substring(0, 100)+"...");
				Map<OriginalHierarchy, LustreTrace> newResult = 
						tryForAllChildren(il, Optional.of(node), Optional.of(prefix));
				newResult.entrySet().forEach(entry -> {
					if (results.get(entry.getKey()) != null) {
						System.out.println("Found new test for "+entry.getKey()+"!");
						results.put(entry.getKey(), entry.getValue());
					}
				});
				alreadyTried.add(node);
			});
		}
	}
	
	private static Map<OriginalHierarchy, LustreTrace> tryForAllChildren(Plan il) {
		return tryForAllChildren(il, Optional.empty(), Optional.empty());
	}
	
	private static Program historify(Program p, LustreTrace trace) {
		// Fill in null values
		LustreTrace filled = FillNullValues.fill(trace, p, Generation.DEFAULT);
		LustreSimulator sim = new LustreSimulator(p);
		LustreTrace simulated = sim.simulate(filled, Simulation.COMPLETE);
		
		// Transform this program into something simpler
		Node node = RemoveEnumTypes.node(Translate.translate(p));
		// And add history
		Node history = CreateHistoryVisitor.node(node, simulated);
		// Restore properties from original program
		NodeBuilder nb = new NodeBuilder(history);
		nb.addProperties(p.getMainNode().properties);
		return new Program(nb.build());
	}

	private static Map<OriginalHierarchy,LustreTrace> 
	tryForAllChildren(Plan il, Optional<OriginalHierarchy> root,
			Optional<LustreTrace> prefix) {
		OriginalHierarchy rootToUse = root.orElse(il.getOriginalHierarchy());
		
		
		// Generate Lustre with all obligations
		PlanToLustre translator = new PlanToLustre(il);
		LustrePropertyGenerator properties = 
				translator.getPropertyGenerator();
		
		List<OriginalHierarchy> allChildren = getAllNodes(rootToUse);
		allChildren.forEach(node -> properties.addNoFailureExecuteProperty(node));
		
		Program original = translator.toLustre();
		Program lustre = new Program(RemoveEnumTypes.node(
				Translate.translate(original)));
		if (prefix.isPresent()) {
			lustre = historify(lustre, prefix.get());
		}
		// Print to a tem
		
		System.out.println("Performing static check...");
		//System.out.println(lustre);
		StaticAnalyzer.check(lustre, SolverOption.Z3);
		
		// Run JKind on it and see what we get
		Map<String, LustreTrace> strResult = 
				JKindExecution.execute(lustre);
		 
		// Remap it to be in terms of nodes
		Map<OriginalHierarchy, LustreTrace> result = new HashMap<>();
		allChildren.forEach(node -> 
			result.put(node, strResult.get(
					LustrePropertyGenerator.getNoFaiureExecuteId(node)))
				);
		return result;
	}

	private static List<OriginalHierarchy> getAllNodes(
			OriginalHierarchy root) {
		List<OriginalHierarchy> ret = new ArrayList<>();
		ret.add(root);
		root.getChildren().forEach(
				child -> ret.addAll(getAllNodes(child)));
		return ret;
	}
}
