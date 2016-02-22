package edu.umn.crisys.plexil.expr.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILExprBase;
import edu.umn.crisys.plexil.expr.il.ILType;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

/**
 * Lookup expression which does no type checking. 
 * 
 * @author jbiatek
 *
 */
public class ASTLookupExpr extends ILExprBase {
	
	private ILExpr name;
	private Optional<ILExpr> tolerance;
	private List<ILExpr> args;
	
	public ASTLookupExpr(ILExpr name, List<ILExpr> args) {
		this(ILType.UNKNOWN, name, args, Optional.empty());
	}
	
	public ASTLookupExpr(ILExpr name, ILExpr tolerance, List<ILExpr> args) {
		this(ILType.UNKNOWN, name, args, Optional.of(tolerance));
	}
	
	public ASTLookupExpr(ILType type, String state) {
		this(type, StringValue.get(state), Collections.emptyList(), Optional.empty());
	}
	
	public ASTLookupExpr(ILType type, ILExpr state, 
			List<ILExpr> args,
			Optional<ILExpr> tolerance) {
		super(type);
	    
	    this.name = state;
	    this.tolerance = tolerance;
	    this.args = args;
	}
	
	public ILExpr getLookupName() {
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
	
	public Optional<ILExpr> getTolerance() {
		return tolerance;
	}
	
	/**
	 * Get the arguments, not including the Lookup name (which getArguments() does include.)
	 * @return
	 */
	public List<ILExpr> getLookupArgs() {
	    return args;
	}

    @Override
    public List<ILExpr> getArguments() {
        ArrayList<ILExpr> argList = new ArrayList<ILExpr>(args);
        argList.add(0, name);
        argList.add(1, tolerance.orElse(UnknownValue.get()));
        return argList;
    }
    
    @Override
    public ASTLookupExpr getCloneWithArgs(List<ILExpr> args) {
    	List<ILExpr> newArgs = new ArrayList<>(args);
    	// First arg is the name.
    	ILExpr name = newArgs.remove(0);
    	// Second arg is the tolerance
    	ILExpr toleranceExpr = newArgs.remove(0);
    	// If it's UNKNOWN, we are actually a LookupNow, which has no tolerance.
    	Optional<ILExpr> tolerance = Optional.of(toleranceExpr);
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
	    
	    for (ILExpr arg : args) {
	        ret += ", "+arg;
	    }
	    return ret+")";
	}
	


}
