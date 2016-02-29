package edu.umn.crisys.plexil.script.translator;

import static edu.umn.crisys.util.xml.XMLUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.plx2ast.ExprParser;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.CommandReturn;
import edu.umn.crisys.plexil.script.ast.Delay;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;
import edu.umn.crisys.plexil.script.ast.UpdateAck;

public class ScriptParser {
	
	public static final String UNKNOWN_VALUE = "Plexil_Unknown";

    private ScriptParser() {}

    public static void main(String[] args) throws Exception {
        /*XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader xml = factory.createXMLEventReader(new FileInputStream("tests/edu/umn/crisys/plexil/test/resources/DriveToSchool.psx"));
        
        PlexilScript test = parse("DriveToSchool", xml);
        
        JCodeModel cm = new JCodeModel();
        test.toJava(cm, "");
        cm.build(new File("/Users/jbiatek/Desktop"));
        */
        
        File resources = new File("tests/edu/umn/crisys/plexil/test/resources");
        
        String[] files = resources.list((File dir, String name) -> name.endsWith(".psx"));
        
        for (String file : files) {
            System.out.println("Parsing file "+file);
            File xmlFile = new File(resources, file);
            
            XMLEventReader xml = 
                XMLInputFactory.newInstance().createXMLEventReader(
                        new FileInputStream(xmlFile));
            
            System.out.println(parse("script", xml));
        }

    }
    
    public static PlexilScript parse(File f) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
    	XMLEventReader xml = XMLInputFactory.newInstance().createXMLEventReader(new FileInputStream(f));
		String scriptName = f.getName().replaceAll("\\.psx$", "")+"Script";

