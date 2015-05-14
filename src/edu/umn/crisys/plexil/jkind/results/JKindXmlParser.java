package edu.umn.crisys.plexil.jkind.results;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;

import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
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
	}
	
	public static PlexilScript translateToScript(List<XmlRawData> data, String name) {
		PlexilScript s = new PlexilScript(name);
		
		
		
		return s;
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
