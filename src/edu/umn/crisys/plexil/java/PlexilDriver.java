package edu.umn.crisys.plexil.java;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.psx.symbolic.ReplayValues;
import edu.umn.crisys.plexil.java.psx.symbolic.SymbolicDecisionMaker;
import edu.umn.crisys.plexil.java.psx.symbolic.SymbolicScript;
import edu.umn.crisys.plexil.java.psx.symbolic.ValueSource;

public class PlexilDriver {
	
	public static int STEP_LIMIT = -1;
	
	public static void mainMethod(TestGenerationInfo info, String[] args) throws IOException {
		if (args.length == 3 && args[0].equalsIgnoreCase("Replay")) {
			createScripts(info, new File(args[1]), new File(args[2]));
		} else {
			generateTests(info);
		}
	}
	
	public static void generateTests(TestGenerationInfo info) {
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
		}
		
		int counter = 0;
		destination.mkdirs();
		for (SymbolicScript fullCase : allScripts) {
			fullCase.writeToXML(new PrintWriter(new File(destination, "test"+counter+".psx")));
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
		} catch (RuntimeException e) {
			if (stopExceptions) {
				e.printStackTrace();
				return world;
			} else {
				throw e;
			}
		}
		return world;
	}
	
	private PlexilDriver() {}

}