    	return parse(scriptName, xml);
    }
    
    public static PlexilScript parse(String scriptName, XMLEventReader xml) throws XMLStreamException {
        PlexilScript ret = new PlexilScript(scriptName);
        
        // Skip ahead to the top tag and make sure things look right
        assertStart("PLEXILScript", xml.nextTag());
        
        // Here we go with the first thing
        StartElement e = xml.nextTag().asStartElement();
        if (isTag(e, "InitialState")) {
            for (StartElement event : allChildTagsOf(e, xml)) {
                ret.addInitialEvent(parseScriptEvent(event, xml));
            }
            // That should be it for initial state. Move to the next tag.
            e = xml.nextTag().asStartElement();
        }
        // Finished with initial state, if any. Now there should be a main Script tag.
        assertStart("Script", e);
        for (StartElement event : allChildTagsOf(e, xml)) {
            ret.addMainEvent(parseScriptEvent(event, xml));
        }
        
        assertEnd("PLEXILScript", xml.nextTag());
        return ret;
    }
    
    private static Event parseScriptEvent(StartElement start, XMLEventReader xml) throws XMLStreamException {
        if (isTag(start, "State")) {
            return parseStateEvent(start, xml);
        } else if (isTagStartingWith(start, "Command")) {
            return parseCommandEvent(start, xml);
        } else if (isTag(start, "Delay")) {
            // No data to read, so just read the closing tag and return a delay
            assertClosedTag(start, xml);
            return Delay.SINGLETON;
        } else if (isTag(start, "UpdateAck")) {
            // Just grab the name and read the end tag
            String name = attribute(start, "name");
            assertClosedTag(start, xml);
            return new UpdateAck(name);
        } else if (isTag(start, "Simultaneous")) {
        	Simultaneous sim = new Simultaneous();
        	for (StartElement child : allChildTagsOf(start, xml)) {
        		sim.addEvent(parseScriptEvent(child, xml));
        	}
        	return sim;
        }
        
        throw new RuntimeException("I don't know how to handle these: "+printEvents(start, xml));
    }
    
    private enum CommandEventTypes {
    	RETURN, ACK, ABORT;
    }
    
    private static Event parseCommandEvent(StartElement cmdStart,
            XMLEventReader xml) throws XMLStreamException {
        String name = attribute(cmdStart, "name");
        String typeStr = attribute(cmdStart, "type");
        PlexilType type = ExprParser.fuzzyParseType(typeStr);

        String tagType = localNameOf(cmdStart);
        CommandEventTypes action;
        if (tagType.equals("Command")) {
            action = CommandEventTypes.RETURN;
        } else if (tagType.equals("CommandAck")) {
            action = CommandEventTypes.ACK;
            // ACKs have their type as String, but actually they're command handles
            type = PlexilType.COMMAND_HANDLE;
        } else if (tagType.equals("CommandAbort")) {
            action = CommandEventTypes.ABORT;
        } else {
            throw new RuntimeException("What kind of command is "+tagType+"?");
        }
        
        // Now there are 0 or more Param tags
        StartElement current = xml.nextTag().asStartElement();
        List<PValue> params = new ArrayList<PValue>();
        while (isTag(current, "Param")) {
            params.add(parseParam(current, xml));
            current = xml.nextTag().asStartElement();
        }
        // Then 1 or more Result tags.
        assertStart("Result", current);
        PValue result = parseValueOrResult(current, xml, type, "Result");
        
        // Make sure that we're done
        assertClosedTag(cmdStart, xml);
        
        FunctionCall call = new FunctionCall(name, params);
        
        switch (action) {
        case RETURN:
        	return new CommandReturn(call, result);
        case ACK:
        	return new CommandAck(call, (CommandHandleState) result);
        case ABORT:
        	throw new RuntimeException("Abort is not implemented yet");
        default:
        	throw new RuntimeException("Missing case: "+action);
        }
    }

    private static StateChange parseStateEvent(StartElement start, XMLEventReader xml) throws XMLStreamException {
        // Get the name and type
        String name = attribute(start, "name");
        String typeStr = attribute(start, "type");
        PlexilType type = ExprParser.fuzzyParseType(typeStr);
        
        StartElement current = xml.nextTag().asStartElement();
        
        // First, 0 to n Param tags
        List<PValue> params = new ArrayList<PValue>();
        if (isTag(current, "Param")) {
            params = parseAllParamTags(current, xml);
            current = xml.nextTag().asStartElement();
        }
        
        // Then, 1 Value tag, unless it's an array.
        PValue returnValue = parseValueOrResult(current, xml, type, "Value");
        
        // That should be everything, right?
        assertClosedTag(start, xml);
        
        return new StateChange(new FunctionCall(name, params), returnValue);
    }
    
    private static List<PValue> parseAllParamTags(XMLEvent current, XMLEventReader xml) throws XMLStreamException {
        List<PValue> params = new ArrayList<PValue>();
        // This check is redundant after the first time.
        while ( isTag(current, "Param") ) {
            // Parse this Param tag
            params.add(parseParam(current.asStartElement(), xml));
            
            // Is it safe to continue eating tags?
            if ( ! nextTagIsStartOf("Param", xml)) {
                break;
            } else {
                current = xml.nextTag();
            }
        }
        return params;
    }
    
    private static PValue parseParam(StartElement start, XMLEventReader xml) throws XMLStreamException {
        Attribute attribute = start.getAttributeByName(new QName("type"));
        String paramType = "string";
        if (attribute != null) {
            paramType = attribute.getValue();
        }
        String value = getStringContent(start, xml);
        return ExprParser.parseValue(ExprParser.fuzzyParseType(paramType),
        		value);
    }

    private static PValue parseValueOrResult(XMLEvent current, XMLEventReader xml, PlexilType type, String expectedTagName) throws XMLStreamException {
        assertStart(expectedTagName, current);
        if (type.isArrayType()) {
            // Expect a few value tags then.
            List<PValue> values = new ArrayList<PValue>();
            PlexilType elementType = type.elementType();
            
            //As long as we keep seeing our tags, keep adding values in.
            while ( isTag(current, expectedTagName) ) {
                values.add(ExprParser.parseValue(elementType, getStringContent(current, xml)));
                
                if ( ! nextTagIsStartOf(expectedTagName, xml)) {
                    break;
                } else {
                    current = xml.nextTag();
                }
            }
            return new PValueList<PValue>(type.toILType(), values);
            
        } else {
        	// Should just be a single value.
            String valueStr = getStringContent(current, xml);
            return ExprParser.parseValue(type, valueStr);
        }
    }
}
