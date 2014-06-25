package edu.umn.crisys.plexil.java;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.StateCoverageMeasurer;
import edu.umn.crisys.plexil.java.psx.symbolic.RandomValues;
import edu.umn.crisys.plexil.java.psx.symbolic.ReplayValues;
import edu.umn.crisys.plexil.java.psx.symbolic.SimulatedBacktrackException;
import edu.umn.crisys.plexil.java.psx.symbolic.SymbolicDecisionMaker;
import edu.umn.crisys.plexil.java.psx.symbolic.SymbolicScript;
import edu.umn.crisys.plexil.java.psx.symbolic.ValueSource;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.script.translator.ScriptToXML;

public class PlexilDriver {
	
	public static int STEP_LIMIT = 100; // TODO: This should be -1, and Replays need to be able to set this too
	
	public static void mainMethod(TestGenerationInfo info, String[] args) throws Exception {
		if (args.length == 0) {
			symbolicallyGenerateTests(info);
		} else if (args[0].equalsIgnoreCase("help")) {
			System.out.println("No arguments: Assumes symbolic environment, runs plan to completion.");
			System.out.println("[number]: Assumes symbolic environment, runs plans for at most N steps.");
			System.out.println("Replay sequencefile.txt destination_dir/ ");
			System.out.println("    Replay sequences from sequence file, place resulting scripts in destination.");
			System.out.println("Random numTests maxNumSteps destination_dir");
			System.out.println("    Generate scripts using random values.");
			System.out.println("Filter class_file_location packageName");
			System.out.println("    Do coverage analysis. Finds Java classes in file location (probably bin/)");
			System.out.println("    inside given package (cannot contain dots). ");
			return;
		} else if (args[0].matches("[0-9]+")) { 
			STEP_LIMIT = Integer.parseInt(args[0]);
			symbolicallyGenerateTests(info);
		} else if (args[0].equalsIgnoreCase("Replay")) {
			createScripts(info, new File(args[1]), new File(args[2]));
		} else if (args[0].equalsIgnoreCase("Random")) {
			randomTesting(info, Integer.parseInt(args[1]), Integer.parseInt(args[2]), new File(args[3]));
		} else if (args[0].equalsIgnoreCase("Filter")) {
			File dir = new File(args[1]);
			String pkg = args[2];
			List<String> classNames = new ArrayList<String>();
			
			File packageDirectory = new File(dir, pkg);
			
			for (String clazz : packageDirectory.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".class");
				}
			})) {
				
				String rawName = clazz.replaceAll(".class$", "");
				classNames.add(pkg+"."+rawName);
			}
			
			coverageFilter(info, classNames);
		}
	}
	
	public static void randomTesting(TestGenerationInfo info, int numTests, int stepLimit, File destination) throws IOException {
		List<SymbolicScript> generated = new ArrayList<SymbolicScript>();
		
		while (generated.size() < numTests) {
			System.out.println("Generated "+generated.size()+" tests.");
			RandomValues rand = new RandomValues();
			SymbolicScript randScript = new SymbolicScript(info.createDecisionMaker(rand));
			JavaPlan plan = info.createPlanUnderTest(randScript);
			
			try {
				plan.runPlanToCompletion(stepLimit);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			
			if (isPrefix(randScript, generated)) continue;
			
			// Remove anything that this test case makes redundant. 
			removePrefixesOf(randScript, generated);
			generated.add(randScript);
		}
		
		writeScriptsToDirectory(generated, destination);
	}
	
	public static void coverageFilter(TestGenerationInfo info, List<String> classes) throws Exception {
		StateCoverageMeasurer coverage = new StateCoverageMeasurer();
		int currentCoverage = 0;
		
		for (String className : classes) {
			ExternalWorld world = (ExternalWorld) Class.forName(className).newInstance();
			JavaPlan plan = info.createPlanUnderTest(world);
			plan.addObserver(coverage);
			
			plan.runPlanToCompletion();
			
			int newCoverage = coverage.getNumStatesCovered();
			if (newCoverage > currentCoverage) {
				System.out.println("\nAdds to coverage: "+className);
				currentCoverage = coverage.getNumStatesCovered();
			} else {
				System.out.print(".");
			}
		}
		
		System.out.println("Finished. Covered states: "+currentCoverage);
		coverage.printData();
	}
	
	public static void symbolicallyGenerateTests(TestGenerationInfo info) {
		runSingleTest(info, info.createSymbolicValueSource(), false);
	}
	
	public static void createScripts(TestGenerationInfo info, File sequenceFile, File destination) throws IOException {
		createScripts(info, ReplayValues.parseSequenceFile(sequenceFile), destination);
	}
	
	public static void createScripts(TestGenerationInfo info, List<ReplayValues> testCases, File destination) throws IOException {
		List<SymbolicScript> allScripts = new ArrayList<SymbolicScript>();
		for (ReplayValues testCase : testCases) {
			SymbolicScript newCase = runSingleTest(info, testCase, true);
			// If this one is just a prefix, skip it.
			if (isPrefix(newCase, allScripts)) continue;
			
			// Remove anything that this test case makes redundant. 
			removePrefixesOf(newCase, allScripts);
			allScripts.add(newCase);
			System.out.println("Added a new script");
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
	
	private static SymbolicScript runSingleTest(TestGenerationInfo info, ValueSource values, boolean stopExceptions) {
		SymbolicDecisionMaker dm = info.createDecisionMaker(values);
		SymbolicScript world = new SymbolicScript(dm);
		JavaPlan plan = info.createPlanUnderTest(world);

		try {
			if (STEP_LIMIT > 0) {
				plan.runPlanToCompletion(STEP_LIMIT);
			} else {
				plan.runPlanToCompletion();
			}
		} catch (SimulatedBacktrackException e) {
			// All done, apparently! The path ended before we got to the end.
		}
		return world;
	}
	
	private PlexilDriver() {}

}
