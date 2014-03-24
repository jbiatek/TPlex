package edu.umn.crisys.plexil.plx2ast;

import static edu.umn.crisys.util.xml.XMLUtils.assertClosedTag;
import static edu.umn.crisys.util.xml.XMLUtils.assertEnd;
import static edu.umn.crisys.util.xml.XMLUtils.assertStart;
import static edu.umn.crisys.util.xml.XMLUtils.getStringContent;
import static edu.umn.crisys.util.xml.XMLUtils.isTag;
import static edu.umn.crisys.util.xml.XMLUtils.isTagEndingWith;
import static edu.umn.crisys.util.xml.XMLUtils.localNameOf;
import static edu.umn.crisys.util.xml.XMLUtils.nextTag;
import static edu.umn.crisys.util.xml.XMLUtils.nextTagIsStartOf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umn.crisys.plexil.ast.core.Node;
import edu.umn.crisys.plexil.ast.core.PlexilPlan;
import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.core.globaldecl.PlexilInterface;
import edu.umn.crisys.plexil.ast.core.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.core.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.core.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.core.nodebody.UpdateBody;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.util.xml.UnexpectedTagException;
import edu.umn.crisys.util.xml.XMLUtils.TagIterator;

public class NodeParser {
    
    private NodeParser() {}
    

    public static Node parsePlexilNode(XMLEvent start, XMLEventReader xml, PlexilPlan thePlan) {
        return parsePlexilNode(start, xml, new Node(thePlan));
    }
    
