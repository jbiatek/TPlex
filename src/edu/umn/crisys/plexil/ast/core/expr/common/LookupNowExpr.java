package edu.umn.crisys.plexil.ast.core.expr.common;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.visitor.CommonExprVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class LookupNowExpr extends CompositeExpr {
	
	private Expression name;
	
	private List<Expression> args;
	
	public LookupNowExpr(String state) {
	    this(new PValueExpression(state), new ArrayList<Expression>());
	}
	
	public LookupNowExpr(Expression state, List<Expression> args) {
	    PlexilType.STRING.typeCheck(state.getType());
	    name = state;
	    this.args = args;
	}
	
	public Expression getLookupName() {
	    return name;
	}
	
	/**
	 * Get the arguments, not including the Lookup name (which getArguments() does include.)
	 * @return
	 */
	public List<Expression> getLookupArgs() {
	    return args;
	}

	@Override
    public PlexilType getType() {
        return PlexilType.UNKNOWN;
    }

	@Override
	public String toString() {
	    String ret = "LookupNow("+name;
	    for (int i=0; i<args.size(); i++) {
	        ret += ", "+args.get(i);
	    }
	    return ret+")";
	}
	
	@Override
	public String asString() { return this.toString(); }

    @Override
    public List<Expression> getArguments() {
        ArrayList<Expression> argList = new ArrayList<Expression>(args);
        argList.add(0, name);
        return argList;
    }

    @Override
    public LookupNowExpr getCloneWithArgs(List<Expression> args) {
        Expression cloneName = args.remove(0);
        return new LookupNowExpr(cloneName, args);
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitLookupNow(this, param);
    }

}
