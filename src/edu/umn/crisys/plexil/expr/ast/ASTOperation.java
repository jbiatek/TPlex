package edu.umn.crisys.plexil.expr.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILExprBase;
import edu.umn.crisys.plexil.expr.il.ILType;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PInteger;
import edu.umn.crisys.plexil.runtime.values.PNumeric;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;
import edu.umn.crisys.util.Pair;

/**
 * Almost all of Plexil's native operations. 
 * 
 * @author jbiatek
 *
 */
public class ASTOperation extends ILExprBase {
    
    public static ILType getArgType(String opName) {
        return Operator.valueOf(opName.toUpperCase()).argType;
    }
    
    public static ASTOperation construct(String opName, ILExpr...args) {
        return new ASTOperation(Operator.valueOf(opName.toUpperCase()), args);
    }
    
    public static ASTOperation construct(String opName, List<ILExpr> args) {
        return new ASTOperation(Operator.valueOf(opName.toUpperCase()), args);
    }
    
    public static ASTOperation and(ILExpr...args) {
        return new ASTOperation(Operator.AND, args);
    }
    
    public static ASTOperation and(List<ILExpr> args) {
        return new ASTOperation(Operator.AND, args);
    }
    
    public static ASTOperation or(ILExpr...args) {
        return new ASTOperation(Operator.OR, args);
    }
    
    public static ASTOperation or(List<ILExpr> args) {
        return new ASTOperation(Operator.OR, args);
    }
    
    public static ASTOperation xor(ILExpr...args) {
        return new ASTOperation(Operator.XOR, args);
    }

    public static ASTOperation xor(List<ILExpr> args) {
        return new ASTOperation(Operator.XOR, args);
    }
    
    public static ASTOperation not(ILExpr arg) {
        return new ASTOperation(Operator.NOT, arg);
    }
    
