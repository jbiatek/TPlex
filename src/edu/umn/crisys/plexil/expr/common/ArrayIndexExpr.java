package edu.umn.crisys.plexil.expr.common;

import java.util.Arrays;
import java.util.List;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.expr.ExprType;

public class ArrayIndexExpr extends ExpressionBase {

    private Expression array;
    private Expression index;
    
    public ArrayIndexExpr(Expression array, Expression index) {
        super(array.getType().isArrayType() ? array.getType().elementType()
        		: ExprType.UNKNOWN);
        ExprType.INTEGER.typeCheck(index.getType());
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
    public String asString() {
        return array+"["+index+"]";
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
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

	@Override
	public boolean isAssignable() {
		return true;
	}

}
