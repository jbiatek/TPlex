package edu.umn.crisys.plexil.ast.core.expr.common;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.visitor.CommonExprVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;

/**
 * An array as an expression. According to the semantics, this should only be 
 * used for the right hand side of an assignment to an entire array. But this
 * way, we can store it as an initializer next to the regular variables. 
 * 
 * @author jbiatek
 *
 */
public class ArrayLiteralExpr extends CompositeExpr {

    private PlexilType type;
    private List<Expression> elements = new ArrayList<Expression>();
    
    public ArrayLiteralExpr(PlexilType type) {
        this(type, new ArrayList<Expression>());
    }

    
    public ArrayLiteralExpr(PlexilType type, List<? extends Expression> elements) {
        this.type = type;
        if ( ! type.isArrayType()) {
            throw new RuntimeException(type+" is not an array type.");
        }
        this.elements.addAll(elements);
    }
    
    @Override
    public PlexilType getType() {
        return type;
    }

    @Override
    public List<Expression> getArguments() {
        return elements;
    }

    @Override
    public ArrayLiteralExpr getCloneWithArgs(List<Expression> args) {
        return new ArrayLiteralExpr(type, args);
    }

    @Override
    public String toString() {
        if (elements.size() == 0) { return "#()"; }
        String ret = "#(";
        for (int i = 0 ; i < elements.size(); i++) {
            ret += elements.get(i);
            
            if (i != elements.size()-1) {
                ret += " ";
            }
        }
        return ret+")";
    }
    
    @Override
    public String asString() {
        return toString();
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitArrayLiteral(this, param);
    }

}
