package edu.umn.crisys.plexil.expr.common;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.runtime.values.StringValue;

public abstract class LookupExpr extends ExpressionBase {

	private Expression name;
	private List<Expression> args;
	
	public LookupExpr(ExprType type, String state) {
	    this(type, StringValue.get(state), new ArrayList<Expression>());
	}
	
	public LookupExpr(ExprType type, Expression state, List<Expression> args) {
		super(type);
	    ExprType.STRING.typeCheck(state.getType());
	    name = state;
	    this.args = args;
	}
	
	public Expression getLookupName() {
	    return name;
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

}
