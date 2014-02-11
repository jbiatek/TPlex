package edu.umn.crisys.plexil.plx2ast;

import static edu.umn.crisys.util.xml.XMLUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import edu.umn.crisys.plexil.ast.core.PlexilPlan;

public class PlxParser {
    
    public static void main(String[] args) throws Exception {
        File resources = new File("tests/edu/umn/crisys/plexil/test/resources");
        
        //String[] files = new String[]{"array1.plx"};
        //*/
        String[] files = resources.list(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".plx");
            }
        });//*/
        
        
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
    
    public static PlexilPlan parseFile(File f) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        XMLEventReader xml = 
            XMLInputFactory.newInstance().createXMLEventReader(
                    new FileInputStream(f));
        String name = f.getName().replaceFirst(".plx$", "");
        return parsePlxFile(xml, name);

    }
    
    public static PlexilPlan parsePlxFile(XMLEventReader xml, String planName) {
        // Right now, we're not looking at anything but the root node. There
        // is other stuff though, like declarations, that we should probably
        // grab too though. 
        XMLEvent start = assertStart("PlexilPlan", nextTag(xml));
        while ( ! isTag(start, "Node")) {
            start = nextEvent(xml);
        }
        PlexilPlan p = new PlexilPlan(planName);
        p.setRootNode(NodeParser.parsePlexilNode(start, xml));
        assertEnd("PlexilPlan", nextTag(xml));
        return p;
    }
    
    
    
}
