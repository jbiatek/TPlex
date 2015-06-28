package edu.umn.crisys.plexil.plx2ast;

import static edu.umn.crisys.util.xml.XMLUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umn.crisys.plexil.ast.Node;
import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.globaldecl.PlexilInterface;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.ast.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.nodebody.UpdateBody;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.util.xml.UnexpectedTagException;

public class NodeParser {
    
    private NodeParser() {}
    

    public static Node parsePlexilNode(XMLEvent start, XMLEventReader xml, PlexilPlan thePlan) {
        return parsePlexilNode(start, xml, new Node(thePlan));
    }
    
    private static Node parsePlexilNode(XMLEvent start, XMLEventReader xml, Node nodeToFill) {
        assertStart("Node", start);
        
        // Process child tags of <Node>
        for (StartElement e : allChildTagsOf(start.asStartElement(), xml)) {
            if (isTag(e, "NodeId")) {
                nodeToFill.setPlexilID(getStringContent(e, xml));
            }
            else if (isTag(e, "StartCondition")) {
                nodeToFill.setStartCondition(ExprParser.parse(nextTag(xml), xml, ExprType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "RepeatCondition")) {
                nodeToFill.setRepeatCondition(ExprParser.parse(nextTag(xml), xml, ExprType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "PreCondition")) {
                nodeToFill.setPreCondition(ExprParser.parse(nextTag(xml), xml, ExprType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "PostCondition")) {
                nodeToFill.setPostCondition(ExprParser.parse(nextTag(xml), xml, ExprType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "InvariantCondition")) {
                nodeToFill.setInvariantCondition(ExprParser.parse(nextTag(xml), xml, ExprType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "EndCondition")) {
                nodeToFill.setEndCondition(ExprParser.parse(nextTag(xml), xml, ExprType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "ExitCondition")) {
                nodeToFill.setExitCondition(ExprParser.parse(nextTag(xml), xml, ExprType.BOOLEAN));
                assertClosedTag(e, xml);
            }
            else if (isTag(e, "SkipCondition")) {
                nodeToFill.setSkipCondition(ExprParser.parse(nextTag(xml), xml, ExprType.BOOLEAN));
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
        for (StartElement e : allChildTagsOf(start, xml)) {
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
        Expression var = null;
        if (isTagEndingWith(child, "Variable") || 
                localNameOf(child).equals("ArrayElement")) {
            ExprType type;
            if (localNameOf(child).equals("ArrayElement")) {
                type = ExprType.UNKNOWN;
            } else {
                type = ExprType.fuzzyValueOf(localNameOf(child).replaceAll("Variable$", ""));
            }
            var = ExprParser.parse(child, xml, type);
            child = nextTag(xml);
        }
        // Must be a name at this point.
        assertStart("Name", child);
        Expression name = ExprParser.parse(nextTag(xml), xml, ExprType.STRING); 
        assertClosedTag(child.asStartElement(), xml);
        
        // Finally, an optional list of variables.
        List<Expression> args = new ArrayList<Expression>();
        child = nextTag(xml);
        if (isTag(child, "Arguments")) {
            for (StartElement arg : allChildTagsOf(child.asStartElement(), xml)) {
                args.add(ExprParser.parse(arg, xml, ExprType.UNKNOWN));
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
        Expression var;
        Expression rhs;
        
        var = ExprParser.parse(nextTag(xml), xml, ExprType.UNKNOWN);
        rhs = ExprParser.parse(nextTag(xml), xml, ExprType.UNKNOWN);
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
        for (StartElement pair : allChildTagsOf(start, xml)) {
            if (isTag(pair, "Pair")) {
                String name = getStringContent(assertStart("Name", nextTag(xml)), xml);
                Expression value = ExprParser.parse(nextTag(xml), xml, ExprType.UNKNOWN);

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
        for (StartElement alias : allChildTagsOf(start, xml)) {
            if (isTag(alias, "Alias")) {
                //These contain a <NodeParameter> string, then an expression.
                String param = getStringContent(assertStart("NodeParameter", nextTag(xml)), xml);
                Expression expr = ExprParser.parse(nextTag(xml), xml, ExprType.UNKNOWN);
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
        for (StartElement e : allChildTagsOf(start, xml)) {
            
            if (isTag(e, "DeclareVariable")) {
                node.addVar(parseDeclareVariable(e, xml));
            } else if (isTag(e, "DeclareArray")) {
                node.addArray(parseDeclareArray(e, xml));
                
            } else {
                throw new UnexpectedTagException(e);
            }
        }
    }
    
    
    private static VariableDecl parseDeclareArray(StartElement start, 
            XMLEventReader xml) {
        assertStart("DeclareArray", start);
        
        String name = null;
        String type = null;
        String maxSizeStr = null;
        List<PValue> parsedInitValues = null;
        
        for (StartElement e : allChildTagsOf(start, xml)) {
            if (isTag(e, "Name")) {
                name = getStringContent(e, xml);
            } else if (isTag(e, "Type")) {
                type = getStringContent(e, xml);
            } else if (isTag(e, "MaxSize")) {
                maxSizeStr = getStringContent(e, xml);
            } else if (isTag(e, "InitialValue")) {
                parsedInitValues = new ArrayList<PValue>();
                for (StartElement v : allChildTagsOf(e.asStartElement(), xml)) {
                    parsedInitValues.add(ExprParser.parsePValue(v, xml));
                }
            } else {
                throw new UnexpectedTagException(e);
            }
        }
        if (name == null || type == null || maxSizeStr == null) {
            throw new RuntimeException("Array has name "+name+" and type "+type
                    +", max size is "+maxSizeStr);
        }

        ExprType arrayType = ExprType.valueOf(type.toUpperCase()).toArrayType();
        int maxSize = Integer.parseInt(maxSizeStr);
        Optional<PValueList<PValue>> initialValue = Optional.empty();
        if (parsedInitValues != null) {
        	initialValue = Optional.of(new PValueList<PValue>(arrayType, parsedInitValues));
        }
        return new VariableDecl(name, arrayType, Optional.of(maxSize), initialValue);
    }


    private static VariableDecl parseDeclareVariable(XMLEvent start, 
            XMLEventReader xml) {
        assertStart("DeclareVariable", start);
        
        String name = null;
        String typeStr = null;
        Optional<PValue> init = Optional.empty();
        
        for (StartElement e : allChildTagsOf(start.asStartElement(), xml)) {
            if (isTag(e, "Name")) {
                name = getStringContent(e, xml);
            } else if (isTag(e, "Type")) {
                typeStr = getStringContent(e, xml);
            } else if (isTag(e, "InitialValue")) {
                init = Optional.of(ExprParser.parsePValue(nextTag(xml).asStartElement(), xml));
                assertClosedTag(e, xml);
            } else {
                throw new UnexpectedTagException(e);
            }
        }
        if (name == null || typeStr == null) {
            throw new RuntimeException("Variable has name "+name+" and type "+typeStr);
        }
        // Now we have all the info
        ExprType type = ExprType.valueOf(typeStr.toUpperCase());
        return new VariableDecl(name, type, Optional.empty(), init);
    }

    public static PlexilInterface parseInterface(StartElement start, XMLEventReader xml) {
        assertStart("Interface", start);
        PlexilInterface iface = new PlexilInterface();
        // Obviously, this one has been defined.
        iface.isDefined();
        
        for (StartElement inOrOut : allChildTagsOf(start, xml)) {
            boolean writeable = false;
            if (isTag(inOrOut, "InOut")) {
                writeable = true;
            } else {
                assertStart("In", inOrOut);
            }
            // Either way, it contains some number of DeclareVariable/Array. 
            // But Arrays aren't officially supported, so don't bother.
            for (StartElement declare : allChildTagsOf(inOrOut, xml)) {

                if (isTag(declare, "DeclareArray")) {
                    throw new RuntimeException("Arrays are not supported as "
                            +"Interface variables, according to the Plexil Reference.");
                }
                else if (isTag(declare, "DeclareVariable")) {
                    VariableDecl info = parseDeclareVariable(declare, xml);

                    if (writeable) {
                        iface.addInOutVariable(info.getName(), info.getType());
                    } else {
                        iface.addInVariable(info.getName(), info.getType());
                    }
                } else {
                    throw new UnexpectedTagException(declare, "DeclareVariable");
                }
            }
        }
        
        return iface;
    }
    
    

}
