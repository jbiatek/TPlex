package edu.umn.crisys.plexil.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.optimize.AssumeTopLevelPlan;
import edu.umn.crisys.plexil.il.optimize.PruneUnusedTimepoints;
import edu.umn.crisys.plexil.il.optimize.RemoveDeadTransitions;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il2java.PlanToJava;
import edu.umn.crisys.plexil.il2java.StateMachineToJava;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.plx2ast.PlxParser;
import edu.umn.crisys.plexil.script.translator.ScriptParser;
import edu.umn.crisys.plexil.script.translator.ScriptToJava;

public class Main {
	
									 //  0         1         2         3         4         5         6         7         8
									 //  012345678901234567890123456789012345678901234567890123456789012345678901234567890
	private static final String usage = "Usage: java -jar TPlex.jar [options] FILE1 [FILE2...]\n"
									  + "    where FILEs are PSX or PLX files. \n"
									  + "Use --help or -h to see this help message.\n"
									  + "Options include:\n"
									  + "    --package com.example.something\n"
									  + "                                Put generated files in the given package\n"
									  + "                                (instead of the default empty package)\n\n"
									  
									  + "    --output-dir src/           Output files to the given directory. If a \n"
									  + "                                package is specified, the package will be placed\n"
									  + "                                in this directory \n"
									  + "                                (e.g. src/com/example/something/)\n"
									  + "                                If not specified, this defaults to the current\n"
									  + "                                directory.\n\n"
									  
									  + "    --no-optimizations          Disable various dead code removal optimizations.\n"
									  + "    --no-biasing                Disable boolean biasing, all expressions will \n"
									  + "                                use the PLEXIL logic library\n"
									  + "    --no-short-circuiting       Disable use of the conditional operator (?:) \n"
									  + "                                in AND and OR operations\n"
									  + "    --lib                       Disable optimization that guesses which plans\n"
									  + "                                are meant to be used as top level plans, and\n"
									  + "                                removes some code from them."
									  + "    --no-debug                  Disable optional JavaPlan.DEBUG statements\n"
									  + "    --no-plan-state             Disable method for generating plan snapshots\n"
									  + "    --just-parse                Just read in files, don't output to Java.\n"
									  + "                                Useful for just printing information.\n"
									  + "    --output-as-source          Any .plx will be written as PLEXIL source\n"
									  + "                                instead of Java code.\n"
									  + "    --print-type-info           Print an analysis of Lookup and Command types\n"
									  + "                                using some basic heuristics.\n"
									  + "    --print-reachable-states    Print the reachable PLEXIL states for each \n"
									  + "                                translated plan.";

	
	
