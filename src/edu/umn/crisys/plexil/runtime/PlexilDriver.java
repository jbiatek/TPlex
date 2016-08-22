package edu.umn.crisys.plexil.runtime;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.JavaPlanObserver;
import edu.umn.crisys.plexil.runtime.plx.StateCoverageMeasurer;
import edu.umn.crisys.plexil.runtime.psx.symbolic.RandomValues;
import edu.umn.crisys.plexil.runtime.psx.symbolic.ReplayValues;
import edu.umn.crisys.plexil.runtime.psx.symbolic.ScriptDecisionMaker;
import edu.umn.crisys.plexil.runtime.psx.symbolic.SimulatedBacktrackException;
import edu.umn.crisys.plexil.runtime.psx.symbolic.SymbolicDecisionMaker;
import edu.umn.crisys.plexil.runtime.psx.symbolic.SymbolicScript;
import edu.umn.crisys.plexil.runtime.psx.symbolic.ValueSource;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import edu.umn.crisys.plexil.script.translator.ScriptToXML;

public class PlexilDriver {
	
	public static int STEP_LIMIT = -1;
	public static boolean PRINT_INFO = true;
	
	public static void mainMethod(TestGenerationInfo info, String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("No step limit");
			symbolicallyGenerateTests(info);
		} else if (args[0].equalsIgnoreCase("help")) {
			System.out.println("No arguments: Assumes symbolic environment, runs plan to completion.");
			System.out.println("[number]: Assumes symbolic environment, runs plans for at most N steps.");
			System.out.println("Replay sequencefile.txt destination_dir/ [maxSteps]");
			System.out.println("    Replay sequences from sequence file, place resulting scripts in destination.");
			System.out.println("Random numTests maxNumSteps destination_dir");
			System.out.println("    Generate scripts using random values.");
			System.out.println("Filter class_file_location packageName1 packageName2...");
			System.out.println("    Do coverage analysis. Finds Java classes in file location (probably bin/)");
			System.out.println("    inside given package (cannot contain dots). ");
			return;
		} else if (args[0].matches("[0-9]+")) { 
			STEP_LIMIT = Integer.parseInt(args[0]);
			System.out.println("Step limit set to "+STEP_LIMIT);
			symbolicallyGenerateTests(info);
		} else if (args[0].equalsIgnoreCase("Replay")) {
			if (args.length == 4) {
				STEP_LIMIT = Integer.parseInt(args[3]);
			}
			replaySequenceFile(info, new File(args[1]), new File(args[2]));
		} else if (args[0].equalsIgnoreCase("Random")) {
			randomTesting(info, Integer.parseInt(args[1]), Integer.parseInt(args[2]), new File(args[3]));
		} else if (args[0].equalsIgnoreCase("Filter")) {
			File dir = new File(args[1]);
			List<String> classNames = new ArrayList<String>();
			for (int i=2; i<args.length; i++) {
				classNames.addAll(getClassNamesInPackage(new File(dir, args[i]), args[i]));
			}
			coverageFilter(info, classNames);
		}
		System.out.println("All finished.");
	}
	
	public static List<String> getClassNamesInPackage(File directLocationOfClassFiles, String javaPackage) {
		List<String> classNames = new ArrayList<String>();
		
		for (String clazz : directLocationOfClassFiles.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".class");
				}
			})) {

			String rawName = clazz.replaceAll(".class$", "");
			classNames.add(javaPackage.equals("") ? rawName : javaPackage+"."+rawName);
		}
		
		return classNames;
	}
	
	public static void randomTesting(TestGenerationInfo info, int numTests, int stepLimit, File destination) throws IOException {
		List<SymbolicScript> generated = new ArrayList<SymbolicScript>();
		STEP_LIMIT = stepLimit;
		
		while (generated.size() < numTests) {
			System.out.println("Generated "+generated.size()+" tests.");
			RandomValues rand = new RandomValues();
			
			SymbolicScript randScript = runSingleTest(info, rand);
			
			if (isPrefix(randScript, generated)) continue;
			
			// Remove anything that this test case makes redundant. 
			removePrefixesOf(randScript, generated);
			generated.add(randScript);
		}
		
		writeScriptsToDirectory(generated, destination);
	}
	
	public static void coverageFilter(TestGenerationInfo info, List<String> classes) throws Exception {
		PRINT_INFO = false;
		
		StateCoverageMeasurer coverage = new StateCoverageMeasurer();
		Map<NodeOutcome, Integer> outcomes = new HashMap<NodeOutcome, Integer>();
		for (NodeOutcome outcome : NodeOutcome.values()) {
			outcomes.put(outcome, 0);
		}
		
		int currentCoverage = 0;
		
		for (String className : classes) {
			ExternalWorld world = (ExternalWorld) Class.forName(className).newInstance();
			JavaPlan plan = info.createPlanUnderTest(world);
			
			runSingleTest(plan, world, coverage);
			
			outcomes.put(plan.getRootNodeOutcome(), outcomes.get(plan.getRootNodeOutcome())+1);
			int newCoverage = coverage.getNumStatesCovered();
			if (newCoverage > currentCoverage) {
				System.out.println("\nAdds to coverage: "+className);
				currentCoverage = coverage.getNumStatesCovered();
			} else {
				System.out.print(".");
			}
		}
		
		System.out.println("Finished. Covered states: "+currentCoverage);
		System.out.println("Outcomes: ");
		for (NodeOutcome outcome : NodeOutcome.values()) {
			System.out.println("    "+outcome+": "+outcomes.get(outcome));
		}
		
		coverage.printData();
	}
	
	public static void symbolicallyGenerateTests(TestGenerationInfo info) {
		runSingleTest(info, info.createSymbolicValueSource());
	}
	
	public static void replaySequenceFile(TestGenerationInfo info, File sequenceFile, File destination) throws IOException {
		produceScriptFiles(info, ReplayValues.parseSequenceFile(sequenceFile), destination);
	}
	
	public static void produceScriptFiles(TestGenerationInfo info, List<ReplayValues> testCases, File destination) throws IOException {
		List<SymbolicScript> allScripts = new ArrayList<SymbolicScript>();
		for (ReplayValues testCase : testCases) {
			SymbolicScript newCase = runSingleTest(info, testCase);
			// If this one is just a prefix, skip it.
			if (isPrefix(newCase, allScripts)) continue;
			
			// Remove anything that this test case makes redundant. 
			removePrefixesOf(newCase, allScripts);
			allScripts.add(newCase);
		}
		writeScriptsToDirectory(allScripts, destination);
	}
	
	private static void writeScriptsToDirectory(List<SymbolicScript> scripts, File destination) throws IOException {
		int counter = 0;
		destination.mkdirs();
		for (SymbolicScript fullCase : scripts) {
			ScriptToXML.writeToStream(
					new PrintWriter(new File(destination, "test"+counter+".psx")), 
					fullCase.getEventsPerformed());
			counter++;
		}

	}
	
    private static boolean firstIsPrefix(List<?> first, List<?> second) {
        if (first.size() > second.size()) {
            return false;
        }
        for (int i=0; i < first.size(); i++) {
            if ( ! first.get(i).equals(second.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if this script is just a prefix of one we already have. 
     */
    private static boolean isPrefix(SymbolicScript newScript, List<SymbolicScript> allScripts ) {
        for (SymbolicScript existing : allScripts) {
            if (firstIsPrefix(newScript.getEventsPerformed(), existing.getEventsPerformed())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove everything that the new script makes redundant. 
     * @param newScript
     * @param allScripts
     */
    private static void removePrefixesOf(SymbolicScript newScript, List<SymbolicScript> allScripts) {
        Iterator<SymbolicScript> iter = allScripts.iterator();
        while (iter.hasNext()) {
            SymbolicScript otherSeq = iter.next();
            
            if (firstIsPrefix(otherSeq.getEventsPerformed(), newScript.getEventsPerformed())) {
                iter.remove();
            }
            
        }
    }
	
	private static SymbolicScript runSingleTest(TestGenerationInfo info, ValueSource values, JavaPlanObserver... observers) {
		ScriptDecisionMaker dm = info.createDecisionMaker(values);
		SymbolicScript world = new SymbolicScript(dm);
		JavaPlan plan = info.createPlanUnderTest(world);
		runSingleTest(plan, world, observers);
		return world;
	}
	
	private static void runSingleTest(JavaPlan plan, ExternalWorld world, JavaPlanObserver... observers) {
		plan.addObserver(world);
		for (JavaPlanObserver observer : observers) {
			plan.addObserver(observer);
		}

		try {
			int steps = plan.runPlanToCompletion(STEP_LIMIT);
			if (PRINT_INFO) {
				System.out.println("Done in "+steps+" steps. Root node was "+
						plan.getRootNodeState()+", outcome "+plan.getRootNodeOutcome());
			}
		} catch (SimulatedBacktrackException e) {
			// All done, apparently! The path ended before we got to the end.
			if (PRINT_INFO) {
				System.out.println("Backtrack detected");
			}
		}
	}
	
	private PlexilDriver() {}

}
