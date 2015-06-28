package edu.umn.crisys.plexil.expr.common;

import java.util.Arrays;
import java.util.List;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;

public class LookupOnChangeExpr extends LookupExpr {

	private Expression tolerance;

	public LookupOnChangeExpr(PlexilType type, Expression name, Expression tolerance, List<Expression> args) {
		super(type, name, args);
		
	    PlexilType.NUMERIC.typeCheck(tolerance.getType());
	    this.tolerance = tolerance;
	}
	
	public LookupOnChangeExpr(Expression name, Expression tolerance, List<Expression> args) {
		this(PlexilType.UNKNOWN, name, tolerance, args);
	}
	
	public LookupOnChangeExpr(Expression name, Expression tolerance, Expression... args) {
		this(name, tolerance, Arrays.asList(args));
	}
	
	public LookupOnChangeExpr(Expression name, List<Expression> args) {
	    this(name, RealValue.get(0.0), args);
	}
	
	public LookupOnChangeExpr(Expression name, Expression...args) {
		this(name, RealValue.get(0.0), Arrays.asList(args));
	}
	
	public LookupOnChangeExpr(String name, Expression...args) {
		this(StringValue.get(name), args);
	}
	
    @Override
    public String toString() {
        String ret = "LookupOnChange("+getLookupName()+", "+getTolerance();
        if (getLookupArgs().size() > 0) {
            ret += ", ";
        }
        for (int i=0; i<getLookupArgs().size(); i++) {
            ret += getLookupArgs().get(i);
            if (i != getLookupArgs().size()-1) {
                ret += ", ";
            }
        }
        return ret+")";
    }
    
    @Override
    public String asString() { return this.toString(); }
    
    public Expression getTolerance() {
        return tolerance;
    }
    
    @Override
    public List<Expression> getArguments() {
        List<Expression> ret = super.getArguments();
        ret.add(1, tolerance);
        return ret;
    }

    @Override
    public LookupOnChangeExpr getCloneWithArgs(List<Expression> args) {
        Expression cloneName = args.remove(0);
        Expression cloneTolerance = args.remove(0);
        return new LookupOnChangeExpr(getType(), cloneName, cloneTolerance, args);
    }

    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

}
