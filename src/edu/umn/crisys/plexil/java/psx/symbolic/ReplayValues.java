package edu.umn.crisys.plexil.java.psx.symbolic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ReplayValues implements ValueSource {
	
	public static List<ReplayValues> parseSequenceFile(File file) throws IOException {
		return parseSequenceFile(new FileReader(file));
	}
	
	public static List<ReplayValues> parseSequenceFile(String filename)  throws IOException {
		return parseSequenceFile(new FileReader(filename));
	}
	
	public static List<ReplayValues> parseSequenceFile(Reader rawInput) throws IOException {
		List<ReplayValues> list = new ArrayList<ReplayValues>();
		
		BufferedReader input;
		if (rawInput instanceof BufferedReader) {
			input = (BufferedReader) rawInput;
		} else {
			input = new BufferedReader(rawInput);
		}
		
		String line;
		while ((line = input.readLine()) != null) {
			if (line.startsWith("[")) {
				// This is a method sequence
				ReplayValues v = new ReplayValues();
				
				// Snip off brackets
				line = line.substring(1, line.length() - 1);
				String[] methods = line.split(", ");
				
				for (String method : methods) {
					if (method.startsWith("symbolicBoolean(")) {
						
						if (method.startsWith("symbolicBoolean(true")) {
							v.addBoolean(true);
						} else if (method.startsWith("symbolicBoolean(false")) {
							v.addBoolean(false);
						} else {
							throw new RuntimeException("Incorrect boolean: "+method);
						}
						
					} else if (method.startsWith("symbolicInteger(")) {
						String theInt = method.substring("symbolicInteger(".length(), method.length()-1);
						try {
							v.addInteger(Integer.parseInt(theInt));
						} catch (NumberFormatException e) {
							System.err.println("Parsing error: "+theInt+" is not an integer");
							System.err.println("Found in method "+method);
							System.err.println("On the line: "+line);
							throw e;
						}
						
					} else if (method.startsWith("symbolicDouble(")) {
						String theDouble = method.substring("symbolicDouble(".length(), method.length()-1);
						try {
							v.addDouble(Double.parseDouble(theDouble));
						} catch (NumberFormatException e) {
							System.err.println("Parsing error: "+theDouble+" is not a double");
							System.err.println("Found in method "+method);
							System.err.println("On the line: "+line);
							throw e;
						}
					} else {
						throw new RuntimeException("Could not parse method "+method);
					}
				}
				list.add(v);
			}
		}
		
		input.close();
		return list;
	}
	
	
	private List<Integer> integers = new ArrayList<Integer>();
	private List<Boolean> booleans = new ArrayList<Boolean>();
	private List<Double> doubles = new ArrayList<Double>();
	
	public void addInteger(int integer) {
		integers.add(integer);
	}

	public void addBoolean(boolean bool) {
		booleans.add(bool);
	}
	
	public void addDouble(double dubs) {
		doubles.add(dubs);
	}
	
	@Override
	public int symbolicInteger(int dummyValue) {
		if (integers.isEmpty()) {
			throw new SimulatedBacktrackException();
		}
		return integers.remove(0);
	}

	@Override
	public boolean symbolicBoolean(boolean dummyValue, double probabilityTrue) {
		return symbolicBoolean(dummyValue);
	}
	
	@Override
	public boolean symbolicBoolean(boolean dummyValue) {
		if (booleans.isEmpty()) {
			throw new SimulatedBacktrackException();
		}
		return booleans.remove(0);
	}

	@Override
	public double symbolicDouble(double dummyValue) {
		if (doubles.isEmpty()) {
			throw new SimulatedBacktrackException();
		}
		return doubles.remove(0);
	}

	@Override
	public void continueOnlyIf(boolean expression) {
		if (! expression) {
			// SPF would, at this point, just do a backtrack since this path
			// is supposed to be impossible.
			throw new SimulatedBacktrackException();
		}
	}

}
