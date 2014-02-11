package edu.umn.crisys.plexil.ast.core.expr.common;

import java.util.Arrays;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.visitor.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class ArrayIndexExpr extends CompositeExpr {

    private Expression array;
    private Expression index;
    
    public ArrayIndexExpr(Expression array, Expression index) {
        PlexilType.INTEGER.typeCheck(index.getType());
        this.array = array;
        this.index = index;
    }
    
    public Expression getArray() {
        return array;
    }
    
    public Expression getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return array+"["+index+"]";
    }
    
    @Override
    public String asString() { return this.toString(); }
    
    @Override
    public PlexilType getType() {
        if (array.getType().isArrayType()) {
            return array.getType().elementType();
        }
        return PlexilType.UNKNOWN;
    }

    @Override
    public List<Expression> getArguments() {
        return Arrays.asList(array, index);
    }

    @Override
    public ArrayIndexExpr getCloneWithArgs(List<Expression> args) {
        return new ArrayIndexExpr(args.get(0), args.get(1));
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitArrayIndex(this, param);
    }

}
