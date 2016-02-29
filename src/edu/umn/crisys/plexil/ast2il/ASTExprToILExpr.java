package edu.umn.crisys.plexil.ast2il;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.ASTLookupExpr;
import edu.umn.crisys.plexil.ast.expr.ASTOperation;
import edu.umn.crisys.plexil.ast.expr.NodeRefExpr;
import edu.umn.crisys.plexil.ast.expr.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.expr.PlexilExpr;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.ast.expr.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.expr.ASTOperation.Operator;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILOperator;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.LookupExpr;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class ASTExprToILExpr extends ASTExprVisitor<PlexilType, ILExpr> {
    
    private NodeToIL context;
    
    public ASTExprToILExpr(NodeToIL context) {
        this.context = context;
    }
    
	@Override
	public ILExpr visit(ASTLookupExpr lookup, PlexilType expected) {
    	List<ILExpr> translatedArgs = lookup.getLookupArgs().stream()
    			.map(e -> e.accept(this, PlexilType.UNKNOWN))
    			.collect(Collectors.toList());
    	ILExpr translatedName = lookup.getLookupName().accept(this, PlexilType.STRING);
    	Optional<ILExpr> translatedTolerance =
    			lookup.getTolerance().map(t -> t.accept(this, PlexilType.REAL));
    	
    	
    	// Find the lookup declaration
    	Optional<LookupDecl> decl = context.getLookupDeclaration(lookup);
    	if (decl.isPresent()) {
    		// Use it to create the lookup.
    		return new LookupExpr(decl.get(), translatedName, 
        			translatedArgs, translatedTolerance);
    	} else if (translatedName.equals(StringValue.get("time"))) {
    		// This is built in to Plexil, we don't need to find a declaration
    		return LookupExpr.lookupTime(NodeToIL.TIMEPOINT_TYPE);
    	} else {
    		// Might be a lookup with a dynamic name, might be undeclared.
    		// Have we been given enough type information to guess?
    		if (expected.isSpecificType()) {
    			// Yeah. We'll just make something up then I guess.
    			LookupDecl fakeDecl = new LookupDecl("<guessed>");
    			fakeDecl.setReturnValue(new VariableDecl("<guessed>", expected));
    			return new LookupExpr(fakeDecl, translatedName, translatedArgs, 
    					translatedTolerance);
    		} else {
    			throw new RuntimeException(
    					"Error translating lookup "+lookup+": "
    					+"Type was not declared, and expected type was "+expected    					);
    		}
    	}
    	
	}
	
	@Override
    public ILExpr visit(UnresolvedVariableExpr expr, PlexilType expected) {
        if (expr.getPlexilType() == PlexilType.NODEREF) {
            throw new RuntimeException("Node references should be resolved by "
                    + "the operation that needs them, they can't be used directly");
        }
        return context.resolveVariable(expr.getName(), expr.getPlexilType().toILType());
    }

    @Override
	public ILExpr visit(NodeRefExpr ref, PlexilType expected) {
		throw new RuntimeException("This reference should have been resolved by the operation that used it");
	}

    

    private NodeToIL resolveNode(PlexilExpr e) {
    	if (e instanceof UnresolvedVariableExpr) {
    		UnresolvedVariableExpr nodeName = (UnresolvedVariableExpr) e;
    		return context.resolveNode(nodeName.getName());
    	} else if (e instanceof NodeRefExpr) {
    		NodeRefExpr ref = (NodeRefExpr) e;
    		switch (ref) {
    		case PARENT: 
    			return context.getParent().orElseThrow(
    					() -> new RuntimeException(context+" has no parent!"));
    		case CHILD: 
    			if (context.getChildren().size() != 1) {
    				throw new RuntimeException("Which child?");
    			}
    			return context.getChildren().get(0);
    		case SIBLING:
    			NodeToIL parent = context.getParent().orElseThrow(
    					() -> new RuntimeException(context+" has no parent!"));
    			if (parent.getChildren().size() != 2) {
    				throw new RuntimeException("Which sibling?");
    			}
    			NodeToIL sibling0 = parent.getChildren().get(0);
    			NodeToIL sibling1 = parent.getChildren().get(1);
    			if (sibling0 == context) {
    				return sibling1;
    			} else {
    				return sibling0;
    			}
    		case SELF:
    			return context;
    		default: 
    			throw new RuntimeException("Missing case: "+ref);
    		}
    	}
    	else {
    		throw new RuntimeException("How do you resolve a node given a "+e.getClass()+"?");
    	}
    }
    
    @Override
    public ILExpr visit(NodeTimepointExpr timept, PlexilType expected) {
        // This one we actually have to take apart, since it ends up as a 
        // variable in the IL.
        return resolveNode(timept.getNodeId()).getNodeTimepoint(timept.getState(), timept.getTimepoint());
    }

    private static PlexilExpr getSoleArgument(PlexilExpr e) {
    	if (e.getPlexilArguments().size() != 1) {
    		throw new RuntimeException("Expected 1 argument for "+e
    				+" but there were "+e.getPlexilArguments().size());
    	}
    	return e.getPlexilArguments().get(0);
    }
    
    private static PlexilExpr getBinaryFirst(PlexilExpr e) {
    	if (e.getPlexilArguments().size() != 2) {
    		throw new RuntimeException("Expected 2 arguments for "+e
    				+" but there were "+e.getPlexilArguments().size());
    	}
    	return e.getPlexilArguments().get(0);
    }
    
    private static PlexilExpr getBinarySecond(PlexilExpr e) {
    	if (e.getPlexilArguments().size() != 2) {
    		throw new RuntimeException("Expected 2 arguments for "+e
    				+" but there were "+e.getPlexilArguments().size());
    	}
    	return e.getPlexilArguments().get(1);
    }
    
    @Override
    public ILExpr visit(ASTOperation op, PlexilType expected) {
        // Some of these we need to handle specially. Specifically, the node
        // operations like .state and .command_handle.
        switch (op.getOperator()) {
        case GET_STATE:
            return resolveNode(getSoleArgument(op)).getState();
        case GET_OUTCOME:
            return resolveNode(getSoleArgument(op)).getOutcome();
        case GET_FAILURE:
            return resolveNode(getSoleArgument(op)).getFailure();
        case GET_COMMAND_HANDLE:
            return resolveNode(getSoleArgument(op)).getCommandHandle();
        case ARRAY_INDEX:
        	ILExpr array = getBinaryFirst(op).accept(this, 
        			expected.isSpecificType() ? 
        					expected.toArrayType() : PlexilType.UNKNOWN);
        	ILExpr index = getBinarySecond(op).accept(this, PlexilType.INTEGER);
        	return ILOperator.arrayIndex(array, index);
        case NE:
        	// Change to not(equal)
        	ASTOperation fixed = ASTOperation.not(ASTOperation.eq(
        			getBinaryFirst(op), getBinarySecond(op)));
        	return fixed.accept(this, null);
        case ISKNOWN:
        	// The IL makes ISKNOWN a native boolean, but since this translator
        	// could be anywhere in PLEXIL land, we need to wrap it in a PBoolean. 
        	return ILOperator.WRAP_BOOLEAN.expr(
        			ILOperator.ISKNOWN_OPERATOR.expr(
        					getSoleArgument(op).accept(this, op.getActualArgumentType())
        					));
        default:
        	// We need to figure out the correct type of these arguments. 
        	PlexilType expectedArgType = op.getExpectedArgumentType();
        	if (expectedArgType == PlexilType.UNKNOWN) {
        		// Hmm, can we infer it from the arguments? 
        		expectedArgType = op.getPlexilArguments().stream()
        				.map(PlexilExpr::getPlexilType)
        				.reduce(PlexilType::getMoreSpecific).get();
        		if (expectedArgType == PlexilType.UNKNOWN) {
        			// Hm. Well, if it's a Condition, boolean is our
        			// best guess.
        			expectedArgType = PlexilType.BOOLEAN;
        		}
        	}
        	if (expectedArgType.isNumeric()) {
        		// Let's check and see if they are all integers
        		if (argsAreAllIntegersAST(op.getPlexilArguments())) {
        			expectedArgType = PlexilType.INTEGER;
        		} else {
        			// Nope, make them all reals
        			expectedArgType = PlexilType.REAL;
        		}
        	}
        	final PlexilType finalGuess = expectedArgType;
        	List<ILExpr> translated = op.getPlexilArguments().stream()
					.map(e -> e.accept(this, finalGuess))
					.collect(Collectors.toList());

        	// The translation to IL might have discovered more type data for
        	// things like Lookups.
        	if (expectedArgType.isNumeric()) {
        		if (argsAreAllIntegers(translated)) {
        			expectedArgType = PlexilType.INTEGER;
        		} else {
        			expectedArgType = PlexilType.REAL;
        		}
        	}
        	
        	// If we converted to real, there's a special method that does 
        	// that using the right operator.
        	if (expectedArgType == PlexilType.REAL) {
        		translated = allIntsToReals(translated);
        	} 
        	
        	// Finally, get the operation that these arguments get passed to.
        	ILOperator opToUse = map(op.getOperator(), expectedArgType);
        	if (! opToUse.isAcceptableArgNumber(translated.size())) {
        		// If there's only one, we're probably seeing that dumb thing
        		// where an AND operator has 1 argument. Just strip the
        		// operator entirely.
        		if (translated.size() == 1) {
        			return translated.get(0);
        		}
        		// The other likely possibility is that it's an AND or similar
        		// with many arguments. The "expr" method will take care of
        		// folding that for us. 
        	}
        	
        	// And pass it in there!
        	return map(op.getOperator(), expectedArgType).expr(translated);
        }
    }
    
    private ILOperator map(Operator o, PlexilType finalType) {
    	switch (o) {
    	case AND: return ILOperator.PAND;
    	case OR: return ILOperator.POR;
    	case XOR: return ILOperator.PXOR;
    	case NOT: return ILOperator.PNOT;
    	
    	case EQ:
    		switch(finalType) {
    		case BOOLEAN: return ILOperator.PBOOL_EQ;
    		case INTEGER: return ILOperator.PINT_EQ;
    		case REAL: return ILOperator.PREAL_EQ;
    		case STRING: return ILOperator.PSTRING_EQ;
    		case STATE: return ILOperator.PSTATE_EQ;
    		case OUTCOME: return ILOperator.POUTCOME_EQ;
    		case FAILURE: return ILOperator.PFAILURE_EQ;
    		case COMMAND_HANDLE: return ILOperator.PHANDLE_EQ;
    		default:
    			throw new RuntimeException("missing case "+finalType);
    		}
    	case NE:
    		throw new RuntimeException("Check for NE first and separate it");
    	case GE:
    		if (finalType == PlexilType.INTEGER) 
    			return ILOperator.PINT_GE;
    		else return ILOperator.PREAL_GE;
    	case GT:
    		if (finalType == PlexilType.INTEGER) 
    			return ILOperator.PINT_GT;
    		else return ILOperator.PREAL_GT;
    	case LE:
    		if (finalType == PlexilType.INTEGER) 
    			return ILOperator.PINT_LE;
    		else return ILOperator.PREAL_LE;
    	case LT:
    		if (finalType == PlexilType.INTEGER) 
    			return ILOperator.PINT_LT;
    		else return ILOperator.PREAL_LT;
    	case ISKNOWN:
    		return ILOperator.ISKNOWN_OPERATOR;
    	case ABS:
    		if (finalType == PlexilType.INTEGER)
    			return ILOperator.PINT_ABS;
    		else return ILOperator.PREAL_ABS;
    	case ADD:
    		if (finalType == PlexilType.INTEGER)
    			return ILOperator.PINT_ADD;
    		else return ILOperator.PREAL_ADD;
    	case DIV:
    		if (finalType == PlexilType.INTEGER)
    			return ILOperator.PINT_DIV;
    		else return ILOperator.PREAL_DIV;
    	case MAX:
    		if (finalType == PlexilType.INTEGER)
    			return ILOperator.PINT_MAX;
    		else return ILOperator.PREAL_MAX;
    	case MIN:
    		if (finalType == PlexilType.INTEGER)
    			return ILOperator.PINT_MIN;
    		else return ILOperator.PREAL_MIN;
    	case MOD:
    		if (finalType == PlexilType.INTEGER)
    			return ILOperator.PINT_MOD;
    		else return ILOperator.PREAL_MOD;
    	case MUL:
    		if (finalType == PlexilType.INTEGER)
    			return ILOperator.PINT_MUL;
    		else return ILOperator.PREAL_MUL;
    	case SQRT:
    		return ILOperator.PREAL_SQRT;
    	case SUB:
    		if (finalType == PlexilType.INTEGER)
    			return ILOperator.PINT_SUB;
    		else return ILOperator.PREAL_SUB;
    	
    	case CONCAT:
    		return ILOperator.PSTR_CONCAT;
    	
    	
    	case CAST_BOOL:
    		return ILOperator.CAST_PBOOL;
    	case CAST_INT:
    		return ILOperator.CAST_PINT;
    	case CAST_REAL:
    		return ILOperator.CAST_PREAL;
    	case CAST_STRING:
    		return ILOperator.CAST_PSTRING;
    		
    	default: 
    		throw new RuntimeException("Missing case: "+o);
    	}
    }
    
    private boolean argsAreAllIntegersAST(List<PlexilExpr> args) {
    	return args.stream()
    			.allMatch(e -> e.getPlexilType() == PlexilType.INTEGER
    					|| e.getPlexilType() == PlexilType.UNKNOWN);
    }
    
    private boolean argsAreAllIntegers(List<ILExpr> translatedChildren) {
    	return translatedChildren.stream()
    			.allMatch(e -> e.getType() == ILType.INTEGER
    							|| e.getType() == ILType.UNKNOWN);
    }
    
    private List<ILExpr> allIntsToReals(List<ILExpr> translatedChildren) {
    	return translatedChildren.stream()
    			.map(e -> e.getType() == ILType.INTEGER ? 
    					ILOperator.TO_PREAL.expr(e) : e)
    			.collect(Collectors.toList());
    }
    
    /*
     * The rest are very easy: they're already IL types! 
     */

	@Override
	public ILExpr visit(BooleanValue bool, PlexilType param) {
		return bool;
	}

	@Override
	public ILExpr visit(IntegerValue integer, PlexilType param) {
		return integer;
	}

	@Override
	public ILExpr visit(RealValue real, PlexilType param) {
		return real;
	}

	@Override
	public ILExpr visit(StringValue string, PlexilType param) {
		return string;
	}

	@Override
	public ILExpr visit(UnknownValue unk, PlexilType param) {
		return unk;
	}

	@Override
	public ILExpr visit(PValueList<?> list, PlexilType param) {
		return list;
	}

	@Override
	public ILExpr visit(CommandHandleState state, PlexilType param) {
		return state;
	}

	@Override
	public ILExpr visit(NodeFailureType type, PlexilType param) {
		return type;
	}

	@Override
	public ILExpr visit(NodeOutcome outcome, PlexilType param) {
		return outcome;
	}

	@Override
	public ILExpr visit(NodeState state, PlexilType param) {
		return state;
	}
    
}
