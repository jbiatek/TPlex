package edu.umn.crisys.plexil.ast.core.expr.common;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.RealValue;

public class LookupOnChangeExpr extends CompositeExpr {

	private Expression name;

	private Expression tolerance;

	private List<Expression> args;

	public LookupOnChangeExpr(Expression name, Expression tolerance, List<Expression> args) {
	    PlexilType.STRING.typeCheck(name.getType());
	    this.name = name;
	    PlexilType.NUMERIC.typeCheck(tolerance.getType());
	    this.tolerance = tolerance;
	    this.args = args;
	}
	
	public LookupOnChangeExpr(Expression name, List<Expression> args) {
	    this(name, new PValueExpression(RealValue.get(0.0)), args);
	}
	
	@Override
	public PlexilType getType() {
	    return PlexilType.UNKNOWN;
	}

    @Override
    public String toString() {
        String ret = "LookupOnChange("+name+", "+tolerance;
        if (args.size() > 0) {
            ret += ", ";
        }
        for (int i=0; i<args.size(); i++) {
            ret += args.get(i);
            if (i != args.size()-1) {
                ret += ", ";
            }
        }
        return ret+")";
    }
    
    @Override
    public String asString() { return this.toString(); }
    
    public Expression getLookupName() {
        return name;
    }
    
    public Expression getTolerance() {
        return tolerance;
    }
    
    public List<Expression> getLookupArgs() {
        return args;
    }

    @Override
    public List<Expression> getArguments() {
        List<Expression> ret = new ArrayList<Expression>(args);
        ret.add(0, name);
        ret.add(1, tolerance);
        return ret;
    }

    @Override
    public LookupOnChangeExpr getCloneWithArgs(List<Expression> args) {
        Expression cloneName = args.remove(0);
        Expression cloneTolerance = args.remove(0);
        return new LookupOnChangeExpr(cloneName, cloneTolerance, args);
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitLookupOnChange(this, param);
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
