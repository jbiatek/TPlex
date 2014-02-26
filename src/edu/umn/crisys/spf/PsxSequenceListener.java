package edu.umn.crisys.spf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.report.Publisher;

public class PsxSequenceListener extends SymbolicSequenceListener {

	private File testCaseDestination;
	
	public PsxSequenceListener(Config conf, JPF jpf) {
		super(conf, jpf);
		String dest = conf.getString("psxlistener.output_dir", "psx_output");
		testCaseDestination = new File(dest);
	}

	@Override
	public void publishFinished(Publisher publisher) {
		//super.publishFinished(publisher);
		testCaseDestination.mkdirs();
		int currentTest = 0;
		for (Vector<String> sequence : this.methodSequences) {
			writeTestToFile(currentTest, sequence);
			currentTest++;
		}
		publisher.getOut().println("Wrote "+currentTest+" tests to "+testCaseDestination);
		
	}
	
	private String convertMethodToXML(String str) {
		String[] args = splitMethod(str);
		if (str.startsWith("psxUpdateAck")) {
			return interpolate(args, "<UpdateAck name=\"", "\" />");
		} else if (str.startsWith("psxCommand")) {
			return interpolate(args, "<Command"," name=\"", "\" type=\"", "\">");
		} else if (str.startsWith("psxEndCommand")) {
			return interpolate(args, "</Command", ">");
		} else if (str.startsWith("psxResultOrValue")) {
			return "<"+args[0]+">"+args[1]+"</"+args[0]+">";
		} else if (str.startsWith("psxState")) {
			return interpolate(args, "<State name=\"", "\" type=\"", "\">");
		} else if (str.startsWith("psxEndState")) {
			return "</State>";
		} else if (str.startsWith("psxParam")) {
			return interpolate(args, "<Param type=\"", "\">", "</Param>");
		} else if (str.startsWith("psxInitialStateEnd")) {
			return "</InitialState>\n<Script>";
		} else if (str.startsWith("psxSimultaneousStart")) {
			return "<Simultaneous>";
		} else if (str.startsWith("psxSimultaneousEnd")) {
			return "</Simultaneous>";
		} else {
			throw new RuntimeException(str+" could not be converted.");
		}
	}
	
	private String[] splitMethod(String method) {
		// Delete everything up to and including the open paren,
		// then everything after the close paren.
		String[] args = method.replaceFirst("^.*\\(", "")
					 .replaceFirst("\\).*$", "")
					 .split(",");
		for (int i=0; i<args.length; i++) {
			if (args[i].startsWith("\"")) {
				args[i] = args[i].substring(1);
			}
			if (args[i].endsWith("\"")) {
				args[i] = args[i].substring(0, args[i].length()-1);
			}
		}
		return args;
	}
	
	private String interpolate(String[] args, String...chunks) {
		if (args.length != chunks.length
				&& args.length != chunks.length-1) {
			throw new RuntimeException("Wrong number of args and chunks");
		}
		
		StringBuilder b = new StringBuilder("");
		for (int i=0; i<args.length; i++) {
			b.append(chunks[i]+args[i]);
		}
		if (chunks.length > args.length) {
			b.append(chunks[chunks.length-1]);
		}
		return b.toString();
	}
	
	private void writeTestToFile(int num, Vector<String> sequence) {
		File newTestFile = new File(testCaseDestination, "test"+num+".psx");
		List<String> xmlToOutput = new ArrayList<String>();
		for (String method : sequence) {
			if (method.startsWith("psx")) {
				xmlToOutput.add(convertMethodToXML(method));
			}
		}
		
		try {
			PrintWriter pw = new PrintWriter(newTestFile);
			pw.println("<PLEXILScript>");
			pw.println("<InitialState>");
			
			for (String xml : xmlToOutput) {
				pw.println(xml);
			}
			pw.println("</Script>");
			pw.println("</PLEXILScript>");
			
			pw.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void putIntoBucket(String method,
			Map<Integer, Set<String>> stepBucket) {
		String num = method.substring("psxLogEntry(".length(), method.indexOf(','));
		Integer realNum = Integer.parseInt(num);
		if ( ! stepBucket.containsKey(realNum)) {
			stepBucket.put(realNum, new HashSet<String>());
		}
		stepBucket.get(realNum).add(method);
		
	}
	
	private String extractXML(String method) {
		// Delete everything up to and including the first quote,
		// then everything after the last one.
		return method.replaceFirst("^[^\"]*\"", "")
					 .replaceFirst("\"[^\"]*$", "");
	}
	
}
