package edu.umn.crisys.plexil.expr.il;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.expr.ast.PlexilType;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class LookupExpr extends ILExprBase {

	public static LookupExpr lookupTime(PlexilType numericType) {
		if ( ! numericType.isNumeric()) {
			throw new RuntimeException("Time is not "+numericType);
		}
		LookupDecl timeDeclaration = new LookupDecl("time");
		timeDeclaration.setReturnValue(new VariableDecl("time", numericType));
		return new LookupExpr(timeDeclaration, StringValue.get("time"));
	}
	
	private ILExpr name;
	private Optional<ILExpr> tolerance;
	private List<ILExpr> args;
	private LookupDecl typeData;
	
	public LookupExpr(LookupDecl typeData, ILExpr name) {
		this(typeData, name, Collections.emptyList());
	}

	public LookupExpr(LookupDecl typeData, ILExpr state, List<ILExpr> args) {
		this(typeData, state, args, Optional.empty());
	}
	
	public LookupExpr(LookupDecl typeData, ILExpr state, List<ILExpr> args,
			Optional<ILExpr> tolerance) {
		super(typeData.getReturnValue().get().getType().toILType());
	    ILTypeChecker.typeCheck(state, ILType.STRING);
	    tolerance.ifPresent(t -> ILTypeChecker.typeCheck(t, ILType.REAL));
	    
	    if (typeData.getParameters().isEmpty()) {
	    	// This is either supposed to have 0 arguments, or it's got
	    	// variable arguments. As far as I know, there's no difference in
	    	// the PLX file. Instead, just make sure that the arguments have
	    	// some sort of legal type. 
	    	args.stream().forEach(ILTypeChecker::ensureExpressionContainsLegalTypes);
	    } else {
	    	// We have some more concrete type data. Let's use it.
	    	if (typeData.getParameters().size() != args.size()) {
	    		throw new RuntimeException("Arguments to lookup don't match the "
	    				+ "lookup declaration: Declared as "+typeData+", "
	    				+ "but there are "+args.size()+" arguments.");
	    	}
	    	for (int i=0; i < args.size(); i++) {
	    		ILTypeChecker.typeCheck(args.get(i), 
	    				typeData.getParameters().get(i).getType().toILType());
	    	}
	    }
	    
	    this.name = state;
	    this.tolerance = tolerance;
	    this.args = args;
	    this.typeData = typeData;
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
    public LookupExpr getCloneWithArgs(List<ILExpr> args) {
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
    	return new LookupExpr(typeData, name, newArgs, tolerance);
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
