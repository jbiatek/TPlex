package edu.umn.crisys.plexil.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import jkind.api.results.JKindResult;
import jkind.lustre.Program;
import jkind.results.Counterexample;
import lustre.LustreTrace;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.ast2il.PlexilPlanToILPlan;
import edu.umn.crisys.plexil.ast2il.StaticLibIncluder;
import edu.umn.crisys.plexil.expr.ast.PlexilType;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILExprModifier;
import edu.umn.crisys.plexil.expr.il.ILType;
import edu.umn.crisys.plexil.expr.il.ILTypeChecker;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.optimizations.AssumeTopLevelPlan;
import edu.umn.crisys.plexil.il.optimizations.ConstantPropagation;
import edu.umn.crisys.plexil.il.optimizations.HackOutArrayAssignments;
import edu.umn.crisys.plexil.il.optimizations.PruneUnusedVariables;
import edu.umn.crisys.plexil.il.optimizations.RemoveDeadTransitions;
import edu.umn.crisys.plexil.il.optimizations.UnknownBiasing;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il2java.PlanToJava;
import edu.umn.crisys.plexil.il2java.StateMachineToJava;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre.Obligation;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.il2lustre.ScriptSimulation;
import edu.umn.crisys.plexil.jkind.results.JKindResultUtils;
import edu.umn.crisys.plexil.jkind.search.JKindSearch;
import edu.umn.crisys.plexil.jkind.search.JKindSettings;
import edu.umn.crisys.plexil.plx2ast.PlxParser;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.script.translator.ScriptParser;
import edu.umn.crisys.plexil.script.translator.ScriptToJava;
import edu.umn.crisys.plexil.script.translator.ScriptToXML;
import edu.umn.crisys.plexil.test.java.RegressionTest;

public class TPlex {
	
	
	@Parameter(names = {"--help", "-h"}, description = "Print this help message.", help = true)
	public boolean help = false;

	@Parameter(names = "--output-dir", description = "The directory to output files to. For Java, this is where the package will be placed.")
	public File outputDir = new File("./");
	
	@Parameter(description="FILE1.{plx|psx} [FILE2.{plx|psx} ...]", variableArity = true)
	public List<File> files = new ArrayList<>();
	
	@Parameter(names="--directory", variableArity = true,
			description = "Also include all files found in the given directories.",
			converter = FileConverter.class)
	public List<File> dirs = new ArrayList<>();
	
	@Parameter(names = "--lang", 
			description = "The language to translate to. 'none' can be used to just get reports from TPlex without producing code. 'plexil' can be used to turn a .plx file in to a more readable .ple file.")
	public OutputLanguage outputLanguage = OutputLanguage.NONE;
	
	@Parameter(names = "--static-libs", description="When translating libraries, first try to go through all the specified PLX files. If the correct ID is found, the library is included statically.")
	public boolean staticLibraries = false;
	
	@Parameter(names = "--no-optimizations", description="Disable all optimizations.")
	public boolean skipAllOptimizations = false;
	
	@Parameter(names = "--no-biasing", description="Disable the optimization which biases 3-valued logic expressions.")
	public boolean noBiasing = false;
	
	@Parameter(names = "--no-root-plans", description="Disable the optimization which removes some code from plans that look like they aren't going to be used as libraries. ")
	public boolean noGuessTopLevelPlans = false;
	

	@Parameter(names = "--infer-types", description="Use static analysis to try to fill in missing Lookup and Command declarations.")
	public boolean inferTypes = false;
	
	@Parameter(names = "--int-timepoints", description="When generating node timepoints, set the type as Integer. By default, they're assumed to be Real.")
	public boolean forceIntTimepoints = false;
	
	@Parameter(names = "--print-type-info", description="After any translating, print an analysis of Lookup and Command types.")
	public boolean analyzeTypes = false;
	
	@Parameter(names = "--print-reachable-states", description="After any translating, print an analysis of reachable PLEXIL states.")
	public boolean reachableStates = false;
	
	@Parameter(names = "--print-simulation", description="Print results of running all specified scripts against the first PLEXIL plan.")
	public boolean printSimulation = false;

