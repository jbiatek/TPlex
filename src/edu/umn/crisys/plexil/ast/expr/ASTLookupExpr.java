package edu.umn.crisys.plexil.ast.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

/**
 * Lookup expression which does no type checking. 
 * 
 * @author jbiatek
 *
 */
public class ASTLookupExpr extends PlexilExprBase {
	
	private PlexilExpr name;
	private Optional<PlexilExpr> tolerance;
	private List<PlexilExpr> args;
	
	public ASTLookupExpr(PlexilExpr name, List<PlexilExpr> args) {
		this(PlexilType.UNKNOWN, name, args, Optional.empty());
	}
	
	public ASTLookupExpr(PlexilExpr name, PlexilExpr tolerance, List<PlexilExpr> args) {
		this(PlexilType.UNKNOWN, name, args, Optional.of(tolerance));
	}
	
	public ASTLookupExpr(PlexilType type, String state) {
		this(type, StringValue.get(state), Collections.emptyList(), Optional.empty());
	}
	
	public ASTLookupExpr(PlexilType type, PlexilExpr state, 
			List<PlexilExpr> args,
			Optional<PlexilExpr> tolerance) {
		super(type);
	    
	    this.name = state;
	    this.tolerance = tolerance;
	    this.args = args;
	}
	
	public PlexilExpr getLookupName() {
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
	
	public Optional<PlexilExpr> getTolerance() {
		return tolerance;
	}
	
	/**
	 * Get the arguments, not including the Lookup name (which getArguments() does include.)
	 * @return
	 */
	public List<PlexilExpr> getLookupArgs() {
	    return args;
	}

    @Override
    public List<PlexilExpr> getPlexilArguments() {
        ArrayList<PlexilExpr> argList = new ArrayList<PlexilExpr>(args);
        argList.add(0, name);
        argList.add(1, tolerance.orElse(UnknownValue.get()));
        return argList;
    }
    
    @Override
    public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

	@Override
	public String asString() {
		String ret = tolerance.map(
				t -> "LookupOnChange("+getLookupName()+", "+t)
				.orElse("LookupNow("+getLookupName());
	    
	    for (PlexilExpr arg : args) {
	        ret += ", "+arg;
	    }
	    return ret+")";
	}
	


}
