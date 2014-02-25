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

	private void writeTestToFile(int num, Vector<String> sequence) {
		File newTestFile = new File(testCaseDestination, "test"+num+".psx");
		Map<Integer, Set<String>> stepBucket = new HashMap<Integer, Set<String>>();
		for (String method : sequence) {
			if (method.startsWith("psxLogEntry(")) {
				putIntoBucket(method, stepBucket);
			}
		}
		
		try {
			PrintWriter pw = new PrintWriter(newTestFile);
			pw.println("<PLEXILScript>");
			// Handle initial state specially
			if (stepBucket.containsKey(-1)) {
				pw.println("<InitialState>");
				for (String method : stepBucket.get(-1)) {
					pw.println(extractXML(method));
				}
				pw.println("</InitialState>");
				stepBucket.remove(-1);
			}

			pw.println("<Script>");
			List<Integer> steps = new ArrayList<Integer>(stepBucket.keySet());
			Collections.sort(steps);
			
			
			for (Integer i : steps) {
				boolean simultaneous = false;
				if (stepBucket.get(i).size() > 1) {
					simultaneous = true;
				}
				
				if (simultaneous) {
					pw.println("<Simultaneous>");
				}
				
				for (String method : stepBucket.get(i)) {
					pw.println(extractXML(method));
				}
				
				if (simultaneous) {
					pw.println("</Simultaneous>");
				}
			}
			pw.println("</Script>\n</PLEXILScript>");
			
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