    public static ASTOperation eq(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.EQ, one, two);
    }
    
    public static ASTOperation ne(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.NE, one, two);
    }
    
    public static ASTOperation eq(ILExpr one, ILExpr two, ILType extraInfo) {
        return new ASTOperation(Operator.EQ, extraInfo, one, two);
    }
    
    public static ASTOperation ne(ILExpr one, ILExpr two, ILType extraInfo) {
        return new ASTOperation(Operator.NE, extraInfo, one, two);
    }

    
    public static ASTOperation ge(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.GE, one, two);
    }
    
    public static ASTOperation gt(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.GT, one, two);
    }
    
    public static ASTOperation le(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.LE, one, two);
    }
    
    public static ASTOperation lt(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.LT, one, two);
    }
    
    public static ASTOperation isKnown(ILExpr arg) {
        return new ASTOperation(Operator.ISKNOWN, arg);
    }
    
    public static ASTOperation castToBoolean(ILExpr arg) {
        return new ASTOperation(Operator.CAST_BOOL, arg);
    }
    
    public static ASTOperation castToInteger(ILExpr arg) {
        return new ASTOperation(Operator.CAST_INT, arg);
    }
    
    public static ASTOperation castToReal(ILExpr arg) {
        return new ASTOperation(Operator.CAST_REAL, arg);
    }
    
    public static ASTOperation castToString(ILExpr arg) {
        return new ASTOperation(Operator.CAST_STRING, arg);
    }
    
    public static ASTOperation abs(ILExpr arg) {
        return new ASTOperation(Operator.ABS, arg);
    }
    
    public static ASTOperation add(ILExpr...args) {
        return new ASTOperation(Operator.ADD, args);
    }

    public static ASTOperation add(List<ILExpr> args) {
        return new ASTOperation(Operator.ADD, args);
    }
    
    public static ASTOperation div(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.DIV, one, two);
    }
    
    public static ASTOperation max(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.MAX, one, two);
    }
    
    public static ASTOperation min(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.MIN, one, two);
    }
    
    public static ASTOperation mod(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.MOD, one, two);
    }
    
    public static ASTOperation mul(ILExpr...args) {
        return new ASTOperation(Operator.MUL, args);
    }

    public static ASTOperation mul(List<ILExpr> args) {
        return new ASTOperation(Operator.MUL, args);
    }

    public static ASTOperation sqrt(ILExpr arg) {
        return new ASTOperation(Operator.SQRT, arg);
    }
    
    public static ASTOperation sub(ILExpr one, ILExpr two) {
        return new ASTOperation(Operator.SUB, one, two);
    }
    
    public static ASTOperation concat(ILExpr...args) {
        return new ASTOperation(Operator.CONCAT, args);
    }
    
    public static ASTOperation concat(List<ILExpr> args) {
        return new ASTOperation(Operator.CONCAT, args);
    }
    
    public static ASTOperation getState(ILExpr node) {
        return new ASTOperation(Operator.GET_STATE, node);
    }
    public static ASTOperation getOutcome(ILExpr node) {
        return new ASTOperation(Operator.GET_OUTCOME, node);
    }
    public static ASTOperation getFailure(ILExpr node) {
        return new ASTOperation(Operator.GET_FAILURE, node);
    }
    public static ASTOperation getCommandHandle(ILExpr node) {
        return new ASTOperation(Operator.GET_COMMAND_HANDLE, node);
    }
    
    public static ASTOperation arrayIndex(ILExpr array, ILExpr index) {
    	return new ASTOperation(Operator.ARRAY_INDEX, array, index);
    }

    public static enum Operator {
        AND(-1, "&&", ILType.BOOLEAN, ILType.BOOLEAN),
        OR(-1, "||", ILType.BOOLEAN, ILType.BOOLEAN),
        XOR(-1, "XOR", ILType.BOOLEAN, ILType.BOOLEAN),
        NOT(1, "!(", ILType.BOOLEAN, ILType.BOOLEAN),
        
        EQ(2, "==", ILType.UNKNOWN, ILType.BOOLEAN),
        NE(2, "!=", ILType.UNKNOWN, ILType.BOOLEAN),

        GE(2, ">=", ILType.INTEGER, ILType.BOOLEAN),
        GT(2, ">", ILType.INTEGER, ILType.BOOLEAN),
        LE(2, "<=", ILType.INTEGER, ILType.BOOLEAN),
        LT(2, "<", ILType.INTEGER, ILType.BOOLEAN),
        
        ISKNOWN(1, "isKnown(", ILType.UNKNOWN, ILType.BOOLEAN),
        ARRAY_INDEX(2, "[]", ILType.ARRAY, ILType.UNKNOWN),
                
        // TODO: Do we need casting?
        CAST_BOOL(1, "(PBoolean) (", ILType.BOOLEAN, ILType.BOOLEAN),
        CAST_INT(1, "(PInteger) (", ILType.INTEGER, ILType.INTEGER),
        CAST_REAL(1, "(PReal) (", ILType.INTEGER, ILType.REAL),
        CAST_STRING(1, "(PString) (", ILType.STRING, ILType.STRING),
        
        ABS(1, "abs(", ILType.INTEGER, ILType.INTEGER),
        ADD(-1, "+", ILType.INTEGER, ILType.INTEGER),
        DIV(2, "/", ILType.INTEGER, ILType.INTEGER),
        MAX(2, "max(", ILType.INTEGER, ILType.INTEGER),
        MIN(2, "min(", ILType.INTEGER, ILType.INTEGER),
        MOD(2, "%", ILType.INTEGER, ILType.INTEGER),
        MUL(-1, "*", ILType.INTEGER, ILType.INTEGER),
        SQRT(1, "sqrt(", ILType.INTEGER, ILType.INTEGER),
        SUB(2, "-", ILType.INTEGER, ILType.INTEGER),
        
        CONCAT(-1, "+", ILType.STRING, ILType.STRING),
        
        GET_COMMAND_HANDLE(1, ".command_handle", ILType.NODEREF, ILType.COMMAND_HANDLE),
        GET_STATE(1, ".state", ILType.NODEREF, ILType.STATE),
        GET_OUTCOME(1, ".outcome", ILType.NODEREF, ILType.OUTCOME),
        GET_FAILURE(1, ".failure", ILType.NODEREF, ILType.FAILURE),
        
        ;
        private int expectedArgs;
        private String symbol;
        private ILType argType;
        private ILType returnType;
        
        private Operator(int expectedArgs, String symbol, ILType argType, ILType returnType) {
            this.expectedArgs = expectedArgs;
            this.symbol = symbol;
            this.argType = argType;
            this.returnType = returnType;
        }
        
        public PValue eval(List<PValue> values) {
        	if (expectedArgs != -1 && values.size() != expectedArgs) {
        		throw new RuntimeException(this+" didn't expect "+values.size()+" args");
        	}
        	for (PValue p : values) {
        		argType.typeCheck(p.getType());
        	}
        	switch(this) {
        	case ABS:
        		return num(values.get(0)).abs();
        	case ADD:
        		PNumeric sum = IntegerValue.get(0);
        		for (PValue n : values) {
        			sum = sum.add(num(n));
        		}
        		return sum;
        	case AND:
        		PBoolean ret = BooleanValue.get(true);
        		for (PValue b : values) {
        			ret = ret.and(bool(b));
        			if (ret.isFalse()) {
        				return ret;
        			}
        		}
        		return ret;
        	case ARRAY_INDEX:
        		PValueList<?> array = (PValueList<?>) values.get(0);
        		PInteger index = (PInteger) values.get(1);
        		return array.get(index);
        	case CONCAT:
        		PString str = StringValue.get("");
        		for (PValue s : values) {
        			str = str.concat(str(s));
        		}
        		return str;
        	case DIV:
        		return num(values.get(0)).div(num(values.get(1)));
        	case EQ:
        		return values.get(0).equalTo(values.get(1));
        	case GE:
        		return num(values.get(0)).ge(num(values.get(1)));
        	case GT:
        		return num(values.get(0)).gt(num(values.get(1)));
        	case LE:
        		return num(values.get(0)).le(num(values.get(1)));
        	case LT: 
        		return num(values.get(0)).lt(num(values.get(1)));
        	case ISKNOWN:
        		return BooleanValue.get(values.get(0).isKnown());
        	case MAX:
        		return num(values.get(0)).max(num(values.get(1)));
        	case MIN:
        		return num(values.get(0)).min(num(values.get(1)));
        	case MOD:
        		return num(values.get(0)).mod(num(values.get(1)));
        	case MUL:
        		PNumeric mul = IntegerValue.get(1);
        		for (PValue n : values) {
        			mul = mul.mul(num(n));
        		}
        		return mul;
        	case NE:
        		return values.get(0).equalTo(values.get(1)).not();
        	case NOT:
        		return bool(values.get(0)).not();
        	case OR:
        		PBoolean or = BooleanValue.get(false);
        		for (PValue b : values) {
        			or = or.or(bool(b));
        			if (or.isTrue()) {
        				return or;
        			}
        		}
        		return or;
        	case SQRT:
        		return num(values.get(0)).sqrt();
        	case SUB:
        		return num(values.get(0)).sub(num(values.get(1)));
        	case XOR:
        		PBoolean xor = bool(values.get(0));
        		for (int i = 1; i < values.size(); i++) {
        			xor = xor.xor(bool(values.get(i)));
        		}
        		return xor;
        	case CAST_BOOL:
        	case CAST_STRING: 
        		return values.get(0);
        	default:
        		return UnknownValue.get();
        	}
        }
        
        private PNumeric num(PValue p) {
        	return (PNumeric) p;
        }
        private PBoolean bool(PValue p) {
        	return (PBoolean) p;
        }
        private PString str(PValue p) {
        	return (PString) p;
        }
        
        
        private String toString(List<ILExpr> args) {
            String ret;
            String infix;
            String end;
            if (symbol.equals("[]")) {
            	return args.get(0)+"["+args.get(1)+"]";
            } else if (symbol.endsWith("(")) {
                // Function style. Start with name, separate by comma, end with
                // closing parenthesis. 
                ret = symbol;
                infix = ", ";
                end = ")";
            } else if (symbol.startsWith(".")) {
                // Dot style. "argument.symbol" is what we want.
                ret = "";
                infix = "";
                end = symbol;
            } else {
                // Use the symbol as the separator. 
                ret = "";
                infix = " "+symbol+" ";
                end = "";
            }
            
            // Do all but the last one
            for (int i=0; i < args.size() - 1; i++) {
                ret += args.get(i).toString()+infix;
            }
            // Add the last one and the end string
            return ret+args.get(args.size()-1)+end;
        }
        
    }
    
    private Operator op;
    private List<ILExpr> args;
    private ILType argType;
    
    private ASTOperation(Operator op, ILType argType, List<ILExpr> args) {
    	super(op.returnType);
        this.op = op;
        this.args = args;
        this.argType = argType;
        checkArgs();
    }
    
    private ASTOperation(Operator op, List<ILExpr> args) {
    	this(op, op.argType, args);
    }
    
    private ASTOperation(Operator op, ILExpr... args) {
        this(op, op.argType, Arrays.asList(args));
    }
    
    private ASTOperation(Operator op, ILType argType, ILExpr... args) {
        this(op, argType, Arrays.asList(args));
    }
    
    private void checkArgs() {
        if (args.size() == 0) {
            throw new RuntimeException("No arguments at all to "+this+"?");
        } else if (op.expectedArgs != -1 && op.expectedArgs != args.size()) {
            throw new RuntimeException("Expected "+op.expectedArgs+" args, not "+args.size());
        }
        
        if (op == Operator.ARRAY_INDEX) {
        	// Bit of a special case here
        	ILType.ARRAY.typeCheck(args.get(0).getType());
        	ILType.INTEGER.typeCheck(args.get(1).getType());
        	return;
        }
        
        // For casts, we're obviously skipping this. Everything else should 
        // check out, though. 
        if (op != Operator.CAST_BOOL && 
        		op != Operator.CAST_STRING) {
	        for (ILExpr e : args) {
	            argType.typeCheck(e.getType());
	        }
        }
    }

    
    public Operator getOperator() {
        return op;
    }
    
    public ILType getExpectedArgumentType() {
        return argType;
    }

    public ILType getActualArgumentType() {
    	if (argType != ILType.UNKNOWN &&
    			argType != ILType.INTEGER) {
    		return argType;
    	}
    	if (op == Operator.ARRAY_INDEX) {
    		return args.get(0).getType().elementType();
    	}
    	
    	// Numbers might actually be reals, and we might be able to do better
    	// than unknown. 
    	ILType mostSpecific = argType;
    	for (ILExpr e : args) {
    		mostSpecific = argType.getMoreSpecific(e.getType());
    	}
    	return mostSpecific;
    }
    
    @Override
    public List<ILExpr> getArguments() {
        return args;
    }
    
	@Override
	public Optional<PValue> eval() {
		// OR and AND have short circuiting, which is slightly different
		// from everything else. 
		PValue shortCircuiter = null;
		if (this.getOperator() == Operator.AND) {
			shortCircuiter = BooleanValue.get(false);
		} else if (this.getOperator() == Operator.OR) {
			shortCircuiter = BooleanValue.get(true);
		}
		
		List<PValue> values = new ArrayList<PValue>();
		for (ILExpr arg : this.getArguments()) {
			Optional<PValue> argResult = arg.eval();
			if (argResult.isPresent()) {
				if (shortCircuiter != null && argResult.get().equals(shortCircuiter)) {
					// We have short circuited! This is always going to be it.
					return argResult;
				} else {
					// Just a regular constant. 
					values.add(argResult.get());
				}
			} else {
				// Not constant. Could be anything.
				return Optional.empty();
			}
		}
		// All the arguments were constant, so this should be constant too. 
		return Optional.of(this.getOperator().eval(values));
	}

    
    public ILExpr getSingleExpectedArgument() {
    	if (op.expectedArgs == 1 || args.size() == 1) {
    		return args.get(0);
    	} 
    	throw new RuntimeException("Single argument was expected");
    }
    
    public Pair<ILExpr,ILExpr> getBinaryExpectedArguments() {
    	if (op.expectedArgs == 2 || args.size() == 2) {
    		return new Pair<>(args.get(0), args.get(1));
    	} 
    	throw new RuntimeException("Two arguments expected");
    }

    @Override
    public ASTOperation getCloneWithArgs(List<ILExpr> args) {
        return new ASTOperation(op, args);
    }

    @Override
    public String asString() {
        return op.toString(args);
    }
    
    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

	@Override
	public boolean isAssignable() {
		return op == Operator.ARRAY_INDEX;
	}

}
