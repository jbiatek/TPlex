package edu.umn.crisys.plexil.plx2ast;

import static edu.umn.crisys.util.xml.XMLUtils.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.expr.ast.NodeIDExpression;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.expr.common.Operation;
import edu.umn.crisys.plexil.expr.common.Operation.Operator;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.NodeTimepoint;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.util.xml.UnexpectedTagException;


public class ExprParser {

    private ExprParser() {}
    
    /**
     * If necessary, wraps the given Expression in a cast operation to make it
     * the expected type. Casting to boolean, numeric, and string is
     * supported. This is necessary for things like Lookups, which could return
     * anything. In the future, hopefully more static typechecking could make
     * this unnecessary.  
     * 
     * @param expr
     * @param expectedType
     * @return
     */
    public static Expression ensureType(Expression expr, PlexilType expectedType) {
        if (expr.getType() == expectedType) {
            return expr;
        }
        if( expr.getType().isNumeric() && expectedType.isNumeric()) {
            return expr;
        }
        // Hmm, not looking the same. 
        expectedType.typeCheck(expr.getType());
        // Okay, we can cast. 
        if (expectedType == PlexilType.BOOLEAN) {
            return Operation.castToBoolean(expr);
        } else if (expectedType.isNumeric()) {
            return Operation.castToNumeric(expr);
        } else if (expectedType == PlexilType.STRING) {
            return Operation.castToString(expr);
        }
        
        // Nothing else to try to do, unfortunately.
        return expr;
    }
    
    public static Expression parse(StartElement start, XMLEventReader xml, PlexilType expectedType) {
        Expression expr = methodDispatcher(start, xml);
        return ensureType(expr, expectedType);
    }
    
    private static Expression methodDispatcher(StartElement start, XMLEventReader xml) {
        String tag = localNameOf(start);
        if (isOperation(tag)) {
            return parseOperation(start, xml);
        } else if (isRHS(tag)) {
            return parseRHS(start, xml);
        } else if (isRegularVariable(tag)) {
            return parseRegularVariable(start, xml);
        } else if (isPValue(tag)) {
            return parsePValue(start, xml);
        } else if (isNodeReference(tag)) {
            return parseNodeReference(start, xml);
        } else if (isNodeStateVar(tag)) {
            return parseNodeStateVar(start, xml);
        } else if (isNodeOutcomeVar(tag)) {
            return parseNodeOutcomeVar(start, xml);
        } else if (isNodeFailureVar(tag)) {
            return parseNodeFailureVar(start, xml);
        } else if (isCommandHandleVar(tag)) {
            return parseCommandHandleVar(start, xml);
        } else if (isNodeTimepoint(tag)) {
            return parseNodeTimepoint(start, xml);
        } else if (isEqOrNe(tag)) {
            return parseEqOrNe(start, xml);
        } else if (isLookup(tag)) {
            return parseLookup(start, xml);
        } else if (isArrayElement(tag)) {
            return parseArrayElement(start, xml);
        }

        throw new RuntimeException("I have no handlers for "+tag+" tags.");
    }

    public static Expression parse(XMLEvent start, XMLEventReader xml, PlexilType expectedType) {
        return parse(start.asStartElement(), xml, expectedType);
    }

    private static List<Expression> parseMultiple(StartElement start, XMLEventReader xml, PlexilType expectedType) {
        List<Expression> list = new ArrayList<Expression>();
        for (StartElement tag : allChildTagsOf(start, xml)) {
            list.add(parse(tag, xml, expectedType));
        }
        return list;
    }

