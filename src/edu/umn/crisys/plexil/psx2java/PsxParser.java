package edu.umn.crisys.plexil.psx2java;

import static edu.umn.crisys.util.xml.XMLUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class PsxParser {

    private PsxParser() {}

    public static void main(String[] args) throws Exception {
        /*XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader xml = factory.createXMLEventReader(new FileInputStream("tests/edu/umn/crisys/plexil/test/resources/DriveToSchool.psx"));
        
        PlexilScript test = parse("DriveToSchool", xml);
        
        JCodeModel cm = new JCodeModel();
        test.toJava(cm, "");
        cm.build(new File("/Users/jbiatek/Desktop"));
        */
        
        File resources = new File("tests/edu/umn/crisys/plexil/test/resources");
        
        //*/
        String[] files = resources.list(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".psx");
            }
        });//*/
        
        
        for (String file : files) {
            System.out.println("Parsing file "+file);
            File xmlFile = new File(resources, file);
            
            XMLEventReader xml = 
                XMLInputFactory.newInstance().createXMLEventReader(
                        new FileInputStream(xmlFile));
            
            System.out.println(parse("script", xml));
        }

    }
    
    
    public static PlexilScript parse(String name, XMLEventReader xml) throws XMLStreamException {
        PlexilScript ret = new PlexilScript(name);
        
        // Skip ahead to the top tag and make sure things look right
        assertStart("PLEXILScript", xml.nextTag());
        
        // Here we go with the first thing
        StartElement e = xml.nextTag().asStartElement();
        if (isTag(e, "InitialState")) {
            for (StartElement event : new TagIterator(xml, e)) {
                ret.addInitialEvent(parseScriptEvent(event, xml));
            }
            // That should be it for initial state. Move to the next tag.
            e = xml.nextTag().asStartElement();
        }
        // Finished with initial state, if any. Now there should be a main Script tag.
        assertStart("Script", e);
        for (StartElement event : new TagIterator(xml, e)) {
            ret.addMainEvent(parseScriptEvent(event, xml));
        }
        
        assertEnd("PLEXILScript", xml.nextTag());
        return ret;
    }
    
    private static ScriptEvent parseScriptEvent(StartElement start, XMLEventReader xml) throws XMLStreamException {
        if (isTag(start, "State")) {
            return parseStateEvent(start, xml);
        } else if (isTagStartingWith(start, "Command")) {
            return parseCommandEvent(start, xml);
        } else if (isTag(start, "Delay")) {
            // No data to read, so just read the closing tag and return a delay
            assertClosedTag(start, xml);
            return new DelayEvent();
        } else if (isTag(start, "UpdateAck")) {
            // Just grab the name and read the end tag
            String name = attribute(start, "name");
            assertClosedTag(start, xml);
            return new UpdateAckEvent(name);
        } else if (isTag(start, "Simultaneous")) {
        	for (StartElement child : new TagIterator(xml, start)) {
        		
        	}
        }
        
        throw new RuntimeException("I don't know how to handle these: "+printEvents(start, xml));
    }
    
    
    private static CommandEvent parseCommandEvent(StartElement cmdStart,
            XMLEventReader xml) throws XMLStreamException {
        String name = attribute(cmdStart, "name");
        String typeStr = attribute(cmdStart, "type");
        PlexilType type = PlexilType.fuzzyValueOf(typeStr);

        String tagType = localNameOf(cmdStart);
        CommandEvent.Action action;
        if (tagType.equals("Command")) {
            action = CommandEvent.Action.RETURN;
        } else if (tagType.equals("CommandAck")) {
            action = CommandEvent.Action.ACK;
            // ACKs have their type as String, but actually they're command handles
            type = PlexilType.COMMAND_HANDLE;
        } else if (tagType.equals("CommandAbort")) {
            action = CommandEvent.Action.ABORT;
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
        ILExpression result = parseValueOrResult(current, xml, type, "Result");
        
        // Make sure that we're done
        assertClosedTag(cmdStart, xml);
        
        return new CommandEvent(action, name, params, result);
    }

    private static StateEvent parseStateEvent(StartElement start, XMLEventReader xml) throws XMLStreamException {
        // Get the name and type
        String name = attribute(start, "name");
        String typeStr = attribute(start, "type");
        PlexilType type = PlexilType.fuzzyValueOf(typeStr);
        
        StartElement current = xml.nextTag().asStartElement();
        
        // First, 0 to n Param tags
        List<PValue> params = new ArrayList<PValue>();
        if (isTag(current, "Param")) {
            params = parseAllParamTags(current, xml);
            current = xml.nextTag().asStartElement();
        }
        
        // Then, 1 Value tag, unless it's an array.
        ILExpression returnValue = parseValueOrResult(current, xml, type, "Value");
        
        
        // That should be everything, right?
        assertClosedTag(start, xml);
        
        return new StateEvent(name, params, returnValue);
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
        return PlexilType.fuzzyValueOf(paramType).parseValue(value);
    }

    private static ILExpression parseValueOrResult(XMLEvent current, XMLEventReader xml, PlexilType type, String expectedTagName) throws XMLStreamException {
        assertStart(expectedTagName, current);
        if (type.isArrayType()) {
            // Expect a few value tags then.
            List<PValueExpression> values = new ArrayList<PValueExpression>();
            PlexilType elementType = type.elementType();
            
            //As long as we keep seeing our tags, keep adding values in.
            while ( isTag(current, expectedTagName) ) {
                values.add(new PValueExpression(elementType.parseValue(getStringContent(current, xml))));
                
                if ( ! nextTagIsStartOf(expectedTagName, xml)) {
                    break;
                } else {
                    current = xml.nextTag();
                }
            }
            return new ArrayLiteralExpr(type, values);
            
        } else {
        	// Should just be a single value.
            String valueStr = getStringContent(current, xml);
            return new PValueExpression(type.parseValue(valueStr));
        }
    }
}
