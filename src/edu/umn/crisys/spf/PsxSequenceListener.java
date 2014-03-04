package edu.umn.crisys.spf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import edu.umn.crisys.util.Pair;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.report.Publisher;

public class PsxSequenceListener extends SymbolicSequenceListener {

	private File testCaseDestination;
	private boolean writeRedundantCasesToo = false;
	private Set<List<String>> testCases = new HashSet<List<String>>();
	private Set<List<String>> redundantTestCases = new HashSet<List<String>>();
	private boolean outputDebugComments = false;
	
	public PsxSequenceListener(Config conf, JPF jpf) {
		super(conf, jpf);
		String dest = conf.getString("psxlistener.output_dir", "psx_output");
		writeRedundantCasesToo = conf.getBoolean("psxlistener.output_all_tests", false);
		outputDebugComments  = conf.getBoolean("output_debug_comments", false);
		testCaseDestination = new File(dest);
	}

	@Override
	public void publishFinished(Publisher publisher) {
		//super.publishFinished(publisher);
		// We're actually mostly going to spit out test case files.
		testCaseDestination.mkdirs();
		
		for (Vector<String> methodSequence : this.methodSequences) {
			// This sanitizes the method sequence by removing unclosed "tags"
			cleanUpMethods(methodSequence);
			// Now we can convert to XML,
			List<String> xmlTestCase = convertMethodsToScript(methodSequence);
			if ( ! alreadyHaveThis(xmlTestCase)) {
				// This test case has new stuff. It might even include other 
				// test cases inside it, so move those.
				removePrefixesOf(xmlTestCase);
				testCases.add(xmlTestCase);
			} else {
				// This one's redundant, we already have it.
				redundantTestCases.add(xmlTestCase);
			}
		}
		
		// Now it's time to write XML to files.
		int currentTest = 0;
		int redundantTest = 0;
		for (List<String> xml : testCases) {
			File newTestFile = new File(testCaseDestination, "test"+currentTest+".psx");
			writeToFile(xml, newTestFile);
			currentTest++;
		}
		if (writeRedundantCasesToo) {
			for (List<String> xml : redundantTestCases) {
				File newTestFile = new File(testCaseDestination, "redundant_test"+redundantTest+".psx");
				writeToFile(xml, newTestFile);
				redundantTest++;
			}
		}
		
		// Write a nice note to the JPF console.
		publisher.getOut().println("Wrote "+currentTest+" tests to "+testCaseDestination);
		if (writeRedundantCasesToo) {
			publisher.getOut().println("Also wrote "+redundantTest+" redundant tests.");
		}
		
	}
	
