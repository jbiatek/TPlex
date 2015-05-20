package edu.umn.crisys.plexil.jkind.results;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;

import edu.umn.crisys.plexil.il2lustre.ILExprToLustre;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;
import edu.umn.crisys.plexil.script.translator.ScriptToXML;
import edu.umn.crisys.util.xml.XMLUtils;

public class JKindXmlParser extends XMLUtils {

	private static class XmlRawData {
		
		private String signal;
		private List<PValue> dataOverTime = new ArrayList<>();
		
	}
	
	public static void main(String... args) throws Exception {
		List<List<XmlRawData>> results = parseResults(XMLInputFactory.newInstance().createXMLEventReader(
				new FileReader("/Users/jbiatek/Repositories/DriveToSchool/DriveToSchool.lus.xml")));
		System.out.println("Parsed in "+results.size()+" counterexamples.");
		List<PlexilScript> scripts = results.stream()
				.map(d -> translateToScript(d, "DriveToSchoolLustre"))
				.collect(Collectors.toList());
		File outputDir = new File("/Users/jbiatek/Repositories/DriveToSchool/LustreTests");
		outputDir.mkdir();
		for (int i=0; i < scripts.size(); i++) {
			ScriptToXML.writeToStream(new PrintWriter(
					new File(outputDir, "DriveToSchool"+i+".psx")), 
					scripts.get(i));
		}
	}
	
	public static List<PlexilScript> translateToScripts(XMLEventReader lustreXml) {
		List<List<XmlRawData>> results = parseResults(lustreXml);
		return results.stream()
				.map(d -> translateToScript(d, ""))
				.collect(Collectors.toList());
	}
	
	
	public static PlexilScript translateToScript(List<XmlRawData> data, String name) {
		PlexilScript script = new PlexilScript(name);
		
		List<XmlRawData> relevant = data.stream()
				.filter(d -> d.signal.startsWith("Lookup")
						&& !d.signal.endsWith("__raw"))
				.collect(Collectors.toList());
		int length = data.get(0).dataOverTime.size();
		
		for (int i=0; i < length; i++) {
			Simultaneous event = new Simultaneous();
			
			for (XmlRawData d : relevant) {
				event.addEvent(toScriptEvent(d.signal, d.dataOverTime.get(i)));
			}
			if (i == 0 ) {
				script.addInitialEvent(event.getCleanestEvent());
			} else {
				script.addMainEvent(event.getCleanestEvent());
			}
		}
		return script;
	}
	
	private static Event toScriptEvent(String signal, PValue v) {
		if (signal.startsWith("Lookup__")) {
			String stateName = signal.replaceFirst("Lookup__", "");
			return new StateChange(new FunctionCall(stateName), v);
		}
		throw new RuntimeException("Signal "+signal
				+" doesn't have a translation to PlexilScript");
	}
	
	
	/**
	 * Parse a JKind results file and produce a list of counterexamples
	 * 
	 * @param xml the stream to parse
	 * @return a list containing counterexamples (where a counterexample is a 
	 * list of values over time)
	 */
	public static List<List<XmlRawData>> parseResults(XMLEventReader xml) {
		List<List<XmlRawData>> counterexamples = new ArrayList<>();
		
		// First tag is supposed to be "Results"
		StartElement rootTag = assertStart("Results", nextTag(xml));
		// Go through children, we really only care about Property tags
		for (StartElement rootChild : allChildTagsOf(rootTag, xml)) {
			if (isTag(rootChild, "Property")) {
				// That's the one!
				for (StartElement propChild : allChildTagsOf(rootChild, xml)) {
					if (isTag(propChild, "Counterexample")) {
						// This we care about
						counterexamples.add(getCounterexample(propChild, xml));
					} else {
						// We don't care about this
						consumeAllOf(propChild, xml);
					}
				}
			} else {
				// Don't care
				consumeAllOf(rootChild, xml);
			}
		}

		// That should do it
		return counterexamples;
	}
	
	private static List<XmlRawData> getCounterexample(StartElement cex, XMLEventReader xml) {
		assertStart("Counterexample", cex);
		List<XmlRawData> counterex = new ArrayList<>();
		for (StartElement signal : allChildTagsOf(cex, xml)) {
			assertStart("Signal", signal);
			XmlRawData signalData = new XmlRawData();
			signalData.signal = attribute(signal, "name");
			String type = attribute(signal, "type");
			
			for (StartElement value : allChildTagsOf(signal, xml)) {
				assertStart("Value", value);
				signalData.dataOverTime.add(parseValue(
						getStringContent(value, xml), type));
			}
			counterex.add(signalData);
		}
		return counterex;

	}
	
	private static PValue parseValue(String value, String type) {
		switch(type) {
		case "int":
			return IntegerValue.get(Integer.parseInt(value));
		case "pboolean":
		case "bool":
			if (value.equals(ILExprToLustre.P_TRUE_ID)) {
				return BooleanValue.get(true);
			} else if (value.equals(ILExprToLustre.P_FALSE_ID)) {
				return BooleanValue.get(false);
			} else if (value.equals(ILExprToLustre.P_UNKNOWN_ID)) {
				return UnknownValue.get();
			}
			return PlexilType.BOOLEAN.parseValue(value);
		case "pstate":
			return PlexilType.STATE.parseValue(value);
		case "node_outcome":
			if (value.contains("unknown")) return NodeOutcome.UNKNOWN;
			return PlexilType.OUTCOME.parseValue(value);
		case "node_failure":
			if (value.contains("unknown")) return NodeFailureType.UNKNOWN;
			return PlexilType.FAILURE.parseValue(value);
		case "command_handle":
			if (value.contains("unknown")) return CommandHandleState.UNKNOWN;
			return PlexilType.COMMAND_HANDLE.parseValue(value);
		default:
			throw new RuntimeException("Parsing for type "+type+" not implemented");
		}
	}
	
}
