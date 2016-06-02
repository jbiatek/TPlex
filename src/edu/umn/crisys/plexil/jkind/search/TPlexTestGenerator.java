package edu.umn.crisys.plexil.jkind.search;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import lustre.LustreTrace;

/**
 * A JKindSearch that outputs traces as files to the given directory. 
 * 
 * @author jbiatek
 *
 */
public class TPlexTestGenerator extends JKindSearch {

	private int fileNameCounter = 0;
	private File outDir;

	
	public TPlexTestGenerator(PlanToLustre translator, File outputDir) {
		super(translator);
		
		outDir = outputDir;
	}
	
	private String createPropString(IncrementalTrace trace) {
		return trace.getProperties().stream()
				.map(TraceProperty::toString)
				.map(str -> str.replaceAll("[<>]", ""))
				.map(str -> str.replace(' ', '_'))
				.collect(Collectors.joining("."));
	}

	
	private void writeRawJKindTracesToFile() {
		int counter = 0;
		for (IncrementalTrace incTrace : getChosenTraces()) {
			writeToFile("raw-trace-"+counter+"-"
					+createPropString(incTrace)
					+".csv",
					incTrace.getPartialTrace().toString());
			counter++;
		}
	}

	
	synchronized private void writeTraceToFile(IncrementalTrace trace, LustreTrace reEnumed) {
		outDir.mkdir();
		
		writeToFile("trace"+fileNameCounter
					+"-"+createPropString(trace)
					+"-hash-"
					+trace.hashCode()
					+".csv", 
					reEnumed.toString());
		fileNameCounter++;
	}

	private void writeToFile(String filename, String content) {
		try { 
			File out = new File(outDir, filename);
			FileWriter fw = new FileWriter(out);
			fw.write(content);
			fw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 

	}

	@Override
	public void newGoalFound(IncrementalTrace foundTrace, LustreTrace extendedTrace) {
		writeTraceToFile(foundTrace, extendedTrace);
	}

	@Override
	public void redundantGoalFound(IncrementalTrace foundTrace, LustreTrace extendedTrace) {
		// Skip these.
	}

	@Override
	public void noTestFound(Optional<IncrementalTrace> prefix, TraceProperty property) {
		// Superclass will log this, we've got nothing to do. 
	}


}