	//Java-specific options
	@Parameter(names = "--java-package", 
			description = "Java package for generated files (default is empty)"
			)
	public String javaPackage = "";
	@Parameter(names = "--java-no-snapshot-method",
			description = "Don't generate the Java getSnapshot() method (useful for debugging and unit testing)")
	public boolean javaNoSnapshotMethod = false;
	@Parameter(names = "--java-no-debug", description="In Java, don't generate code that prints everything that happens to stdout.",
			arity = 1)
	public boolean javaNoDebugStatements = false;
	@Parameter(names = "--java-no-ternary", description="In Java, don't use the ternary operator (?:). This will disable short-circuiting of some AND and OR expressions.",
			arity = 1)
	public boolean javaNoTernaryOperator = false;

	
	//Lustre-specific options
	@Parameter(names = "--lustre-obligations", description = 
			"Generate test generation properties to cover Plexil states.")
	public PlanToLustre.Obligation lustreObligation = Obligation.NONE;
	
	@Parameter(names = {"--sim-lus-file", "--sim-lustre-file"}, description = 
			"Specify a Lustre file to be used when simulation is required. "
			+ "This is needed for --compliance-plx and for translating .csv "
			+ "files to PLEXILScript.")
	public File simLustreFile = null;
	@Parameter(names = "--compliance-plx", description =
			"Also requires --sim-lustre-file. When translating a .lus.xml file"
			+ " to PlexilScript, use this PLEXIL plan and the given Lustre file to"
			+ " ensure that the translated script exhibits the same behavior that"
			+ " it did in JKind/Lustre. If they differ, the errors will be printed. ")
	public File compliancePlexilProgram = null;
	
	@Parameter(names = "--lustre-plan-to-simulate", description = 
			"PLEXILScripts need to be translated via simulation, since Lustre input is at a "
			+ "microstep level. The plan should be passed in as a file to be translated, "
			+ "and also specified here as something like 'DriveToSchool'. The resulting "
			+ "execution will be output as a CSV file.")
	public String lustreSimulateScriptsAgainst = "";
	
	@Parameter(names = "--lustre-incremental", description = 
			"Generate test cases incrementally using JKind, putting any "
			+ "resulting test cases in the output directory. ")
	public boolean lustreIncrementalSearch = false;
	
	@Parameter(names = {"--depth", "-d"}, description = 
			"Set the depth limit for incremental search")
	public int lustreIncrementalDepth = 15;
	

	//Variables to use during translation
	public Map<String, PlexilPlan> asts = new HashMap<>();
	public Map<String, PlexilScript> scripts = new HashMap<>();
	public Map<String, Plan> ilPlans = new HashMap<>();
	public Map<String, ReverseTranslationMap> lustreTranslationMap = new HashMap<>();
	public Map<LustreTrace, PlexilScript> lustreResultsTranslated = new HashMap<>();
//	public Map<Plan, NodeToIL> originalTranslator = new HashMap<>();
//	public Map<Plan, PlexilPlan> originalAst = new HashMap<>();
	public Map<String, String> idToFile = new HashMap<>();
	
	/**
	 * Our own usage string, as set by JCommander/the main method. 
	 */
	private String usage;

	
	public static enum OutputLanguage {
		JAVA, LUSTRE, PLEXIL, NONE;
		
		public String toString() {
			return super.toString().toLowerCase();
		}
		
	}
	
	
	public static void main(String... args) {
		TPlex m = new TPlex();
		
		JCommander cmd = new JCommander(m);
		cmd.setProgramName("java -jar TPlex.jar");
		cmd.setColumnSize(75);
		
		StringBuilder usage = new StringBuilder();
		cmd.usage(usage);
		m.usage = usage.toString();

		try {
			cmd.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			return;
		}
		
		m.execute();
	}
	
	public boolean execute() {
		return  readOptions()
				&& parseInputFiles()
				&& translateToIL()
				&& optimizeIL()
				&& generateOutput()
				&& printReports();
	}

	public void printUsage(PrintStream out) {
		out.println(usage);
	}
	
	public boolean isRecognizedFile(String fileName) {
		return  fileName.endsWith(".plx") 
				|| fileName.endsWith(".psx")
				|| fileName.endsWith(".lus.xml")
				|| fileName.endsWith(".csv");
	}
	
