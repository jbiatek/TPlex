package edu.umn.crisys.plexil.ast.expr.common;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.plexil.runtime.values.StringValue;

public abstract class LookupExpr implements Expression {

	private Expression name;
	private PlexilType type = PlexilType.UNKNOWN;
	private List<Expression> args;
	
	public LookupExpr(PlexilType type, String state) {
	    this(type, StringValue.get(state), new ArrayList<Expression>());
	}
	
	public LookupExpr(PlexilType type, Expression state, List<Expression> args) {
	    PlexilType.STRING.typeCheck(state.getType());
	    name = state;
	    this.args = args;
	}
	
	public Expression getLookupName() {
	    return name;
	}

	@Override
    public PlexilType getType() {
        return type;
    }

	
	public boolean hasConstantLookupName() {
		return name instanceof StringValue;
	}
	
	public String getLookupNameAsString() {
		if (name instanceof StringValue) {
			return ((StringValue) name).getString();
		}
		throw new RuntimeException("Tried to get non-constant lookup name");
	}
	
	/**
	 * Get the arguments, not including the Lookup name (which getArguments() does include.)
	 * @return
	 */
	public List<Expression> getLookupArgs() {
	    return args;
	}

    @Override
    public List<Expression> getArguments() {
        ArrayList<Expression> argList = new ArrayList<Expression>(args);
        argList.add(0, name);
        return argList;
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
