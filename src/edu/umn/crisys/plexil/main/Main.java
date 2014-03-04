package edu.umn.crisys.plexil.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;

import edu.umn.crisys.plexil.ast.core.PlexilPlan;
import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.optimize.PruneUnusedTimepoints;
import edu.umn.crisys.plexil.il.optimize.RemoveDeadTransitions;
import edu.umn.crisys.plexil.il2java.PlanToJava;
import edu.umn.crisys.plexil.plx2ast.PlxParser;
import edu.umn.crisys.plexil.psx2java.PsxParser;

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
									  
									  + "    --no-optimizations          Disable removal of impossible transitions and \n"
									  + "                                unused node timepoints.\n";

	
	
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
				} else if (args[i].equals("--no-optimizations")) {
					optimize = false;
				} else if (args[i].equals("-h") || args[i].equals("--help")) {
					System.out.println(usage);
					return;
				} else {
					System.err.println("Error: Unrecognized option "+args[i]+". Try --help.");
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
		JCodeModel cm = new JCodeModel();
		for (File f : files) {
			if (f.getName().endsWith(".plx")) {
				try {
					PlxToJava(f, pkg, cm, optimize);
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
		
		outputDir.mkdirs();
		try {
			cm.build(outputDir);
		} catch (IOException e) {
			System.err.println("Error writing Java code to output directory: "+e.getMessage());
		}
		
	}
	
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
		PlanToJava.toJava(ilPlan, cm, pkg, false);
	}
	
	public static void PsxToJava(File f, String pkg, JCodeModel cm) throws FileNotFoundException, XMLStreamException, JClassAlreadyExistsException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLEventReader xml = factory.createXMLEventReader(new FileInputStream(f));
        
        String name = f.getName().replaceAll("\\.psx$", "") + "Script";
        PsxParser.parse(name, xml).toJava(cm, pkg);

	}

}
