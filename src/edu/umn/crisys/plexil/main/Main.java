package edu.umn.crisys.plexil.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import jkind.lustre.Program;
import jkind.lustre.visitors.PrettyPrintVisitor;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.ast2il.PlexilPlanToILPlan;
import edu.umn.crisys.plexil.ast2il.StaticLibIncluder;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.optimizations.AssumeTopLevelPlan;
import edu.umn.crisys.plexil.il.optimizations.ConstantPropagation;
import edu.umn.crisys.plexil.il.optimizations.HackOutArrayAssignments;
import edu.umn.crisys.plexil.il.optimizations.PruneUnusedVariables;
import edu.umn.crisys.plexil.il.optimizations.RemoveDeadTransitions;
import edu.umn.crisys.plexil.il.optimizations.UnknownBiasing;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il2java.PlanToJava;
import edu.umn.crisys.plexil.il2java.StateMachineToJava;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre.Obligation;
import edu.umn.crisys.plexil.jkind.results.JKindXmlParser;
import edu.umn.crisys.plexil.plx2ast.PlxParser;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.script.translator.ScriptParser;
import edu.umn.crisys.plexil.script.translator.ScriptToJava;
import edu.umn.crisys.plexil.script.translator.ScriptToXML;

public class Main {
	
	@Parameter(names = {"--help", "-h"}, description = "Print this help message.", help = true)
	public boolean help = false;

	@Parameter(names = "--output-dir", description = "The directory to output files to. For Java, this is where the package will be placed.")
	public File outputDir = new File("./");
	
	@Parameter(description="FILE1.{plx|psx} [FILE2.{plx|psx} ...]", variableArity = true)
	public List<File> files = new ArrayList<>();
	
	@Parameter(names="--directory", variableArity = true,
			description = "Also include all PLX and PSX files found in the given directories.",
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
	
	@Parameter(names = "--int-timepoints", description="When generating node timepoints, set the type as Integer. By default, they're assumed to be Real.")
	public boolean forceIntTimepoints = false;
	
	@Parameter(names = "--print-type-info", description="After any translating, print an analysis of Lookup and Command types.")
	public boolean analyzeTypes = false;
	
	@Parameter(names = "--print-reachable-states", description="After any translating, print an analysis of reachable PLEXIL states.")
	public boolean reachableStates = false;

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
	public PlanToLustre.Obligation obligation = Obligation.NONE;
	
	@Parameter(names = "--lustre-to-scripts", description = 
			"Translate a JKind .xml file with counterexamples to the output directory as PlexilScript.")
	public String lustreResultsToTranslate = "";
	

	//Variables to use during translation
	public Map<String, PlexilPlan> asts = new HashMap<>();
	public Map<String, PlexilScript> scripts = new HashMap<>();
	public Set<Plan> ilPlans = new HashSet<>();
	public Map<Plan, NodeToIL> originalTranslator = new HashMap<>();
	public Map<Plan, PlexilPlan> originalAst = new HashMap<>();
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
		Main m = new Main();
		
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
				&& parsePlexilFiles()
				&& translateToIL()
				&& optimizeIL()
				&& generateOutput()
				&& printReports();
	}

