package edu.umn.crisys.plexil.jkind.results;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;

import edu.umn.crisys.plexil.il2lustre.ILExprToLustre;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.plexil.runtime.values.StringValue;
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
	
	private static class Counterexample {
		private String name;
		private List<XmlRawData> data;
	}
	
	public static void main(String... args) throws Exception {
		List<Counterexample> results = parseResults(XMLInputFactory.newInstance().createXMLEventReader(
				new FileReader("/Users/jbiatek/Repositories/DriveToSchool/DriveToSchool.lus.xml")),
				Optional.empty());
		System.out.println("Parsed in "+results.size()+" counterexamples.");
		List<PlexilScript> scripts = results.stream()
				.map(JKindXmlParser::translateToScript)
				.collect(Collectors.toList());
		File outputDir = new File("/Users/jbiatek/Repositories/DriveToSchool/LustreTests");
		outputDir.mkdir();
		for (int i=0; i < scripts.size(); i++) {
			ScriptToXML.writeToStream(new PrintWriter(
					new File(outputDir, "DriveToSchool"+i+".psx")), 
					scripts.get(i));
		}
	}
	
	public static List<PlexilScript> translateToScripts(XMLEventReader lustreXml, 
			Optional<Map<String,StringValue>> stringMap) {
		List<Counterexample> results = parseResults(lustreXml, stringMap);
		return results.stream()
				.map(JKindXmlParser::translateToScript)
				.collect(Collectors.toList());
	}
	
	
	public static PlexilScript translateToScript(Counterexample cex) {
		PlexilScript script = new PlexilScript(cex.name);
		
		List<XmlRawData> relevant = cex.data.stream()
				.filter(d -> d.signal.startsWith("Lookup")
						&& !d.signal.endsWith("__raw"))
				.collect(Collectors.toList());
		int length = cex.data.get(0).dataOverTime.size();
		
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
	public static List<Counterexample> parseResults(XMLEventReader xml, 
			Optional<Map<String,StringValue>> stringMap) {
		List<Counterexample> counterexamples = new ArrayList<>();
		
		// First tag is supposed to be "Results"
		StartElement rootTag = assertStart("Results", nextTag(xml));
		// Go through children, we really only care about Property tags
		for (StartElement rootChild : allChildTagsOf(rootTag, xml)) {
			if (isTag(rootChild, "Property")) {
				// That's the one!
				String name = attribute(rootChild, "name");
				for (StartElement propChild : allChildTagsOf(rootChild, xml)) {
					if (isTag(propChild, "Counterexample")) {
						// This we care about
						Counterexample cex = getCounterexample(propChild, xml, stringMap);
						cex.name = name;
						counterexamples.add(cex);
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
	
	private static Counterexample getCounterexample(StartElement cex, XMLEventReader xml,
			Optional<Map<String,StringValue>> stringMap) {
		assertStart("Counterexample", cex);
		List<XmlRawData> dataList = new ArrayList<>();
		for (StartElement signal : allChildTagsOf(cex, xml)) {
			assertStart("Signal", signal);
			XmlRawData signalData = new XmlRawData();
			signalData.signal = attribute(signal, "name");
			String type = attribute(signal, "type");
			
			for (StartElement value : allChildTagsOf(signal, xml)) {
				assertStart("Value", value);
				signalData.dataOverTime.add(parseValue(
						getStringContent(value, xml), type, stringMap));
			}
			dataList.add(signalData);
		}
		Counterexample cexObj = new Counterexample();
		cexObj.data = dataList;
		return cexObj;

	}
	
	private static PValue parseValue(String value, String type, 
			Optional<Map<String,StringValue>> stringMap) {
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
		case PlanToLustre.STRING_ENUM_NAME: 
			if (value.equals(ILExprToLustre.UNKNOWN_STRING)) {
				return UnknownValue.get();
			}
			if (value.equals(ILExprToLustre.EMPTY_STRING)) {
				return StringValue.get("");
			}
			if (! stringMap.isPresent()) {
				throw new RuntimeException("Need to translate Lustre enum "+value+
						" back to Plexil, but string map wasn't provided");
			}
			return stringMap.map(actual -> actual.get(value))
					.orElseThrow(() -> new RuntimeException(
							"Lustre string "+value+" not found in string map."));
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