	private void writeToFile(List<String> lines, File newTestFile) {
		try {
			PrintWriter pw = new PrintWriter(newTestFile);
			for (String line : lines) {
				pw.println(line);
			}
			pw.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
    /**
     * Check for prefixes while skipping anything that starts with "&lt!--". 
     * @param first
     * @param second
     * @return
     */
    private boolean firstIsPrefixXML(List<String> first, List<String> second) {
    	int indexFirst = 0;
    	int indexSecond = 0;
    	while (indexFirst < first.size() && indexSecond < second.size()) {
    		if ( ! first.get(indexFirst).equals(second.get(indexSecond))) {
    			// First has something different from second. It's not a prefix.
    			return false;
    		}
    		indexFirst++;
    		indexSecond++;
    		
    		// In this class, we always start with a tag, so we can get away
    		// with skipping comments afterward.
    		while (indexFirst < first.size() && 
    				first.get(indexFirst).startsWith("<!--")) {
    			indexFirst++;
    		}
    		while (indexSecond < second.size() 
    				&& second.get(indexSecond).startsWith("<!--")) {
    			indexSecond++;
    		}
    	}
    	// Okay, they both matched until one of them ended. If first has no more
    	// content, it must be a prefix. (Or it's exactly the same, but 
    	// technically that's a prefix too. Either way it's redundant.)
    	if (indexFirst == first.size()) {
    		return true;
    	}
    	// Ah, the second one ran out. That one's the prefix, not first.
    	return false;
    }

	
	private boolean alreadyHaveThis(List<String> xml) {
		for (List<String> testCase : testCases) {
			if (firstIsPrefixXML(xml, testCase)) {
				return true;
			}
		}
		return false;
	}
	
	private void removePrefixesOf(List<String> xml) {
		Iterator<List<String>> iter = testCases.iterator();
		while (iter.hasNext()) {
			List<String> existing = iter.next();
			if (firstIsPrefixXML(existing, xml)) {
				redundantTestCases.add(existing);
				iter.remove();
			}
		}
	}
	
	private void cleanUpMethods(List<String> methods) {
		// Some of these "tags" need to be closed, but if the depth limit gets
		// hit before they get a chance to, we're left with incomplete data.
		// This finds any unclosed "tags" and removes them (and everything after
		// them).
		LinkedList<Pair<String,Integer>> stack = new LinkedList<Pair<String,Integer>>();
		boolean initialStateWasClosed = false;
		
		for (int i=0; i<methods.size(); i++) {
			String method = methods.get(i);
			if (method.startsWith("psxUpdateAck")
					|| method.startsWith("psxResultOrValue")
					|| method.startsWith("psxParam")) {
				// Nothing needs to be done, these close themselves. 
			} else if (method.startsWith("psxCommand")) {
				// The full tag name is CommandBlah, where Blah=args[0].
				String[] args = splitMethod(method);
				stack.push(new Pair<String,Integer>("Command"+args[0], i));
			} else if (method.startsWith("psxEndCommand")) {
				// Again, full name. 
				String[] args = splitMethod(method);
				String expected = "Command"+args[0];
				Pair<String, Integer> start = stack.pop();
				if ( ! start.first.equals(expected)) {
					// This tag shouldn't have ended... The ExternalWorld
					// messed something up.
					throw new RuntimeException(expected+" end tag inserted in the wrong place.");
				}
			} else if (method.startsWith("psxState")) {
				stack.push(new Pair<String, Integer>("State", i));
			} else if (method.startsWith("psxEndState")) {
				Pair<String, Integer> start = stack.pop();
				if ( ! start.first.equals("State")) {
					// This tag shouldn't have ended... The ExternalWorld
					// messed something up.
					throw new RuntimeException("State end tag inserted in the wrong place.");
				}
			} else if (method.startsWith("psxInitialStateEnd")) {
				initialStateWasClosed = true;
			} else if (method.startsWith("psxSimultaneousStart")) {
				stack.push(new Pair<String, Integer>("Simultaneous", i));
			} else if (method.startsWith("psxSimultaneousEnd")) {
				Pair<String, Integer> start = stack.pop();
				if ( ! start.first.equals("Simultaneous")) {
					// This tag shouldn't have ended... The ExternalWorld
					// messed something up.
					throw new RuntimeException("Simultaneous tag inserted in the wrong place.");
				}
			} else if (method.startsWith("psx")) {
					throw new RuntimeException(method+" is unknown...");
				// If it doesn't start with "psx", then it's something we don't
				// care about.
			}
		}
		
		// Anything left on the stack is unclosed. But if the outermost one was
		// a Simultaneous tag, we can close it up, that's fine. But we have to
		// add it *after* other stuff gets purged.
		boolean stickOnSimultaneousEnd = false;
		if (stack.size() > 0 && stack.getLast().first.equals("Simultaneous")) {
			stickOnSimultaneousEnd = true;
			stack.removeLast();
		}
		
		// If anything is left in the stack, it hasn't been closed, and we 
		// might be looking at incomplete data. The XML
		// converter comments out anything that doesn't start with "psx"
		// so instead of deleting, we just add some words. That way we can
		// leave that stuff in for debugging purposes.
		if (stack.size() > 0) {
			int firstUnclosedTag = stack.getLast().second;
			for (int i=firstUnclosedTag; i<methods.size(); i++) {
				methods.set(i, "Part of unfinished action: "+methods.get(i));
			}
		}
		
		if (stickOnSimultaneousEnd) {
			methods.add("psxSimultaneousEnd()");
		}
		
		if ( ! initialStateWasClosed) {
			// Hmm. This is probably a useless test case if we got cut short
			// before the initial state even finished. But we can fix this,
			// I guess.
			methods.add("psxInitialStateEnd()");
		}
	}

	private List<String> convertMethodsToScript(List<String> sequence) {
		List<String> list = new ArrayList<String>();
		list.add("<PLEXILScript>");
		list.add("<InitialState>");
		for (String method : sequence) {
			String xml = convertMethodToXML(method);
			if ( ! xml.equals("")) {
				list.add(convertMethodToXML(method));
			}
		}
		list.add("</Script>");
		list.add("</PLEXILScript>");
		
		
		return list;
	}
	
	/**
	 * Transform a SymbolicExternalWorld psxSomething() call into XML.
	 * 
	 * @param str
	 * @return
	 */
	private String convertMethodToXML(String str) {
		/*
		 * This is probably a horrible hack, but other, prettier stuff didn't work.
		 * Here's what's happening:
		 * 
		 *   - SymbolicExternalWorld does its normal job, pulling symbolic 
		 *     values out of thin air and giving them back to the PLEXIL plan
		 *     as data. 
		 *   - As it goes, it calls its own methods like 
		 *     psxUpdateAck("someNodeName"). These correspond with PLEXILScript
		 *     XML tags, with the arguments filling in some blanks.
		 *   - These methods are marked as symbolic, so SPF picks up on them
		 *     and it gives them to us. All the arguments are concrete, of 
		 *     course, so it's just passing information back to us.
		 *   - Now it's our job in PsxSequenceListener to take that information,
		 *     fill in the XML blanks, and print it to a file. Ta-dah!
		 * 
		 * Originally, I wanted to just have SymbolicExternalWorld just 
		 * pass back XML, but that didn't work. I think symbolic values getting
		 * concatenated into strings broke it. So now everything gets put
		 * as its own argument. 
		 */
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
		} else if (str.startsWith("psx")) {
			throw new RuntimeException(str+" could not be converted.");
		} else {
			if (outputDebugComments) {
				return "<!--"+str+"-->";
			} else {
				return "";
			}
		}
	}
	
	/**
	 * Given a string "foo(\"arg1\",arg2,\"arg3\")", returns 
	 * {"arg1", "arg2", "arg3"}. The parens are chopped off, it's
	 * split by comma (not comma space, just a comma) and if the argument is 
	 * quoted the quotes are removed. 
	 * @param method
	 * @return
	 */
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
	
	/**
	 * Spread args inbetween chunks. Chunks get added first. For example:
	 * 
	 * interpolate(new String[]{"1","3","5"}, "0","2","4","6")
	 * 
	 * returns "0123456". 
	 * 
	 * The array HAS TO be either the same length as chunks, or one less. 
	 * @param args
	 * @param chunks
	 * @return
	 */
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

}