	private Main() {}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("No arguments passed in.");
			System.out.println(usage);
			return;
		}
		
		File outputDir = new File(System.getProperty("user.dir"));
		String pkg = "";
		boolean optimize = true;
		boolean produceJava = true;
		boolean produceSourceCode = false;
		boolean createSnapshotMethod = true;
		boolean guessTopLevelPlans = true;
		boolean analyzeTypes = false;
		boolean reachableStates = false;
		List<File> files = new ArrayList<File>();
		
		// Some simple parsing of options.
		for (int i=0; i<args.length; i++) {
			if (args[i].startsWith("--")) {
				if (args[i].equals("--package")) {
					pkg = args[i+1];
					i++;
					continue;
				} else if (args[i].equals("--output-dir")) {
					outputDir = new File(args[i+1]);
					i++;
					continue;
				} else if (args[i].equals("--directory")) {
					File dir = new File(args[i+1]);
					i++;
					files.addAll(Arrays.asList(
							
							dir.listFiles(new FilenameFilter() {
						
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".plx") || name.endsWith(".psx");
						}
					})));
					
					continue;
				} else if (args[i].equals("--no-optimizations")) {
					optimize = false;
					continue;
				} else if (args[i].equals("--no-biasing")) { 
					StateMachineToJava.BIASING = false;
					continue;
				} else if (args[i].equals("--no-short-circuiting")) {
					ILExprToJava.SHORT_CIRCUITING = false;
					continue;
				} else if (args[i].equals("--no-debug")) {
					StateMachineToJava.DEBUG_STATEMENTS = false;
					continue;
				} else if (args[i].equals("--no-plan-state")) {
					createSnapshotMethod = false;
					continue;
				} else if (args[i].equals("--libs")) {
					guessTopLevelPlans = false;
					continue;
				} else if (args[i].equals("--just-parse")) {
					produceJava = false;
					continue;
				} else if (args[i].equals("--output-as-source")) {
					produceJava = false;
					produceSourceCode = true;
					continue;
				} else if (args[i].equals("--print-type-info")) {
					analyzeTypes = true;
					continue;
				} else if (args[i].equals("--print-reachable-states")) {
					reachableStates= true;
					continue;
				} else if (args[i].equals("-h") || args[i].equals("--help")) {
					System.out.println(usage);
					return;
				} else {
					System.err.println("Error: Unrecognized option "+args[i]+". Try --help.");
					return;
				}
			}
			// Must be a file.
			File file = new File(args[i]);
			if ( ! file.isFile()) {
				System.err.println("File "+args[i]+" is not a standard file.");
				return;
			} else if (file.getName().endsWith(".ple") || file.getName().endsWith(".pst")
					   || file.getName().endsWith(".pli")) {
				System.err.println("Error: "+file.getName()+" is not a compiled PLEXIL file.");
				System.err.println("Use plexilc to compile it to XML first.");
			} else if ( ! file.getName().endsWith(".plx") && ! file.getName().endsWith(".psx")) {
				System.err.println("Error: File extension of "+file+" is not recognized.");
			}
			files.add(file);
		}
		
		if (files.size() == 0) {
			System.err.println("Error: No files specified for translation.");
			for (String arg : args) {
				System.out.println(arg);
			}
			System.err.println(usage);
			return;
		}
		
		// Now we can translate.
		Map<String,PlexilPlan> asts = new HashMap<String, PlexilPlan>();
		JCodeModel cm = new JCodeModel();
		for (File f : files) {
			if (f.getName().endsWith(".plx")) {
				try {
					asts.put(f.getName().replaceAll("\\.plx$", ""), PlxParser.parseFile(f));
				} catch (FileNotFoundException e) {
					// This should have been checked before, but okay.
					System.err.println("Error: File not found: "+f);
					return;
				} catch (XMLStreamException e) {
					System.err.println("XML Parsing error: "+e.getMessage());
					return;
				} catch (FactoryConfigurationError e) {
					System.err.println("Error starting XML parser: "+e.getMessage());
					return;
				}
			} else if (f.getName().endsWith(".psx")) {
				try {
					PsxToJava(f, pkg, cm);
				} catch (FileNotFoundException e) {
					// This should have been checked before, but okay.
					System.err.println("Error: File not found: "+f);
					return;
				} catch (XMLStreamException e) {
					System.err.println("XML Parsing error: "+e.getMessage());
					return;
				} catch (JClassAlreadyExistsException e) {
					System.err.println("Tried to create two classes with the same name: "+f.getName());
					return;
				}
			}
		}
		// Now we can map file names to root node IDs. Since file names become 
		// Java class names, libraries need these to find the correct object
		// to create.
		Map<String,String> idToFile = new HashMap<String, String>();
		for (String filename : asts.keySet()) {
			idToFile.put(asts.get(filename).getRootNode().getPlexilID(), NameUtils.clean(filename));
		}
		
		Set<Plan> ilPlans = new HashSet<Plan>();
		// Finally, we translate each file into our JCodeModel.
		for (String filename : asts.keySet()) {
			PlexilPlan plan = asts.get(filename);
			NodeToIL toIl = new NodeToIL(plan.getRootNode());
			Plan ilPlan = new Plan(filename);
			ilPlans.add(ilPlan);
			toIl.translate(ilPlan);
			
			if (optimize) {
				if (guessTopLevelPlans && AssumeTopLevelPlan.looksLikeTopLevelPlan(plan)) {
					System.out.println("I think "+filename+" isn't a library, so I'm removing some code.");
					System.out.println("If I'm wrong, you can either add an interface or use the \"--libs\" option.");
					AssumeTopLevelPlan.optimize(ilPlan);
				}
				PruneUnusedTimepoints.optimize(ilPlan);
				RemoveDeadTransitions.optimize(ilPlan);
			}
			
			JDefinedClass clazz = PlanToJava.toJava(ilPlan, cm, pkg, idToFile);
			// Add the snapshot method too.
			if (createSnapshotMethod) {
				PlanToJava.addGetSnapshotMethod(ilPlan, toIl, clazz);
			}
		}
		
		// Now try to output the actual files
		if (produceJava) {
			outputDir.mkdirs();
			try {
				cm.build(outputDir);
			} catch (IOException e) {
				System.err.println("Error writing Java code to output directory: "+e.getMessage());
			}
		}
		
		if (produceSourceCode) {
			outputDir.mkdirs();
			for (String filename : asts.keySet()) {
				File outFile = new File(outputDir, filename+".ple");
				String text = asts.get(filename).getFullPrintout();
				try {
					FileWriter fw = new FileWriter(outFile);
					fw.write(text);
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				System.out.println(outFile.getName());
			}
		}
		
		// Yay, all done. Did they ask for a type analysis?
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
		
	}
	/*
	public static Plan PlxToIL(File f, boolean optimize) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		// Parse to AST
        PlexilPlan planXml = PlxParser.parseFile(f);
        // Wrap AST in translator
        NodeToIL translator = new NodeToIL(planXml.getRootNode());
        // Create empty IL plan
        Plan ilPlan = new Plan(f.getName().replaceAll("\\.plx$", ""));
        // Tell the wrapper to fill it with content.
        translator.translate(ilPlan);

        if (optimize) {
        	PruneUnusedTimepoints.optimize(ilPlan);
        	RemoveDeadTransitions.optimize(ilPlan);
        }
        
        return ilPlan;
	}
	
	public static void PlxToJava(File f, String pkg, JCodeModel cm, boolean optimize) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		Plan ilPlan = PlxToIL(f, optimize);
		// We're going to Java. (This will be more complicated once Java output
		// is separated from the IL.)
		PlanToJava.toJava(ilPlan, cm, pkg);
	}
	*/
	
	public static void PsxToJava(File f, String pkg, JCodeModel cm) throws FileNotFoundException, XMLStreamException, JClassAlreadyExistsException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLEventReader xml = factory.createXMLEventReader(new FileInputStream(f));
        
        String name = f.getName().replaceAll("\\.psx$", "") + "Script";
        ScriptToJava.toJava(ScriptParser.parse(name, xml), cm, pkg);

	}

}