    private static boolean isOperation(String tag) {
        for (Operator o : Operator.values()) {
            if (o.toString().equals(tag.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public static Operation parseOperation(StartElement start, XMLEventReader xml) {
        return Operation.construct(localNameOf(start), 
                parseMultiple(start, xml, Operation.getArgType(localNameOf(start))));
    }

    private static boolean isRHS(String tag) {
        return tag.endsWith("RHS");
    }

    public static Expression parseRHS(StartElement start, XMLEventReader xml) {
        // These are all just wrappers around expressions. 
        // Go in, get the expression, check the end tag, and move on.
        PlexilType type = PlexilType.UNKNOWN;
        if ( ! isTagStartingWith(start, "Lookup")) {
            type = PlexilType.fuzzyValueOf(localNameOf(start).replaceFirst("RHS$", ""));
        }
        Expression ret = parse(nextTag(xml), xml, type);
        assertClosedTag(start, xml);
        return ret;
    }


    private static boolean isRegularVariable(String tag) {
        return tag.endsWith("Variable") && ! tag.startsWith("Node");
    }

    public static UnresolvedVariableExpr parseRegularVariable(StartElement start, XMLEventReader xml) {
        String typeStr = localNameOf(start).replaceFirst("Variable$", "");
        PlexilType type = PlexilType.valueOf(typeStr.toUpperCase());
        return new UnresolvedVariableExpr(getStringContent(start, xml), type);
    }


    private static boolean isPValue(String tag) {
        return tag.equals("BooleanValue")
        || tag.equals("IntegerValue")
        || tag.equals("RealValue")
        || tag.equals("StringValue") 
        || tag.equals("NodeStateValue") 
        || tag.equals("NodeOutcomeValue") 
        || tag.equals("NodeFailureValue") 
        || tag.equals("NodeCommandHandleValue"); 
    }

    public static PValue parsePValue(StartElement start, XMLEventReader xml) {
        String type = localNameOf(start).replaceAll("(Node|Value)", "");
        PlexilType pType = PlexilType.fuzzyValueOf(type);
        return pType.parseValue(getStringContent(start, xml));
    }


    private static boolean isNodeReference(String tag) {
        return tag.equals("NodeId") || tag.equals("NodeRef");
    }

    public static Expression parseNodeReference(StartElement start, XMLEventReader xml) {
    	Expression toReturn;
        if (localNameOf(start).equals("NodeId")) {
            toReturn = new NodeIDExpression(getStringContent(start, xml));
        } else if (localNameOf(start).equals("NodeRef")) {
        	toReturn = NodeRefExpr.valueOf(attribute(start, "dir").toUpperCase());
            assertClosedTag(start, xml);
        } else {
            throw new RuntimeException("Was expecting a node reference, not a "+start);
        }
        return toReturn;
    }


    private static boolean isNodeStateVar(String tag) {
        return tag.equals("NodeStateVariable");
    }

    public static Operation parseNodeStateVar(StartElement start, XMLEventReader xml) {
        // Should be a node reference here
        Expression nodeRef = parse(nextTag(xml), xml, PlexilType.NODEREF);
        assertClosedTag(start, xml);
        return Operation.getState(nodeRef);
    }


    private static boolean isNodeOutcomeVar(String tag) {
        return tag.equals("NodeOutcomeVariable");
    }

    public static Operation parseNodeOutcomeVar(StartElement start, XMLEventReader xml) {
        // Should be a node reference here
        Expression nodeRef = parse(nextTag(xml), xml, PlexilType.NODEREF);
        assertClosedTag(start, xml);
        return Operation.getOutcome(nodeRef);
    }


    private static boolean isNodeFailureVar(String tag) {
        return tag.equals("NodeFailureVariable");
    }

    public static Operation parseNodeFailureVar(StartElement start, XMLEventReader xml) {
        // Should be a node reference here
        Expression nodeRef = parse(nextTag(xml), xml, PlexilType.NODEREF);
        assertClosedTag(start, xml);
        return Operation.getFailure(nodeRef);
    }


    private static boolean isCommandHandleVar(String tag) {
        return tag.equals("NodeCommandHandleVariable");
    }

    public static Operation parseCommandHandleVar(StartElement start, XMLEventReader xml) {
        // Should be a node reference here
        Expression nodeRef = parse(nextTag(xml), xml, PlexilType.NODEREF);
        assertClosedTag(start, xml);
        return Operation.getCommandHandle(nodeRef);
    }


    private static boolean isNodeTimepoint(String tag) {
        return tag.equals("NodeTimepointValue");
    }

    public static NodeTimepointExpr parseNodeTimepoint(StartElement start, XMLEventReader xml) {
        String state = null;
        String timepoint = null;
        Expression nodeId = null;

        for (StartElement e : allChildTagsOf(start, xml)) {
            if (isNodeReference(localNameOf(e))) {
                nodeId = parseNodeReference(e, xml);
            } else if (isTag(e, "NodeStateValue")) {
                state = getStringContent(e, xml);;
            } else if (isTag(e, "Timepoint")) {
                timepoint = getStringContent(e, xml);;
            } else {
                throw new UnexpectedTagException(e);
            }
        }
        if (state == null || timepoint == null || nodeId == null) {
            throw new RuntimeException("Did not find all 3 tags, I got"+
                    state+", "+timepoint+", and "+nodeId+".");
        }

        return new NodeTimepointExpr(
        		NodeState.valueOf(state.toUpperCase()), 
        		NodeTimepoint.valueOf(timepoint.toUpperCase()), 
        		nodeId);    
    }



    private static boolean isEqOrNe(String tag) {
        return tag.startsWith("EQ") || tag.startsWith("NE");
    }

    public static Operation parseEqOrNe(StartElement start, XMLEventReader xml) {
        boolean negate = isTagStartingWith(start, "NE");
        // Slice off the "NE" or "EQ" to get the expected type.
        PlexilType type = PlexilType.fuzzyValueOf(localNameOf(start).substring(2));

        Expression one = parse(nextTag(xml), xml, type);
        Expression two = parse(nextTag(xml), xml, type);
        assertClosedTag(start, xml);

        if (negate) {
            return Operation.ne(one, two, type);
        } else {
            return Operation.eq(one, two, type);
        }    
    }



    private static boolean isLookup(String tag) {
        return tag.equals("LookupNow") || tag.equals("LookupOnChange");
    }


    public static Expression parseLookup(StartElement start, XMLEventReader xml) {
        Expression name = null;
        Expression tolerance = RealValue.get(0.0);
        List<Expression> args = new ArrayList<Expression>();

        for (StartElement e : allChildTagsOf(start, xml)) {
            if (isTag(e, "Name")) {
                name = parse(nextTag(xml), xml, PlexilType.STRING);
                assertClosedTag(e, xml);
            } else if (isTag(e, "Tolerance")) {
                tolerance = parse(nextTag(xml), xml, PlexilType.NUMERIC);
                assertClosedTag(e, xml);
            } else if (isTag(e, "Arguments")) {
                args = parseMultiple(e, xml, PlexilType.UNKNOWN);
            } else {
                throw new UnexpectedTagException(e);
            }
        }

        if (name == null) {
            throw new RuntimeException("Did not find a name for lookup "+start);
        }

        if (isTag(start, "LookupNow")) {
            return new LookupNowExpr(name, args);
        } else if (isTag(start, "LookupOnChange")) {
            return new LookupOnChangeExpr(name, tolerance, args);
        } else {
            throw new RuntimeException("uh, what happened here? "+start);
        }
    }


    private static boolean isArrayElement(String tag) {
        return tag.equals("ArrayElement");
    }

    public static ArrayIndexExpr parseArrayElement(StartElement start, XMLEventReader xml) {
        UnresolvedVariableExpr array = null;
        Expression index = null;

        for (StartElement e : allChildTagsOf(start, xml)) {
            if (isTag(e, "Name")) {
                array = new UnresolvedVariableExpr(getStringContent(e, xml), PlexilType.ARRAY);
            } else if (isTag(e, "Index")) {
                index = parse(nextTag(xml), xml, PlexilType.INTEGER);
                assertClosedTag(e, xml);
            } else {
                throw new UnexpectedTagException(e);
            }
        }

        if (array == null || index == null) {
            throw new RuntimeException("Didn't find an array name or index, " +
                    "instead found "+array+" and "+index);
        }
        return new ArrayIndexExpr(array, index);
    }

}
