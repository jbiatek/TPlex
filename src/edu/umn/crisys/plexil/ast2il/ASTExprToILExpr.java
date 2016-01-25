package edu.umn.crisys.plexil.ast2il;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.expr.CascadingExprVisitor;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.NamedCondition;
import edu.umn.crisys.plexil.expr.ast.ASTLookupExpr;
import edu.umn.crisys.plexil.expr.ast.ASTOperation;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.ast.ASTOperation.Operator;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILOperation;
import edu.umn.crisys.plexil.expr.il.ILOperator;
import edu.umn.crisys.plexil.expr.il.LookupExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;

public class ASTExprToILExpr implements CascadingExprVisitor<ExprType, Expression> {
    
    private NodeToIL context;
    
    public ASTExprToILExpr(NodeToIL context) {
        this.context = context;
    }
    
    @Override
	public Expression visit(PValue v, ExprType param) {
    	// Return these as-is, the IL uses them natively.
    	return v;
	}

	@Override
	public Expression visit(ASTLookupExpr lookup, ExprType expected) {
    	List<Expression> translatedArgs = lookup.getLookupArgs().stream()
    			.map(e -> e.accept(this, ExprType.UNKNOWN))
    			.collect(Collectors.toList());
    	Expression translatedName = lookup.getLookupName().accept(this, ExprType.STRING);
    	Optional<Expression> translatedTolerance =
    			lookup.getTolerance().map(t -> t.accept(this, ExprType.REAL));
    	
    	
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
    public Expression visit(UnresolvedVariableExpr expr, ExprType expected) {
        if (expr.getType() == ExprType.NODEREF) {
            throw new RuntimeException("Node references should be resolved by "
                    + "the operation that needs them, they can't be used directly");
        }
        return context.resolveVariable(expr.getName(), expr.getType());
    }

    @Override
	public Expression visit(NodeRefExpr ref, ExprType expected) {
		throw new RuntimeException("This reference should have been resolved by the operation that used it");
	}

    

    private NodeToIL resolveNode(Expression e) {
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
    public Expression visit(NodeTimepointExpr timept, ExprType expected) {
        // This one we actually have to take apart, since it ends up as a 
        // variable in the IL.
        return resolveNode(timept.getNodeId()).getNodeTimepoint(timept.getState(), timept.getTimepoint());
    }

    @Override
    public Expression visit(ASTOperation op, ExprType expected) {
        // Some of these we need to handle specially. Specifically, the node
        // operations like .state and .command_handle.
        switch (op.getOperator()) {
        case GET_STATE:
            return resolveNode(op.getUnaryArg()).getState();
        case GET_OUTCOME:
            return resolveNode(op.getUnaryArg()).getOutcome();
        case GET_FAILURE:
            return resolveNode(op.getUnaryArg()).getFailure();
        case GET_COMMAND_HANDLE:
            return resolveNode(op.getUnaryArg()).getCommandHandle();
        case ARRAY_INDEX:
        	Expression array = op.getBinaryFirst().accept(this, 
        			expected.isSpecificType() ? 
        					expected.toArrayType() : ExprType.UNKNOWN);
        	Expression index = op.getBinarySecond().accept(this, ExprType.INTEGER);
        	return ILOperator.arrayIndex(array, index);
        case NE:
        	// Change to not(equal)
        	ASTOperation fixed = ASTOperation.not(ASTOperation.eq(
        			op.getBinaryFirst(), op.getBinarySecond()));
        	return fixed.accept(this, null);
        case ISKNOWN:
        	// The IL makes ISKNOWN a native boolean, but since this translator
        	// could be anywhere in PLEXIL land, we need to wrap it in a PBoolean. 
        	return ILOperator.WRAP_BOOLEAN.expr(
        			ILOperator.ISKNOWN_OPERATOR.expr(
        					op.getUnaryArg().accept(this, op.getActualArgumentType())
        					));
        default:
        	// We need to figure out the correct type of these arguments. 
        	ExprType expectedArgType = op.getExpectedArgumentType();
        	if (expectedArgType == ExprType.UNKNOWN) {
        		// Hmm, can we infer it from the arguments? 
        		expectedArgType = op.getArguments().stream()
        				.map(Expression::getType)
        				.reduce(ExprType::getMoreSpecific).get();
        		if (expectedArgType == ExprType.UNKNOWN) {
        			// Hm. Well, if it's a Condition, boolean is our
        			// best guess.
        			expectedArgType = ExprType.BOOLEAN;
        		}
        	}
        	if (expectedArgType.isNumeric()) {
        		// Let's check and see if they are all integers
        		if (argsAreAllIntegers(op.getArguments())) {
        			expectedArgType = ExprType.INTEGER;
        		} else {
        			// Nope, make them all reals
        			expectedArgType = ExprType.REAL;
        		}
        	}
        	final ExprType finalGuess = expectedArgType;
        	List<Expression> translated = op.getArguments().stream()
					.map(e -> e.accept(this, finalGuess))
					.collect(Collectors.toList());

        	// The translation to IL might have discovered more type data for
        	// things like Lookups.
        	if (expectedArgType.isNumeric()) {
        		if (argsAreAllIntegers(translated)) {
        			expectedArgType = ExprType.INTEGER;
        		} else {
        			expectedArgType = ExprType.REAL;
        		}
        	}
        	
        	// If we converted to real, there's a special method that does 
        	// that using the right operator.
        	if (expectedArgType == ExprType.REAL) {
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
    
    private Expression cast(Expression e, ExprType expectedType) {
    	switch(expectedType) {
    	case BOOLEAN:
    		return ILOperator.CAST_PBOOL.expr(e);
    	case INTEGER:
    		return ILOperator.CAST_PINT.expr(e);
    	case REAL:
    		return ILOperator.CAST_PREAL.expr(e);
    	case STRING:
    		return ILOperator.CAST_PSTRING.expr(e);
		default:
			throw new RuntimeException("Shouldn't be casting to "+expectedType);
    	}
    }
    
    private ILOperator map(Operator o, ExprType finalType) {
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
    		if (finalType == ExprType.INTEGER) 
    			return ILOperator.PINT_GE;
    		else return ILOperator.PREAL_GE;
    	case GT:
    		if (finalType == ExprType.INTEGER) 
    			return ILOperator.PINT_GT;
    		else return ILOperator.PREAL_GT;
    	case LE:
    		if (finalType == ExprType.INTEGER) 
    			return ILOperator.PINT_LE;
    		else return ILOperator.PREAL_LE;
    	case LT:
    		if (finalType == ExprType.INTEGER) 
    			return ILOperator.PINT_LT;
    		else return ILOperator.PREAL_LT;
    	case ISKNOWN:
    		return ILOperator.ISKNOWN_OPERATOR;
    	case ABS:
    		if (finalType == ExprType.INTEGER)
    			return ILOperator.PINT_ABS;
    		else return ILOperator.PREAL_ABS;
    	case ADD:
    		if (finalType == ExprType.INTEGER)
    			return ILOperator.PINT_ADD;
    		else return ILOperator.PREAL_ADD;
    	case DIV:
    		if (finalType == ExprType.INTEGER)
    			return ILOperator.PINT_DIV;
    		else return ILOperator.PREAL_DIV;
    	case MAX:
    		if (finalType == ExprType.INTEGER)
    			return ILOperator.PINT_MAX;
    		else return ILOperator.PREAL_MAX;
    	case MIN:
    		if (finalType == ExprType.INTEGER)
    			return ILOperator.PINT_MIN;
    		else return ILOperator.PREAL_MIN;
    	case MOD:
    		if (finalType == ExprType.INTEGER)
    			return ILOperator.PINT_MOD;
    		else return ILOperator.PREAL_MOD;
    	case MUL:
    		if (finalType == ExprType.INTEGER)
    			return ILOperator.PINT_MUL;
    		else return ILOperator.PREAL_MUL;
    	case SQRT:
    		return ILOperator.PREAL_SQRT;
    	case SUB:
    		if (finalType == ExprType.INTEGER)
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
    
    private boolean argsAreAllIntegers(List<Expression> translatedChildren) {
    	return translatedChildren.stream()
    			.allMatch(e -> e.getType() == ExprType.INTEGER
    							|| e.getType() == ExprType.UNKNOWN);
    }
    
    private List<Expression> allIntsToReals(List<Expression> translatedChildren) {
    	return translatedChildren.stream()
    			.map(e -> e.getType() == ExprType.INTEGER ? 
    					ILOperator.TO_PREAL.expr(e) : e)
    			.collect(Collectors.toList());
    }
    
    private Expression visitILExpr(Expression e) {
    	throw new RuntimeException(e+" is already an IL-only expr, something is "
    			+ "probably very wrong here");
    }
    
	@Override
	public Expression visit(NativeBool b, ExprType expected) {
		return visitILExpr(b);
	}

	@Override
	public Expression visit(ILOperation op, ExprType expected) {
		return visitILExpr(op);
	}

	@Override
	public Expression visit(GetNodeStateExpr state, ExprType expected) {
		return visitILExpr(state);
	}

	@Override
	public Expression visit(SimpleVar var, ExprType expected) {
		return visitILExpr(var);
	}

	@Override
	public Expression visit(ArrayVar array, ExprType expected) {
		return visitILExpr(array);
	}

	@Override
	public Expression visit(LibraryVar lib, ExprType expected) {
		return visitILExpr(lib);
	}

	@Override
	public Expression visit(NamedCondition named, ExprType expected) {
		return visitILExpr(named);
	}

	@Override
	public Expression visit(AliasExpr alias, ExprType expected) {
		return visitILExpr(alias);
	}

	@Override
	public Expression visit(RootAncestorExpr root, ExprType expected) {
		return visitILExpr(root);
	}

	@Override
	public Expression visit(ILVariable v, ExprType expected) {
		return visitILExpr(v);
	}

}