    private static Node parsePlexilNode(XMLEvent start, XMLEventReader xml, Node nodeToFill) {
        assertStart("Node", start);
        
        // Process child tags of <Node>
        for (StartElement e : new TagIterator(xml, "Node")) {
            if (isTag(e, "NodeId")) {
                nodeToFill.setPlexilID(getStringContent(e, xml));
            }
            else if (isTag(e, "StartCondition")) {
                nodeToFill.setStartCondition(ExprParser.parse(nextTag(xml), xml, PlexilType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "RepeatCondition")) {
                nodeToFill.setRepeatCondition(ExprParser.parse(nextTag(xml), xml, PlexilType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "PreCondition")) {
                nodeToFill.setPreCondition(ExprParser.parse(nextTag(xml), xml, PlexilType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "PostCondition")) {
                nodeToFill.setPostCondition(ExprParser.parse(nextTag(xml), xml, PlexilType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "InvariantCondition")) {
                nodeToFill.setInvariantCondition(ExprParser.parse(nextTag(xml), xml, PlexilType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "EndCondition")) {
                nodeToFill.setEndCondition(ExprParser.parse(nextTag(xml), xml, PlexilType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "ExitCondition")) {
                nodeToFill.setExitCondition(ExprParser.parse(nextTag(xml), xml, PlexilType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "SkipCondition")) {
                nodeToFill.setSkipCondition(ExprParser.parse(nextTag(xml), xml, PlexilType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "Priority" )) {
                nodeToFill.setPriority(Integer.parseInt(getStringContent(e, xml)));
            }
            else if (isTag(e, "Interface")) {
            	nodeToFill.setInterface(parseInterface(e, xml));
            }
            else if (isTag(e, "NodeBody")) {
                nodeToFill.setNodeBody(parseNodeBody(nextTag(xml).asStartElement(), xml, nodeToFill));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "VariableDeclarations")) {
                parseVariableDeclarations(nodeToFill, e, xml);
                // That method takes care of the end tag for us
            } 
            else {     
                throw new UnexpectedTagException(e);
            }
        }
        return nodeToFill;
    }
    
    private static NodeBody parseNodeBody(StartElement start, XMLEventReader xml, Node theNode)  {
        if (isTag(start, "NodeList")) {
            return parseNodeListBody(start, xml, theNode);
        } else if (isTag(start, "Command")) {
            return parseCommandBody(start, xml);
        } else if (isTag(start, "Assignment")) {
            return parseAssignment(start, xml);
        } else if (isTag(start, "Update")) {
            return parseUpdate(start, xml);
        } else if (isTag(start, "LibraryNodeCall")) {
            return parseLibraryNodeCall(start, xml);
        } else {
            throw new UnexpectedTagException(start);
        }
    }


    private static NodeListBody parseNodeListBody(StartElement start, XMLEventReader xml, Node theNode) {
        NodeListBody list = new NodeListBody();
        for (StartElement e : new TagIterator(xml, "NodeList")) {
            list.getChildList().add(parsePlexilNode(e, xml, new Node(theNode)));
        }
        return list;
    }

    private static CommandBody parseCommandBody(StartElement start, XMLEventReader xml) {
        
        XMLEvent child = nextTag(xml).asStartElement();
        // Optional <ResourceList>
        if (isTag(child, "ResourceList")) {
            throw new RuntimeException("Resources not implemented");
        }
        // Optional variable expression
        ASTExpression var = null;
        if (isTagEndingWith(child, "Variable") || 
                localNameOf(child).equals("ArrayElement")) {
            PlexilType type;
            if (localNameOf(child).equals("ArrayElement")) {
                type = PlexilType.UNKNOWN;
            } else {
                type = PlexilType.fuzzyValueOf(localNameOf(child).replaceAll("Variable$", ""));
            }
            var = ExprParser.parse(child, xml, type);
            child = nextTag(xml);
        }
        // Must be a name at this point.
        assertStart("Name", child);
        ASTExpression name = ExprParser.parse(nextTag(xml), xml, PlexilType.STRING); 
        assertClosedTag(child.asStartElement(), xml);
        
        // Finally, an optional list of variables.
        List<ASTExpression> args = new ArrayList<ASTExpression>();
        child = nextTag(xml);
        if (isTag(child, "Arguments")) {
            for (StartElement arg : new TagIterator(xml, child.asStartElement())) {
                args.add(ExprParser.parse(arg, xml, PlexilType.UNKNOWN));
            }
            // Now we should be done.
            assertClosedTag(start, xml);
        } else {
            // If it's not args, there's nothing left but to end the Command.
            assertEnd("Command", child);
        }
        
        if (var == null) {
            return new CommandBody(name, args);
        } else if (var instanceof UnresolvedVariableExpr) {
            return new CommandBody((UnresolvedVariableExpr) var, name, args);
        } else if (var instanceof ArrayIndexExpr) {
            return new CommandBody((ArrayIndexExpr) var, name, args);
        } else {
            throw new RuntimeException("Some kind of LHS expected, not "+var);
        }
    }

    private static NodeBody parseAssignment(StartElement start,
            XMLEventReader xml) {
        // It should be a Variable and then a TypeRHS.
        ASTExpression var;
        ASTExpression rhs;
        
        var = ExprParser.parse(nextTag(xml), xml, PlexilType.UNKNOWN);
        rhs = ExprParser.parse(nextTag(xml), xml, PlexilType.UNKNOWN);
        // That should be it.
        assertClosedTag(start, xml);
        
        if (var instanceof UnresolvedVariableExpr) {
            return new AssignmentBody((UnresolvedVariableExpr) var, rhs);
        } else if (var instanceof ArrayIndexExpr) {
            return new AssignmentBody((ArrayIndexExpr) var, rhs);
        } else {
            throw new RuntimeException(var+"is not a valid left hand side.");
        }
    }

    private static NodeBody parseUpdate(StartElement start, XMLEventReader xml) {
        UpdateBody update = new UpdateBody();
        
        // Contains a bunch of things like this:
        // <Pair><Name>blah</Name><IntegerValue>1</IntegerValue></Pair>
        for (StartElement pair : new TagIterator(xml, start)) {
            if (isTag(pair, "Pair")) {
                String name = getStringContent(assertStart("Name", nextTag(xml)), xml);
                ASTExpression value = ExprParser.parse(nextTag(xml), xml, PlexilType.UNKNOWN);

                assertEnd("Pair", nextTag(xml));
                update.addUpdate(name, value);
            } else {
                throw new UnexpectedTagException(pair, "Pair");
            }
        }
        return update;
    }

    private static NodeBody parseLibraryNodeCall(StartElement start,
            XMLEventReader xml) {
        // Mandatory: NodeId, just a string
        String nodeId = getStringContent(assertStart("NodeId", nextTag(xml)), xml);
        
        // Optional: RenameNodeId, also just a string.
        if (nextTagIsStartOf("RenameNodeId", xml)) {
            // I've never seen this before.
            throw new RuntimeException("RenameNodeId support not written yet.");
        }
        
        // Then, 0 or more Alias tags. 
        // First, let's make the body to store them in.
        LibraryBody lib = new LibraryBody(nodeId);
        for (StartElement alias : new TagIterator(xml, start)) {
            if (isTag(alias, "Alias")) {
                //These contain a <NodeParameter> string, then an expression.
                String param = getStringContent(assertStart("NodeParameter", nextTag(xml)), xml);
                ASTExpression expr = ExprParser.parse(nextTag(xml), xml, PlexilType.UNKNOWN);
                assertClosedTag(alias, xml);
                
                lib.addAlias(param, expr);
            } else {
                throw new UnexpectedTagException(alias, "Alias");
            }
        }
        
        return lib;
    }

    private static void parseVariableDeclarations(Node node, StartElement start, 
            XMLEventReader xml)  {
        assertStart("VariableDeclarations", start);
        for (StartElement e : new TagIterator(xml, start)) {
            
            if (isTag(e, "DeclareVariable")) {
                DeclaredVarInfo varInfo = parseDeclareVariable(e, xml);
                if (varInfo.varInit == null) {
                    node.addVar(varInfo.name, varInfo.type);
                } else {
                    node.addVar(varInfo.name, varInfo.type, varInfo.varInit);
                }
                
            } else if (isTag(e, "DeclareArray")) {
                DeclaredVarInfo arrayInfo = parseDeclareArray(e, xml);
                if (arrayInfo.varInit == null) {
                    node.addArray(arrayInfo.name, arrayInfo.type, 
                            arrayInfo.maxSize);
                } else {
                    node.addArray(arrayInfo.name, arrayInfo.type, 
                            arrayInfo.maxSize, (PValueList<?>)arrayInfo.varInit);
                }
                
            } else {
                throw new UnexpectedTagException(e);
            }
        }
    }
    
    private static class DeclaredVarInfo {
        public String name = null;
        public PlexilType type = null;
        public int maxSize = -1;
        public PValue varInit = null;
    }

    
    private static DeclaredVarInfo parseDeclareArray(StartElement start, 
            XMLEventReader xml) {
        assertStart("DeclareArray", start);
        
        String name = null;
        String type = null;
        String maxSize = null;
        List<PValue> init = null;
        
        for (StartElement e : new TagIterator(xml, "DeclareArray")) {
            if (isTag(e, "Name")) {
                name = getStringContent(e, xml);
            } else if (isTag(e, "Type")) {
                type = getStringContent(e, xml);
            } else if (isTag(e, "MaxSize")) {
                maxSize = getStringContent(e, xml);
            } else if (isTag(e, "InitialValue")) {
                init = new ArrayList<PValue>();
                for (StartElement v : new TagIterator(xml, "InitialValue")) {
                    init.add(ExprParser.parsePValue(v, xml));
                }
            } else {
                throw new UnexpectedTagException(e);
            }
        }
        if (name == null || type == null || maxSize == null) {
            throw new RuntimeException("Array has name "+name+" and type "+type
                    +", max size is "+maxSize);
        }
        DeclaredVarInfo info = new DeclaredVarInfo();
        info.name = name;
        info.type = PlexilType.valueOf(type.toUpperCase()).toArrayType();
        info.maxSize = Integer.parseInt(maxSize); 
        if (init != null) {
        	info.varInit = new PValueList<PValue>(info.type, init);
        }
        
        return info;
    }


    private static DeclaredVarInfo parseDeclareVariable(XMLEvent start, 
            XMLEventReader xml) {
        assertStart("DeclareVariable", start);
        
        String name = null;
        String type = null;
        PValue init = null;
        
        for (StartElement e : new TagIterator(xml, "DeclareVariable")) {
            if (isTag(e, "Name")) {
                name = getStringContent(e, xml);
            } else if (isTag(e, "Type")) {
                type = getStringContent(e, xml);
            } else if (isTag(e, "InitialValue")) {
                init = ExprParser.parsePValue(nextTag(xml).asStartElement(), xml);
                assertClosedTag(e, xml);
            } else {
                throw new UnexpectedTagException(e);
            }
        }
        if (name == null || type == null) {
            throw new RuntimeException("Variable has name "+name+" and type "+type);
        }
        // Now we have all the info
        DeclaredVarInfo info = new DeclaredVarInfo();
        info.name = name;
        if (type.equalsIgnoreCase("Duration") || type.equalsIgnoreCase("Date")) {
        	info.type = PlexilType.REAL;
        } else {
        	info.type = PlexilType.valueOf(type.toUpperCase());
        }
        if (init != null) {
            info.varInit = init;
        }
        return info;
    }

    public static PlexilInterface parseInterface(StartElement start, XMLEventReader xml) {
        assertStart("Interface", start);
        PlexilInterface iface = new PlexilInterface();
        // Obviously, this one has been defined.
        iface.isDefined();
        
        for (StartElement inOrOut : new TagIterator(xml, start)) {
            boolean writeable = false;
            if (isTag(inOrOut, "InOut")) {
                writeable = true;
            } else {
                assertStart("In", inOrOut);
            }
            // Either way, it contains some number of DeclareVariable/Array. 
            // But Arrays aren't officially supported, so don't bother.
            for (StartElement declare : new TagIterator(xml, inOrOut)) {

                if (isTag(declare, "DeclareArray")) {
                    throw new RuntimeException("Arrays are not supported as "
                            +"Interface variables, according to the Plexil Reference.");
                }
                else if (isTag(declare, "DeclareVariable")) {
                    DeclaredVarInfo info = parseDeclareVariable(declare, xml);

                    if (writeable) {
                        iface.addInOutVariable(info.name, info.type);
                    } else {
                        iface.addInVariable(info.name, info.type);
                    }
                } else {
                    throw new UnexpectedTagException(declare, "DeclareVariable");
                }
            }
        }
        
        return iface;
    }
    
    

}