	public boolean readOptions() {
		if (help) {
			printUsage(System.out);
			return false;
		}
		
		// Parse the --directories option into files
		for (File d  : dirs) {
			files.addAll(Arrays.asList(
					
					d.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return isRecognizedFile(name);
				}
			})));
		}
		if (forceIntTimepoints) {
			NodeToIL.TIMEPOINT_TYPE = PlexilType.INTEGER;
		}
		
		if (javaNoTernaryOperator) {
			ILExprToJava.SHORT_CIRCUITING = false;
		}
		if (javaNoDebugStatements) {
			StateMachineToJava.DEBUG_STATEMENTS = false;
		}
		
		
		// Check our list of all files
		for (File file : files) {
			if ( ! file.isFile()) {
				System.err.println("File "+file+" is not a standard file.");
				return false;
			} else if (file.getName().endsWith(".ple") || file.getName().endsWith(".pst")
					   || file.getName().endsWith(".pli")) {
				System.err.println("Error: "+file.getName()+" is not a compiled PLEXIL file.");
				System.err.println("Use plexilc to compile it to XML first.");
				return false;
			} else if ( ! isRecognizedFile(file.getName())) {
				System.err.println("Error: File extension of "+file+" is not recognized.");
				return false;
			}
		}
		
		if (files.size() == 0) {
			System.err.println("Warning: No files specified for translation.");
		}
		
		return true;
	}
	
	private String getKeyForPlx(File f) {
		return f.getName().replaceAll("\\.plx$", "");
	}
	
	private String getKeyForPsx(File f) {
		return f.getName().replaceAll("\\.psx$", "");
	}

	public boolean parseInputFiles() {
		for (File f : files) {
			if (f.getName().endsWith(".plx")) {
				try {
					asts.put(getKeyForPlx(f), PlxParser.parseFile(f));
				} catch (IOException e) {
					System.err.println(f+": "+e.getMessage());
					return false;
				} catch (XMLStreamException e) {
					System.err.println("XML Parsing error: "+e.getMessage());
					return false;
				} catch (FactoryConfigurationError e) {
					System.err.println("Error starting XML parser: "+e.getMessage());
					return false;
				}
			} else if (f.getName().endsWith(".psx")) {
				try {
					scripts.put(getKeyForPsx(f), ScriptParser.parse(f));
				} catch (FileNotFoundException e) {
					System.err.println(f+": "+e.getMessage());
					return false;
				} catch (XMLStreamException e) {
					System.err.println("XML Parsing error: "+e.getMessage());
					return false;
				} 
			} else if (f.getName().endsWith(".lus.xml")) {
				if (simLustreFile == null) {
					System.err.println("Error: A Lustre program must be supplied. Use --sim-lustre-file.");
					return false;
				}
				if (compliancePlexilProgram == null) {
					System.err.println("Error: A PLEXIL plx file must be supplied for compliance testing. Use --compliance-plx.");
				}
				try {
					JKindResult results = JKindResultUtils.parseJKindFile(f);
					Map<LustreTrace, PlexilScript> parsedScripts = JKindResultUtils.translateToScripts(
							JKindResultUtils.extractTraces(results, 
									JKindResultUtils.parseProgram(simLustreFile)), 
							getStringMapForXml(f), 
							quickParsePlan(compliancePlexilProgram))
							.entrySet().stream()
							.filter(e -> e.getValue().isPresent())
							.collect(Collectors.toMap(
									e -> e.getKey(), 
									e -> e.getValue().get()));
					System.out.println("Created "+parsedScripts.size()+" scripts from "+f);
					lustreResultsTranslated.putAll(parsedScripts);
				} catch (Exception e) {
					System.err.println("Error parsing JKind XML: ");
					e.printStackTrace();
					return false;
				}
			} else if (f.getName().endsWith(".csv")) {
				if (simLustreFile == null) {
					System.err.println("Error: A Lustre program must be supplied. Use --sim-lustre-file.");
					return false;
				}
				if (compliancePlexilProgram == null) {
					System.err.println("Error: A PLEXIL plx file must be supplied for compliance testing. Use --compliance-plx.");
				}
				try {
					List<LustreTrace> traces = JKindResultUtils.simulateCSV(
							simLustreFile, 
							getMainNodeFromFile(simLustreFile), 
							f);
					System.out.println("Simulated "+traces.size()+" test cases from "+f);
					// They all need names
					String baseName = f.getName().replaceAll("\\.csv$", "");
					Map<String,LustreTrace> namedTraces = new HashMap<>();
					for (int i = 0; i < traces.size(); i++) {
						namedTraces.put(baseName+"_"+i, traces.get(i));
					}
					Map<LustreTrace, PlexilScript> parsedScripts = 
							JKindResultUtils.translateToScripts(
									namedTraces, 
									getStringMapForLus(simLustreFile),
									quickParsePlan(compliancePlexilProgram))
							.entrySet().stream()
							.filter(e -> e.getValue().isPresent())
							.collect(Collectors.toMap(
									e -> e.getKey(), 
									e -> e.getValue().get()));
					System.out.println("Created "+parsedScripts.size()+" scripts from "+f);
					lustreResultsTranslated.putAll(parsedScripts);
				} catch (Exception e) {
					System.err.println("Error parsing Lustre input CSV: ");
					e.printStackTrace();
					return false;
				}
			}
		}
		// Now we can map file names to root node IDs. Since file names become 
		// Java class names, libraries need these to find the correct object
		// to create.
		idToFile = new HashMap<String, String>();
		for (String filename : asts.keySet()) {
			asts.get(filename).getRootNode().getPlexilID().ifPresent(
					(id) -> idToFile.put(id, NameUtils.clean(filename)));
		}	
		return true;
	}

	public boolean translateToIL() {
		for (String filename : asts.keySet()) {
			ilPlans.put(filename, translateToIL(asts.get(filename)));
		}
		return true;
	}
	
	public Plan translateToIL(PlexilPlan plan) {
		NodeToIL toIl = new NodeToIL(plan.getRootNode());

		if (inferTypes) {
			// Find lookups and commands without global declarations and
			// try to figure them out from context. 
			TypeAnalyzer ta = new TypeAnalyzer();
			ta.checkNode(plan.getRootNode());
			ta.inferTypeDeclarations(plan);
		}
		
		if (!skipAllOptimizations && staticLibraries) {
			StaticLibIncluder.optimize(toIl, new HashSet<>(asts.values()));
		}
		
		Plan ret =  PlexilPlanToILPlan.translate(plan);
		ILTypeChecker.typeCheck(ret);
		return ret;
	}

	public boolean optimizeIL() {
		if (skipAllOptimizations) return true;
		
		ilPlans.forEach((name, plan) -> optimizeIL(plan));
		
		return true;
	}
	
	public Plan optimizeIL(Plan ilPlan) {
		if (!noGuessTopLevelPlans 
				&& AssumeTopLevelPlan.looksLikeTopLevelPlan(ilPlan)) {
			System.out.println("I think "+ilPlan+" isn't a library, so I'm removing some code.");
			System.out.println("If I'm wrong, either add an interface to the orignal PLEXIL code or use \nthe \"--no-root-plans\" option.");
			AssumeTopLevelPlan.optimize(ilPlan);
		}


		ConstantPropagation.optimize(ilPlan);
		RemoveDeadTransitions.optimize(ilPlan);
		if (!noBiasing) {
			UnknownBiasing.optimize(ilPlan);
		}
		PruneUnusedVariables.optimize(ilPlan);
		
		ILTypeChecker.typeCheck(ilPlan);
		
		return ilPlan;
	}

	public boolean generateOutput() {
		if (outputLanguage != OutputLanguage.NONE) {
			outputDir.mkdirs();
		}
		switch(outputLanguage) {
		case NONE: 
			System.out.println("Files are not being produced, use --lang to change this.");
			return true;
		case JAVA: return generateJava();
		case LUSTRE: return generateLustre();
		case PLEXIL: return generatePlexil();
		}
		
		return true;
	}
	
	private boolean printString(String filename, String outString) {
		File output = new File(outputDir, filename);
		try {
			FileWriter fw = new FileWriter(output);
			fw.write(outString);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 
		return true;
	}

	private boolean generatePlexil() {
		for (Entry<LustreTrace, PlexilScript> e : lustreResultsTranslated.entrySet()) {
			// Print these PlexilScripts to output directory
			try {
				String filename = e.getValue().getScriptName()+".psx";
				System.out.println(filename);
				ScriptToXML.writeToFile(new File(outputDir, 
						filename), e.getValue());
			} catch (FileNotFoundException fnf) {
				System.err.println("Error writing PlexilScript to file: ");
				fnf.printStackTrace();
				return false;
			}
		}
		boolean scriptSuccess =  asts.entrySet().stream()
			.peek(entry -> System.out.println(entry.getKey()))
			.map(e -> printString(e.getKey()+".ple",e.getValue().getFullPrintout()))
			.allMatch(success -> success);
		
		return scriptSuccess;
	}

	private boolean generateLustre() {
		for (Entry<String, Plan> entry : ilPlans.entrySet()) {
			Plan p = entry.getValue();
			HackOutArrayAssignments.hack(p);
			
			PlanToLustre p2l = new PlanToLustre(p);
			
			if (! printString(p.getPlanName()+".lus", 
					p2l.toLustreAsString(lustreObligation))) {
				return false;
			}
			
			ReverseTranslationMap map = p2l.getTranslationMap();
			lustreTranslationMap.put(entry.getKey(), map);
			if (! printString(p.getPlanName()+".strings.txt",
					new Gson().toJson(map))) {
				return false;
			}
			if (lustreIncrementalSearch) {
				// Probably needs to return results at some point, or be
				// told where to put them. 
				JKindSearch searcher = new JKindSearch(p2l, new File(outputDir, "jkind-traces"));
				searcher.go(JKindSettings.createBMCOnly(
						Integer.MAX_VALUE, lustreIncrementalDepth));
			}
		}
		if ( ! scripts.isEmpty()) {
			if (lustreSimulateScriptsAgainst.equals("")) {
				System.err.println("No plan was specified to simulate scripts against!");
				return false;
			}
			Plan planToUse;
			try {
				planToUse = ilPlans.get(lustreSimulateScriptsAgainst);
			} catch (Exception e1) {
				System.err.println("Error getting plan for simulation "+lustreSimulateScriptsAgainst);
				System.err.println("Was it specified for translation? ");
				e1.printStackTrace();
				return false;
			}
			
			ScriptSimulation.DEBUG = true;
			ScriptedEnvironment.DEBUG = true;
			JavaPlan.DEBUG = true;
			for (Entry<String, PlexilScript> scriptEntry: scripts.entrySet()) {
				LinkedHashMap<ILExpr,List<PValue>> data = 
						ScriptSimulation.simulateToCSV(
								planToUse, scriptEntry.getValue());
				
				String csv = ScriptSimulation.toLustreCSV(data, 
						lustreTranslationMap.get(lustreSimulateScriptsAgainst));

				if ( ! printString(
						lustreSimulateScriptsAgainst+"__"+scriptEntry.getKey()+".csv",
						csv)) {
					return false;
				}
			}
			ScriptSimulation.DEBUG = false;
			ScriptedEnvironment.DEBUG = false;
			JavaPlan.DEBUG = false;
		}
		return true;
	}
	
	private boolean generateJava() {
		JCodeModel cm = new JCodeModel();
		for (Entry<String, Plan> entry: ilPlans.entrySet()) {
			Plan p = entry.getValue();
			JDefinedClass clazz = PlanToJava.toJava(p, cm, javaPackage, idToFile);
			if ( ! javaNoSnapshotMethod) {
				PlanToJava.addGetSnapshotMethod(p, clazz);
			} else {
				PlanToJava.addBlankSnapshotMethod(clazz);
			}
			
		}
		for (String filename : scripts.keySet()) {
			PlexilScript script = scripts.get(filename);
			try {
				ScriptToJava.toJava(script, cm, javaPackage);
			} catch (JClassAlreadyExistsException e) {
				e.printStackTrace();
				return false;
			}
		}
		// Write code to the output directory
		try {	
			cm.build(outputDir);
		} catch (IOException e) {
			System.err.println("Error writing Java code to output directory: "+e.getMessage());
			return false;
		} 
		return true;
	}

	public boolean printReports() {
		if (printSimulation) {
			// Do we have a plan to use? 
			if ( ilPlans.keySet().size() == 1) {
				// Use that one
				Plan ilPlan = ilPlans.entrySet().stream().findFirst().get().getValue();
				JavaPlan.DEBUG = true;
				ScriptedEnvironment.DEBUG = true;
				ILSimulator.LIMIT_TO_LUSTRE_FUNCTIONALITY = true;
				for (Entry<String, PlexilScript> entry : scripts.entrySet()) {
					System.out.println("Running script "+entry.getKey()
							+" against "+ilPlan.getPlanName());
					ILSimulator sim = new ILSimulator(ilPlan, 
							new JavaPlexilScript(entry.getValue()));
					sim.runPlanToCompletion();
				}
			} else {
				System.out.println("Didn't run simulations: "+ilPlans.keySet().size()
						+" plans were found. 1 is required.");
			}
		}
		
		if (analyzeTypes) {
			TypeAnalyzer all = new TypeAnalyzer();
			System.out.println();
			System.out.println("Type analysis:");
			System.out.println("--------------");
			for (String filename : asts.keySet()) {
				PlexilPlan plan = asts.get(filename);
				TypeAnalyzer analyzer = new TypeAnalyzer();
				analyzer.checkNode(plan.getRootNode());
				all.checkNode(plan.getRootNode());
				System.out.println("  "+filename+": ");
				analyzer.printAnalysis();
			}
			
			System.out.println("Info from all plans:");
			all.printAnalysis();
		}
		
		if (reachableStates) {
			for (Entry<String, Plan> entry: ilPlans.entrySet()) {
				Plan ilPlan = entry.getValue();
				int numStates = 0;
				for (NodeStateMachine machine : ilPlan.getMachines()) {
					numStates += machine.getStates().size();
				}
				System.out.println(ilPlan.getPlanName()+": states == "+numStates);
			}
		}
		if (compliancePlexilProgram != null) {
			try {
				doComplianceTesting();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		return true;

	}
	
	private static String getMainNodeFromFile(File lusFile) {
		return lusFile.getName().replaceFirst("\\.lus$", "");
	}
	
	public Plan quickParsePlan(File f) {
		try {
			return optimizeIL(translateToIL(PlxParser.parseFile(f)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	private void doComplianceTesting() throws Exception {
		// Both better be there
		if (simLustreFile == null) {
			System.err.println("Error: --sim-lustre-file not given");
			return;
		}
		if (compliancePlexilProgram == null) {
			System.err.println("Error: --compliance-plx not given");
			return;
		}
		if (lustreResultsTranslated.size() == 0 ) {
			System.out.println("No scripts to do compliance testing on!");
			return;
		}
		
		Program lustreProg = JKindResultUtils.parseProgram(simLustreFile);
		
		Plan ilPlan = quickParsePlan(compliancePlexilProgram);
		
		ReverseTranslationMap mapper = getStringMapForLus(simLustreFile);
		
		for (Entry<LustreTrace, PlexilScript> e : lustreResultsTranslated.entrySet()) {
			doLustreCompliance(lustreProg, ilPlan, mapper, e.getKey(), e.getValue());
		}
		
	}

	private void doLustreCompliance(Program lustreProg, Plan ilPlan,
			ReverseTranslationMap mapper,
			LustreTrace trace, PlexilScript plexilScript 
			) throws Exception {

		System.out.println("Testing translated script "+plexilScript.getScriptName());
		try {
			RegressionTest.complianceTest(ilPlan, 
					new JavaPlexilScript(plexilScript), 
					trace, mapper);
		} catch (Exception e) {
			System.err.println("Exception for script "+plexilScript.getScriptName());
			e.printStackTrace();
		}
	}
	
	public static ReverseTranslationMap getStringMapForLus(File lusFile) throws FileNotFoundException {
		String baseName = lusFile.getName().replaceAll("\\.lus$", "");
		File stringsFile = new File(lusFile.getParentFile(), 
				baseName+".strings.txt");
		if (stringsFile.isFile()) {
			try { 
				return new Gson()
					.fromJson(new FileReader(stringsFile), 
						ReverseTranslationMap.class);
			} catch (Exception e ) {
				System.err.println("Error parsing strings file "+stringsFile+":");
				e.printStackTrace();
			}
		}

		throw new FileNotFoundException("Error: Strings file "+stringsFile+" not found.");
	}
	
	public static ReverseTranslationMap getStringMapForXml(File lusXmlFile) throws FileNotFoundException {
		File lusFile = new File(lusXmlFile.getParentFile(), 
				lusXmlFile.getName().replaceAll("\\.xml$", ""));
		return getStringMapForLus(lusFile);
	}
	
}
