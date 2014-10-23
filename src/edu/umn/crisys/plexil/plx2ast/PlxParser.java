package edu.umn.crisys.plexil.plx2ast;

import static edu.umn.crisys.util.xml.XMLUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.globaldecl.CommandDecl;
import edu.umn.crisys.plexil.ast.globaldecl.LibraryDecl;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.ast.globaldecl.PlexilInterface;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class PlxParser {
    
    public static void main(String[] args) throws Exception {
        File resources = new File("tests/edu/umn/crisys/plexil/test/resources");
        
        //String[] files = new String[]{"array1.plx"};
        //*/
        String[] files = resources.list((File dir, String name) -> name.endsWith(".plx"));
        
        
        for (String file : files) {
            System.out.println("Parsing file "+file);
            File xmlFile = new File(resources, file);
            
            XMLEventReader xml = 
                XMLInputFactory.newInstance().createXMLEventReader(
                        new FileInputStream(xmlFile));
            
            System.out.println(parsePlxFile(xml, file.replaceFirst(".plx$", ""))
                    .getFullPrintout());
        }
    }
    
    public static PlexilPlan parseFile(File f) throws IOException, XMLStreamException, FactoryConfigurationError {
    	FileInputStream in = new FileInputStream(f);
        XMLEventReader xml = 
            XMLInputFactory.newInstance().createXMLEventReader(in);
        String name = f.getName().replaceFirst(".plx$", "");
        PlexilPlan ret = parsePlxFile(xml, name);
        xml.close();
        in.close();
        return ret;
    }
    
    public static PlexilPlan parsePlxFile(XMLEventReader xml, String planName) {
        // Right now, we're not looking at anything but the root node. There
        // is other stuff though, like declarations, that we should probably
        // grab too though. 
        assertStart("PlexilPlan", nextTag(xml));
        
        // Oh, good. It's a Plexil plan. Let's make an empty one.
        PlexilPlan p = new PlexilPlan(planName);
        
        XMLEvent nextTag = nextTag(xml);
        if (isTag(nextTag, "GlobalDeclarations")) {
        	// Global declarations. It could be any of these.
        	for (StartElement child : new TagIterator(xml, nextTag.asStartElement())) {
        		if (isTag(child, "CommandDeclaration")) {
        			p.getCommandDeclarations().add(parseCommandDecl(xml, child));
        		} else if (isTag(child, "StateDeclaration")) {
        			p.getStateDeclarations().add(parseStateDeclaration(xml, child));
        		} else if (isTag(child, "LibraryNodeDeclaration")) {
        			p.getLibraryDeclarations().add(parseLibraryDeclaration(xml, child));
        		} else if (isTag(child, "TimeScalingUnitsSubunits")) {
        			// I have ZERO idea what this means. But it's a simple
        			// integer.
        			p.setTimeScalingUnitsSubunits(Integer.parseInt(getStringContent(child, xml)));
        		}
        	}
        	nextTag = nextTag(xml);
        }
        
        // Now we should find a Node tag.
        assertStart("Node", nextTag);
        
        // Parse all the nodes.
        p.setRootNode(NodeParser.parsePlexilNode(nextTag, xml, p));
        
        // Parse parse parse, and finally close it all out.
        assertEnd("PlexilPlan", nextTag(xml));
        try {
			xml.close();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
        return p;
    }
    
    private static CommandDecl parseCommandDecl(XMLEventReader xml, StartElement start) {
    	// First is supposed to be the name.
    	String name = getStringContent(assertStart("Name", nextTag(xml)), xml);
    	// Now we can make the actual object.
    	CommandDecl cmd = new CommandDecl(name);
    	
    	// Next there may be return value(s????). 
    	List<VariableDecl> returns = new ArrayList<VariableDecl>();
    	while (nextTagIsStartOf("Return", xml)) {
    		returns.add(parseReturnOrParam(xml, nextTag(xml).asStartElement()));
    	}
    	if (returns.size() > 1) {
    		throw new RuntimeException("What does it mean to have multiple return tags???");
    	} else if (returns.size() == 1) {
    		cmd.setReturnValue(returns.get(0));
    	}
    	
    	// After that, any number of parameters.
    	while (nextTagIsStartOf("Parameter", xml)) {
    		cmd.addParameter(parseReturnOrParam(xml, nextTag(xml).asStartElement()));
    	}
    	
    	// Then finally, an optional resource list.
    	if (nextTagIsStartOf("ResourceList", xml)) {
    		System.err.println("Warning: This plan uses resources!!!");
    		while ( ! isEndTag(nextEvent(xml), "ResourceList")) { }
    	}
    	
    	// That should close it out.
    	assertClosedTag(start, xml);
    	return cmd;
    }
    
    private static LookupDecl parseStateDeclaration(XMLEventReader xml, StartElement start) {
    	// This is very similar to command declarations, but not quite the same.
    	// First is supposed to be the name.
    	String name = getStringContent(assertStart("Name", nextTag(xml)), xml);
    	// Now we can make the actual object.
    	LookupDecl lookup = new LookupDecl(name);
    	
    	// Next is a mandatory return value. 
    	lookup.setReturnValue(parseReturnOrParam(xml, assertStart("Return", nextTag(xml))));
    	
    	// After that, any number of parameters.
    	while (nextTagIsStartOf("Parameter", xml)) {
    		lookup.addParameter(parseReturnOrParam(xml, nextTag(xml).asStartElement()));
    	}
    	
    	// That should close it out.
    	assertClosedTag(start, xml);
    	return lookup;
    }
    
    private static LibraryDecl parseLibraryDeclaration(XMLEventReader xml, StartElement start) {
    	// First is supposed to be the name.
    	String name = getStringContent(assertStart("Name", nextTag(xml)), xml);

    	// Then an interface.
    	PlexilInterface iface = NodeParser.parseInterface(assertStart("Interface", nextTag(xml)), xml);
    	
    	// That's all!
    	assertClosedTag(start, xml);
    	
    	return new LibraryDecl(name, iface);
    }
    
    private static VariableDecl parseReturnOrParam(XMLEventReader xml, StartElement start) {
    	
    	String name = "";
    	// First, an optional name.
    	if (nextTagIsStartOf("Name", xml)) {
    		name = getStringContent(nextTag(xml), xml);
    	}
    	// Next, there has to be a type.
    	StartElement typeTag = assertStart("Type", nextTag(xml));
    	String typeStr = getStringContent(typeTag, xml);
    	PlexilType type = PlexilType.fuzzyValueOf(typeStr);
    	
    	// Finally, an optional size (which indicates that this shoulud actually
    	// be an array.
    	int maxSize = -1;
    	if (nextTagIsStartOf("MaxSize", xml)) {
    		XMLEvent maxSizeTag = nextTag(xml);
    		type = type.toArrayType();
    		maxSize = Integer.parseInt(getStringContent(maxSizeTag, xml));
    	}
    	
    	// And that should be it.
    	assertClosedTag(start, xml);
    	
    	if (maxSize >= 0) {
    		// This is an array.
    		return new VariableDecl(name, maxSize, type);
    	} else {
    		// It's just a variable.
    		return new VariableDecl(name, type);
    	}
    }

    
}
