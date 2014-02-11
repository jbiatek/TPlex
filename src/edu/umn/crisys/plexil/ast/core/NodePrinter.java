package edu.umn.crisys.plexil.ast.core;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.ast.core.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.core.node.AssignmentBody;
import edu.umn.crisys.plexil.ast.core.node.CommandBody;
import edu.umn.crisys.plexil.ast.core.node.LibraryBody;
import edu.umn.crisys.plexil.ast.core.node.Node;
import edu.umn.crisys.plexil.ast.core.node.NodeBody;
import edu.umn.crisys.plexil.ast.core.node.NodeListBody;
import edu.umn.crisys.plexil.ast.core.node.UpdateBody;
import edu.umn.crisys.plexil.ast.core.visitor.NodeBodyVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.util.Pair;

public class NodePrinter implements NodeBodyVisitor<Void, String> {
    
    private static final int TAB = 4;
    
    private static String tab(int indent) {
        return new String(new char[indent]).replace('\0',' ');
    }
    
    private static void newLine(StringBuilder str, int indent) {
        str.append("\n"+tab(indent));
    }
    
    private static void trim(StringBuilder str, int numToTrim) {
        str.delete(str.length()-numToTrim, str.length());
    }
    
    private Node n;
    private int indent;
    
    public NodePrinter(Node n) {
        this(n, 0);
    }
    
    private NodePrinter(Node n, int indent) {
        this.n = n;
        this.indent = indent;
    }

    public String prettyPrint() {
        StringBuilder str = new StringBuilder(tab(indent));
        
        str.append(n.getPlexilID()+":"); newLine(str, indent);
        str.append("{"); newLine(str, indent);
        
        // Variables and the Interface must come first
        if (n.hasInterface()) {
            str.append(doInterface());
        }
        
        for (String variable : n.getVarNames()) {
            str.append(doVariable(variable));
        }
        
        if ( n.getStartCondition() != PValueExpression.TRUE) {
            str.append(tab(TAB)+"StartCondition "+n.getStartCondition()+";");
            newLine(str, indent);
        }
        if ( n.getSkipCondition() != PValueExpression.FALSE) {
            str.append(tab(TAB)+"SkipCondition "+n.getSkipCondition()+";");
            newLine(str, indent);
        }
        if ( n.getPreCondition() != PValueExpression.TRUE) {
            str.append(tab(TAB)+"PreCondition "+n.getPreCondition()+";");
            newLine(str, indent);
        }
        if ( n.getInvariantCondition() != PValueExpression.TRUE) {
            str.append(tab(TAB)+"InvariantCondition "+n.getInvariantCondition()+";");
            newLine(str, indent);
        }
        if ( n.getRepeatCondition() != PValueExpression.FALSE) {
            str.append(tab(TAB)+"RepeatCondition "+n.getRepeatCondition()+";");
            newLine(str, indent);
        }
        if ( n.getPostCondition() != PValueExpression.TRUE) {
            str.append(tab(TAB)+"PostCondition "+n.getPostCondition()+";");
            newLine(str, indent);
        }
        if ( n.getEndCondition() != DefaultEndExpr.get()) {
            str.append(tab(TAB)+"EndCondition "+n.getEndCondition()+";");
            newLine(str, indent);
        }
        if ( n.getExitCondition() != PValueExpression.FALSE) {
            str.append(tab(TAB)+"ExitCondition "+n.getExitCondition()+";");
            newLine(str, indent);
        }
        if (n.getPriority() != Integer.MAX_VALUE) {
            str.append(tab(TAB)+"Priority "+n.getPriority());
            newLine(str, indent);
        }

        str.append(n.getNodeBody().accept(this, null));
        
        str.append("}\n");
        
        return str.toString();
    }
    
