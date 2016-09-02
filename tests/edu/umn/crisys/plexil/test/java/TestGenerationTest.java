package edu.umn.crisys.plexil.test.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import static org.junit.Assert.*;

import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.optimizations.HackOutArrayAssignments;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.jkind.results.JKindResultUtils;
import edu.umn.crisys.plexil.jkind.results.ScriptRecorderFromLustreData;
import edu.umn.crisys.plexil.jkind.search.IncrementalTrace;
import edu.umn.crisys.plexil.jkind.search.JKindSearch;
import edu.umn.crisys.plexil.jkind.search.JKindSettings;
import edu.umn.crisys.plexil.jkind.search.TraceProperty;
import edu.umn.crisys.plexil.main.TPlex;
import edu.umn.crisys.plexil.main.TPlex.OutputLanguage;
import edu.umn.crisys.plexil.runtime.plx.PlanState;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.test.java.RegressionTest.TestSuite;
import lustre.LustreTrace;

public class TestGenerationTest {

	@Test
	public void runQuickTestGeneration() {
		RegressionTest.getLustreTestSuites().forEach(this::testSingleSuite);
	}
	
	private void testSingleSuite(TestSuite suite)  {
		File plxPlan = new File(RegressionTest.RESOURCES, suite.planFile+".plx");

		OfficialPlexilExecutive template = new OfficialPlexilExecutive(plxPlan);
		
		TPlex tplex = new TPlex();
		tplex.files.add(plxPlan);
		for (String libFile : suite.libs) {
			File lib = new File(RegressionTest.RESOURCES, libFile+".plx");
			tplex.addToLibraryPath(lib);
			template.addLibrary(lib);
		}
		
		tplex.staticLibraries = true;
		tplex.inferTypes = true;
		// Just translate to IL, we'll take over for the rest
		tplex.outputLanguage = OutputLanguage.NONE;
		try {
			tplex.outputDir = Files.createTempDirectory(suite.planFile).toFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		assertTrue(tplex.execute());
		assertEquals(1, tplex.ilPlans.size());
		
		Plan ilPlan = tplex.ilPlans.values().stream().findFirst().get();
		
		// Push into Lustre
		HackOutArrayAssignments.hack(ilPlan);
		PlanToLustre p2l = new PlanToLustre(ilPlan);
		
		// Search for tests and check 'em
		TestSearcher searcher = new TestSearcher(p2l, template);
		searcher.addTransitionCoverageObligations();
		// Just do 1 round of searching, otherwise this will go on forever for
		// some of these. Give each test 20 seconds.
		searcher.turnOffIncrementalSearch();
		searcher.go(new JKindSettings(20, Integer.MAX_VALUE));
		
		searcher.testAllTheTests();
		
	}
	
	private static class TestSearcher extends JKindSearch {
		
		private Plan ilPlan;
		private OfficialPlexilExecutive template;
		private PlanToLustre translator;
		private List<LustreTrace> reEnumedVersions = new ArrayList<>();

		public TestSearcher(PlanToLustre translator, OfficialPlexilExecutive template) {
			super(translator);
			this.ilPlan = translator.getILPlan();
			this.translator = translator;
			this.template = template;
		}
		

		@Override
		public void newGoalFound(IncrementalTrace foundTrace, LustreTrace extendedTrace) {
			// Save for testing later
			reEnumedVersions.add(extendedTrace);
		}

		@Override
		public void redundantGoalFound(IncrementalTrace foundTrace, LustreTrace extendedTrace) {
			// Save for testing later
			reEnumedVersions.add(extendedTrace);
		}

		@Override
		public void noTestFound(Optional<IncrementalTrace> prefix, TraceProperty property) {
			// No test, so there's nothing to do.
		}
		
		public void testAllTheTests() {
			reEnumedVersions.forEach(this::testThisOne);
		}
		
		private void testThisOne(LustreTrace trace) {
			// Convert to a script
			Optional<PlexilScript> script = JKindResultUtils.translateToScript(
					ilPlan.getPlanName(), 
					trace, 
					translator.getTranslationMap(), 
					ilPlan);
			assertTrue(script.isPresent());

			// Write it somewhere
			File psxFile = ComplianceTesting.writeScriptToTempFile(script.get());
			// Get the executive oracle
			template.setScript(psxFile);
			List<PlanState> fromExecutive;
			try {
				fromExecutive = PlanState.parseLogFile(
						template.generateLog());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// That's everything we need
			ComplianceTesting.lustre(
					ilPlan, 
					new JavaPlexilScript(script.get()), 
					fromExecutive,
					ComplianceTesting.createLustreChecker(
							trace, 
							ilPlan, 
							translator.getTranslationMap()));
			
		}


		@Override
		public void goalFoundToBeImpossible(TraceProperty property) {
			// Nothing to do about that. 
		}
		
		
		
	}
}
