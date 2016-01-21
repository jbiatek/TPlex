package edu.umn.crisys.plexil.expr.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

/**
 * Lookup expression which does no type checking. 
 * 
 * @author jbiatek
 *
 */
public class ASTLookupExpr extends ExpressionBase {
	
	private Expression name;
	private Optional<Expression> tolerance;
	private List<Expression> args;
	
	public ASTLookupExpr(Expression name, List<Expression> args) {
		this(ExprType.UNKNOWN, name, args, Optional.empty());
	}
	
	public ASTLookupExpr(Expression name, Expression tolerance, List<Expression> args) {
		this(ExprType.UNKNOWN, name, args, Optional.of(tolerance));
	}
	
	public ASTLookupExpr(ExprType type, String state) {
		this(type, StringValue.get(state), Collections.emptyList(), Optional.empty());
	}
	
	public ASTLookupExpr(ExprType type, Expression state, 
			List<Expression> args,
			Optional<Expression> tolerance) {
		super(type);
	    
	    this.name = state;
	    this.tolerance = tolerance;
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
		throw new RuntimeException("Tried to get non-constant lookup name: "+name);
	}
	
	public Optional<Expression> getTolerance() {
		return tolerance;
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
        argList.add(1, tolerance.orElse(UnknownValue.get()));
        return argList;
    }
    
    @Override
    public ASTLookupExpr getCloneWithArgs(List<Expression> args) {
    	List<Expression> newArgs = new ArrayList<>(args);
    	// First arg is the name.
    	Expression name = newArgs.remove(0);
    	// Second arg is the tolerance
    	Expression toleranceExpr = newArgs.remove(0);
    	// If it's UNKNOWN, we are actually a LookupNow, which has no tolerance.
    	Optional<Expression> tolerance = Optional.of(toleranceExpr);
    	if (toleranceExpr instanceof UnknownValue) {
    		tolerance = Optional.empty();
    	}
    	return new ASTLookupExpr(getType(), name, args, tolerance);
    }

    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

	@Override
	public String asString() {
		String ret = tolerance.map(
				t -> "LookupOnChange("+getLookupName()+", "+t)
				.orElse("LookupNow("+getLookupName());
	    
	    for (Expression arg : args) {
	        ret += ", "+arg;
	    }
	    return ret+")";
	}
	


}