    private String doVariable(String variable) {
        StringBuilder str = new StringBuilder(tab(TAB));
        PlexilType type = n.getVarType(variable);
        int arraySize = -1;
        if (type.isArrayType()) {
            type = type.elementType();
            arraySize = n.getArraySize(variable);
        }
        
        // Uncapitalize the type.
        String typeStr = type.toString().charAt(0) 
            + type.toString().substring(1).toLowerCase();
        
        str.append(typeStr+" "+variable);
        if (arraySize != -1) {
            str.append("["+arraySize+"]");
            if (n.getInitArray(variable) != null) {
                str.append(" = "+n.getInitArray(variable));
            }
        } else {
            if (n.getInitVariable(variable) != null) {
                str.append(" = "+n.getInitVariable(variable));
            }
        }
        
        str.append(";"); newLine(str, indent);

        return str.toString();
    }

    private String doInterface() {
        StringBuilder str = new StringBuilder();
        
        
        if (n.getInterfaceReadOnlyVars().size() != 0) {
            str.append(tab(TAB));
            str.append("In ");
            for (String v : n.getInterfaceReadOnlyVars()) {
                str.append(v+", ");
            }
            trim(str, 2);
            str.append(";"); newLine(str, indent); 
        }
        if (n.getInterfaceWriteableVars().size() != 0) {
            str.append(tab(TAB));
            str.append("InOut ");
            for (String v : n.getInterfaceWriteableVars()) {
                str.append(v+", ");
            }
            trim(str, 2);
            str.append(";"); newLine(str, indent); 
        }
        return str.toString();
    }

    @Override
    public String visitEmpty(NodeBody empty, Void p) {
        return "// Empty node, no body\n"+tab(indent);
    }

    @Override
    public String visitAssignment(AssignmentBody assign, Void p) {
        return "\n"+tab(indent+TAB)+assign.getLeftHandSide()
        +" = "+assign.getRightHandSide()+";\n"+tab(indent);
    }

    @Override
    public String visitCommand(CommandBody cmd, Void p) {
        StringBuilder str = new StringBuilder("\n"+tab(indent+TAB));
        if (cmd.getVarToAssign() != null) {
            str.append(cmd.getVarToAssign()+" = ");
        }
        if (cmd.getCommandName() instanceof PValueExpression) {
            // We want to avoid the quotes if it's just a single value.
            PValueExpression expr = (PValueExpression) cmd.getCommandName();
            str.append(expr.getValue() + "(");
        } else {
            // Yes, it's possible for the command being called to be an 
            // expression. The spec says it just has to be parenthesized.
            str.append("("+cmd.getCommandName()+") (");
        }
        for (Expression e : cmd.getCommandArguments()) {
            str.append(e.toString()+", ");
        }
        if (cmd.getCommandArguments().size() != 0) {
            trim(str, 2);
        }
        str.append(");");
        newLine(str, indent);
        
        return str.toString();
    }

    @Override
    public String visitLibrary(LibraryBody lib, Void p) {
        StringBuilder str = new StringBuilder("\n"+tab(indent+TAB));
        str.append("LibraryCall"+lib.getNodeId()+"(");
        
        if (lib.getAliases().size() != 0) {
            for (String alias : lib.getAliases()) {
                str.append(alias+" = "+lib.getAlias(alias));
            }
            trim(str, 2);
        }
        str.append(");");
        newLine(str, indent);
        return str.toString();
    }

    @Override
    public String visitNodeList(NodeListBody list, Void p) {
        StringBuilder str = new StringBuilder(tab(TAB));
        str.append("Concurrence"); newLine(str, indent+TAB);
        str.append("{\n"); // child will indent itself
        for (Node child : list.getChildList()) {
            NodePrinter childPrinter = new NodePrinter(child, indent+TAB*2);
            str.append(childPrinter.prettyPrint());
        }
        str.append(tab(indent+TAB)+"}");
        newLine(str, indent);
        
        return str.toString();
    }

    @Override
    public String visitUpdate(UpdateBody update, Void p) {
        StringBuilder str = new StringBuilder("\n"+tab(indent+TAB));
        str.append("Update ");
        for (Pair<String, ASTExpression> up : update.getUpdates()) {
            str.append(up.first +" = "+up.second +", ");
        }
        if (update.getUpdates().size() != 0) {
            trim(str, 2);
        }
        str.append(";");
        newLine(str, indent);
        
        return str.toString();
    }
    
}
