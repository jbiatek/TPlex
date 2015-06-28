package edu.umn.crisys.plexil.expr.common;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.runtime.values.StringValue;

public class LookupNowExpr extends LookupExpr {

	public LookupNowExpr(String state) {
	    this(PlexilType.UNKNOWN, StringValue.get(state), new ArrayList<Expression>());
	}
	
	public LookupNowExpr(Expression state, List<Expression> args) {
		this(PlexilType.UNKNOWN, state,args);
	}
	
	public LookupNowExpr(PlexilType type, Expression state, List<Expression> args) {
		super(type, state, args);
	}
	
	@Override
	public String asString() {
	    String ret = "LookupNow("+getLookupName();
	    for (int i=0; i<getLookupArgs().size(); i++) {
	        ret += ", "+getLookupArgs().get(i);
	    }
	    return ret+")";
	}

    @Override
    public LookupNowExpr getCloneWithArgs(List<Expression> args) {
        Expression cloneName = args.remove(0);
        return new LookupNowExpr(getType(), cloneName, args);
    }

    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

}