	public void printUsage(PrintStream out) {
		out.println(usage);
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
					return name.endsWith(".plx") || name.endsWith(".psx");
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
			} else if ( ! file.getName().endsWith(".plx") && ! file.getName().endsWith(".psx")) {
				System.err.println("Error: File extension of "+file+" is not recognized.");
				return false;
			}
		}
		
		if (files.size() == 0 && lustreResultsToTranslate.equals("")) {
			System.err.println("Warning: No files specified for translation.");
		}
		return true;
	}

	public boolean parsePlexilFiles() {
		for (File f : files) {
			if (f.getName().endsWith(".plx")) {
				try {
					asts.put(f.getName().replaceAll("\\.plx$", ""), PlxParser.parseFile(f));
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
					scripts.put(f.getName(), ScriptParser.parse(f));
				} catch (FileNotFoundException e) {
					System.err.println(f+": "+e.getMessage());
					return false;
				} catch (XMLStreamException e) {
					System.err.println("XML Parsing error: "+e.getMessage());
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
			PlexilPlan plan = asts.get(filename);
			NodeToIL toIl = new NodeToIL(plan.getRootNode());

			if (!skipAllOptimizations && staticLibraries) {
				StaticLibIncluder.optimize(toIl, new HashSet<>(asts.values()));
			}
			
			Plan ilPlan = PlexilPlanToILPlan.translate(plan);
			ilPlans.add(ilPlan);
			originalTranslator.put(ilPlan, toIl);
			originalAst.put(ilPlan, plan);
			
		}
		return true;
	}

	public boolean optimizeIL() {
		if (skipAllOptimizations) return true;
		
		for (Plan ilPlan : ilPlans) {
			if (!noGuessTopLevelPlans 
					&& AssumeTopLevelPlan.looksLikeTopLevelPlan(originalAst.get(ilPlan))) {
				System.out.println("I think "+ilPlan+" isn't a library, so I'm removing some code.");
				System.out.println("If I'm wrong, either add an interface to the orignal PLEXIL code or use \nthe \"--no-root-plans\" option.");
				AssumeTopLevelPlan.optimize(ilPlan);
			}


			if (!noBiasing) {
				UnknownBiasing.optimize(ilPlan);
			}
			PruneUnusedVariables.optimize(ilPlan);
			RemoveDeadTransitions.optimize(ilPlan);
			ConstantPropagation.optimize(ilPlan);
		}
		return true;
	}

	public boolean generateOutput() {
		if (outputLanguage != OutputLanguage.NONE) {
			outputDir.mkdirs();
		}
		switch(outputLanguage) {
		case NONE: return true;
		case JAVA: return generateJava();
		case LUSTRE: return generateLustre();
		case PLEXIL: return generatePlexil();
		}
		
		return true;
	}

	private boolean generatePlexil() {
		for (String filename : asts.keySet()) {
			File outFile = new File(outputDir, filename+".ple");
			String text = asts.get(filename).getFullPrintout();
			try {
				FileWriter fw = new FileWriter(outFile);
				fw.write(text);
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			System.out.println(outFile.getName());
		}
		
		return true;
	}

	private boolean generateLustre() {
		for (Plan p : ilPlans) {
			HackOutArrayAssignments.hack(p);
			
			PlanToLustre p2l = new PlanToLustre(p);
			Program lustre = p2l.toLustre(obligation);
			PrettyPrintVisitor pp = new PrettyPrintVisitor();
			lustre.accept(pp);
			
			File output = new File(outputDir, p.getPlanName()+".lus");
			try {
				FileWriter fw = new FileWriter(output);
				fw.write(pp.toString());
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} 
			
			p2l.getStringMap().ifPresent((map) -> {
				File stringOut = new File(outputDir, p.getPlanName()+".strings.txt");
				String json = new Gson().toJson(map);
				try {
					FileWriter fw = new FileWriter(stringOut);
					fw.write(json);
					fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
		}
		return true;
	}

	private boolean generateJava() {
		JCodeModel cm = new JCodeModel();
		for (Plan p : ilPlans) {
			JDefinedClass clazz = PlanToJava.toJava(p, cm, javaPackage, idToFile);
			if ( ! javaNoSnapshotMethod) {
				PlanToJava.addGetSnapshotMethod(p, clazz);
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
			for (Plan ilPlan : ilPlans) {
				int numStates = 0;
				for (NodeStateMachine machine : ilPlan.getMachines()) {
					numStates += machine.getStates().size();
				}
				System.out.println(ilPlan.getPlanName()+": states == "+numStates);
			}
		}
		
		if ( ! lustreResultsToTranslate.equals("")) {
			translateLustreResults();
		}

		return true;

	}
	
	private void translateLustreResults() {
		File lustreXml = new File(lustreResultsToTranslate);
		if (lustreXml.isFile()) {
			Optional<Map<String,StringValue>> stringMap = getStringMapFor(lustreXml);
			List<PlexilScript> parsedScripts = new ArrayList<PlexilScript>();
			
			
			try {
				XMLEventReader xml = XMLInputFactory.newInstance().createXMLEventReader(
						new FileReader(lustreXml));
				parsedScripts = JKindXmlParser.translateToScripts(xml, stringMap);
			} catch (Exception e) {
				System.err.println("Error parsing JKind XML file: ");
				e.printStackTrace();
				return;
			}
			
			
			outputDir.mkdirs();
			for (PlexilScript script : parsedScripts) {
				try {
					String filename = script.getScriptName()+".psx";
					System.out.println(filename);
					ScriptToXML.writeToFile(new File(outputDir, 
							filename), script);
				} catch (FileNotFoundException e) {
					System.err.println("Error writing PlexilScript to file: ");
					e.printStackTrace();
					return;
				}
			}
		} else {
			throw new RuntimeException(
					new FileNotFoundException(lustreResultsToTranslate));
		}

	}
	
	private Optional<Map<String,StringValue>> getStringMapFor(File lustreXml) {
		String baseName = lustreXml.getName().replaceAll("\\.lus\\.xml$", "");
		File stringsFile = new File(lustreXml.getParentFile(), 
				baseName+".strings.txt");
		if (stringsFile.isFile()) {
			try { 
				Map<String,StringValue> map = new Gson()
					.fromJson(new FileReader(stringsFile), 
						new TypeToken<Map<String,StringValue>>(){}.getType());
				return Optional.of(map);
			} catch (Exception e ) {
				System.err.println("Error parsing strings file "+stringsFile+":");
				e.printStackTrace();
			}
		}

		System.out.println("Warning: Strings file "+stringsFile+" not found.");
		return Optional.empty();
	}
	
}
