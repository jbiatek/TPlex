package edu.umn.crisys.plexil.ast.core.expr.common;

import java.math.BigInteger;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.visitor.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.core.visitor.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.RealValue;
import edu.umn.crisys.plexil.java.values.StringValue;

public class PValueExpression implements ASTExpression, ILExpression {
    
    public static final PValueExpression TRUE = new PValueExpression(true);
    public static final PValueExpression FALSE = new PValueExpression(false);
    
    
    private PValue value;
    
    /**
     * Try to put together a PValue out of the type and value(s). 
     * @param type
     * @param values
     */
    public PValueExpression(String type, String...values) {
        this.value = PlexilType.fuzzyValueOf(type).parseValue(values);
    }
    
    /**
     * Parse the value(s) as the given type. Does the same thing as 
     * PlexilType.parseValue(), meaning that unless the type is an array type,
     * you should only pass in one value to be parsed.
     * @param type
     * @param value
     */
    public PValueExpression(PlexilType type, String... value) {
        this.value = type.parseValue(value);
    }
    
    /**
     * Create a PValueExpression from a Java native. Integer becomes PInteger,
     * String becomes PString, etc.
     * 
     * @param type
     * @param values
     */
    public PValueExpression(Object o) {
        if (o instanceof Boolean) {
            value = BooleanValue.get((Boolean) o);
        } else if (o instanceof Integer) {
            value = IntegerValue.get((Integer) o);
        } else if (o instanceof BigInteger) {
            value = IntegerValue.get(((BigInteger) o).intValue());
        } else if (o instanceof Double) {
            value = RealValue.get((Double) o);
        } else if (o instanceof String) {
            value = StringValue.get((String) o);
        } else {
            throw new RuntimeException("Add conversion for "+o.getClass());
        }
    }
    
    public PValueExpression(PValue v) {
        value = v;
    }

    public PValue getValue() {
        return value;
    }
    
    @Override
    public PlexilType getType() {
        return value.getType();
    }
    
    @Override
    public String toString() {
        if (value.getType() == PlexilType.STRING) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }
    
    @Override
    public String asString() { return this.toString(); }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PValueExpression) {
            return value.equals(((PValueExpression) o).value);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitPValue(this, param);
    }

    @Override
    public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
        return accept((CommonExprVisitor<P, R>) visitor, param);
    }

    @Override
    public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
        return accept((CommonExprVisitor<P,R>) visitor, param);
    }

}
