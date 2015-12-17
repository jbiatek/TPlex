package edu.umn.crisys.plexil.expr.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PNumeric;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;
import edu.umn.crisys.util.Pair;

/**
 * Almost all of Plexil's native operations. 
 * 
 * @author jbiatek
 *
 */
public class ASTOperation extends ExpressionBase {
    
    public static ExprType getArgType(String opName) {
        return Operator.valueOf(opName.toUpperCase()).argType;
    }
    
    public static ASTOperation construct(String opName, Expression...args) {
        return new ASTOperation(Operator.valueOf(opName.toUpperCase()), args);
    }
    
    public static ASTOperation construct(String opName, List<Expression> args) {
        return new ASTOperation(Operator.valueOf(opName.toUpperCase()), args);
    }
    
    public static ASTOperation and(Expression...args) {
        return new ASTOperation(Operator.AND, args);
    }
    
    public static ASTOperation and(List<Expression> args) {
        return new ASTOperation(Operator.AND, args);
    }
    
    public static ASTOperation or(Expression...args) {
        return new ASTOperation(Operator.OR, args);
    }
    
    public static ASTOperation or(List<Expression> args) {
        return new ASTOperation(Operator.OR, args);
    }
    
    public static ASTOperation xor(Expression...args) {
        return new ASTOperation(Operator.XOR, args);
    }

    public static ASTOperation xor(List<Expression> args) {
        return new ASTOperation(Operator.XOR, args);
    }
    
    public static ASTOperation not(Expression arg) {
        return new ASTOperation(Operator.NOT, arg);
    }
    
    public static ASTOperation eq(Expression one, Expression two) {
        return new ASTOperation(Operator.EQ, one, two);
    }
    
    public static ASTOperation ne(Expression one, Expression two) {
        return new ASTOperation(Operator.NE, one, two);
    }
    
    public static ASTOperation eq(Expression one, Expression two, ExprType extraInfo) {
        return new ASTOperation(Operator.EQ, extraInfo, one, two);
    }
    
    public static ASTOperation ne(Expression one, Expression two, ExprType extraInfo) {
        return new ASTOperation(Operator.NE, extraInfo, one, two);
    }

    
    public static ASTOperation ge(Expression one, Expression two) {
        return new ASTOperation(Operator.GE, one, two);
    }
    
    public static ASTOperation gt(Expression one, Expression two) {
        return new ASTOperation(Operator.GT, one, two);
    }
    
    public static ASTOperation le(Expression one, Expression two) {
        return new ASTOperation(Operator.LE, one, two);
    }
    
    public static ASTOperation lt(Expression one, Expression two) {
        return new ASTOperation(Operator.LT, one, two);
    }
    
    public static ASTOperation isKnown(Expression arg) {
        return new ASTOperation(Operator.ISKNOWN, arg);
    }
    
    public static ASTOperation castToBoolean(Expression arg) {
        return new ASTOperation(Operator.CAST_BOOL, arg);
    }
    
    public static ASTOperation castToInteger(Expression arg) {
        return new ASTOperation(Operator.CAST_INT, arg);
    }
    
    public static ASTOperation castToReal(Expression arg) {
        return new ASTOperation(Operator.CAST_REAL, arg);
    }
    
    public static ASTOperation castToString(Expression arg) {
        return new ASTOperation(Operator.CAST_STRING, arg);
    }
    
    public static ASTOperation abs(Expression arg) {
        return new ASTOperation(Operator.ABS, arg);
    }
    
    public static ASTOperation add(Expression...args) {
        return new ASTOperation(Operator.ADD, args);
    }

    public static ASTOperation add(List<Expression> args) {
        return new ASTOperation(Operator.ADD, args);
    }
    
    public static ASTOperation div(Expression one, Expression two) {
        return new ASTOperation(Operator.DIV, one, two);
    }
    
    public static ASTOperation max(Expression one, Expression two) {
        return new ASTOperation(Operator.MAX, one, two);
    }
    
    public static ASTOperation min(Expression one, Expression two) {
        return new ASTOperation(Operator.MIN, one, two);
    }
    
    public static ASTOperation mod(Expression one, Expression two) {
        return new ASTOperation(Operator.MOD, one, two);
    }
    
    public static ASTOperation mul(Expression...args) {
        return new ASTOperation(Operator.MUL, args);
    }

    public static ASTOperation mul(List<Expression> args) {
        return new ASTOperation(Operator.MUL, args);
    }

    public static ASTOperation sqrt(Expression arg) {
        return new ASTOperation(Operator.SQRT, arg);
    }
    
    public static ASTOperation sub(Expression one, Expression two) {
        return new ASTOperation(Operator.SUB, one, two);
    }
    
    public static ASTOperation concat(Expression...args) {
        return new ASTOperation(Operator.CONCAT, args);
    }
    
    public static ASTOperation concat(List<Expression> args) {
        return new ASTOperation(Operator.CONCAT, args);
    }
    
    public static ASTOperation getState(Expression node) {
        return new ASTOperation(Operator.GET_STATE, node);
    }
    public static ASTOperation getOutcome(Expression node) {
        return new ASTOperation(Operator.GET_OUTCOME, node);
    }
    public static ASTOperation getFailure(Expression node) {
        return new ASTOperation(Operator.GET_FAILURE, node);
    }
    public static ASTOperation getCommandHandle(Expression node) {
        return new ASTOperation(Operator.GET_COMMAND_HANDLE, node);
    }

    public static enum Operator {
        AND(-1, "&&", ExprType.BOOLEAN, ExprType.BOOLEAN),
        OR(-1, "||", ExprType.BOOLEAN, ExprType.BOOLEAN),
        XOR(-1, "XOR", ExprType.BOOLEAN, ExprType.BOOLEAN),
        NOT(1, "!(", ExprType.BOOLEAN, ExprType.BOOLEAN),
        
        EQ(2, "==", ExprType.UNKNOWN, ExprType.BOOLEAN),
        NE(2, "!=", ExprType.UNKNOWN, ExprType.BOOLEAN),

        GE(2, ">=", ExprType.INTEGER, ExprType.BOOLEAN),
        GT(2, ">", ExprType.INTEGER, ExprType.BOOLEAN),
        LE(2, "<=", ExprType.INTEGER, ExprType.BOOLEAN),
        LT(2, "<", ExprType.INTEGER, ExprType.BOOLEAN),
        
        ISKNOWN(1, "isKnown(", ExprType.UNKNOWN, ExprType.BOOLEAN),
                
        // TODO: Do we need casting?
        CAST_BOOL(1, "(PBoolean) (", ExprType.BOOLEAN, ExprType.BOOLEAN),
        CAST_INT(1, "(PInteger) (", ExprType.INTEGER, ExprType.INTEGER),
        CAST_REAL(1, "(PReal) (", ExprType.INTEGER, ExprType.REAL),
        CAST_STRING(1, "(PString) (", ExprType.STRING, ExprType.STRING),
        
        ABS(1, "abs(", ExprType.INTEGER, ExprType.INTEGER),
        ADD(-1, "+", ExprType.INTEGER, ExprType.INTEGER),
        DIV(2, "/", ExprType.INTEGER, ExprType.INTEGER),
        MAX(2, "max(", ExprType.INTEGER, ExprType.INTEGER),
        MIN(2, "min(", ExprType.INTEGER, ExprType.INTEGER),
        MOD(2, "%", ExprType.INTEGER, ExprType.INTEGER),
        MUL(-1, "*", ExprType.INTEGER, ExprType.INTEGER),
        SQRT(1, "sqrt(", ExprType.INTEGER, ExprType.INTEGER),
        SUB(2, "-", ExprType.INTEGER, ExprType.INTEGER),
        
        CONCAT(-1, "+", ExprType.STRING, ExprType.STRING),
        
        GET_COMMAND_HANDLE(1, ".command_handle", ExprType.NODEREF, ExprType.COMMAND_HANDLE),
        GET_STATE(1, ".state", ExprType.NODEREF, ExprType.STATE),
        GET_OUTCOME(1, ".outcome", ExprType.NODEREF, ExprType.OUTCOME),
        GET_FAILURE(1, ".failure", ExprType.NODEREF, ExprType.FAILURE),
        
        ;
        private int expectedArgs;
        private String symbol;
        private ExprType argType;
        private ExprType returnType;
        
        private Operator(int expectedArgs, String symbol, ExprType argType, ExprType returnType) {
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
        
        
        private String toString(List<Expression> args) {
            String ret;
            String infix;
            String end;
            if (symbol.endsWith("(")) {
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
    private List<Expression> args;
    private ExprType argType;
    
    private ASTOperation(Operator op, ExprType argType, List<Expression> args) {
    	super(op.returnType);
        this.op = op;
        this.args = args;
        this.argType = argType;
        checkArgs();
    }
    
    private ASTOperation(Operator op, List<Expression> args) {
    	this(op, op.argType, args);
    }
    
    private ASTOperation(Operator op, Expression... args) {
        this(op, op.argType, Arrays.asList(args));
    }
    
    private ASTOperation(Operator op, ExprType argType, Expression... args) {
        this(op, argType, Arrays.asList(args));
    }
    
    private void checkArgs() {
        if (args.size() == 0) {
            throw new RuntimeException("No arguments at all to "+this+"?");
        } else if (op.expectedArgs != -1 && op.expectedArgs != args.size()) {
            throw new RuntimeException("Expected "+op.expectedArgs+" args, not "+args.size());
        }
        // For casts, we're obviously skipping this. Everything else should 
        // check out, though. 
        if (op != Operator.CAST_BOOL && 
        		op != Operator.CAST_STRING) {
	        for (Expression e : args) {
	            argType.typeCheck(e.getType());
	        }
        }
    }

    
    public Operator getOperator() {
        return op;
    }
    
    public ExprType getExpectedArgumentType() {
        return argType;
    }

    public ExprType getActualArgumentType() {
    	if (argType != ExprType.UNKNOWN &&
    			argType != ExprType.INTEGER) {
    		return argType;
    	}
    	// Numbers might actually be reals, and we might be able to do better
    	// than unknown. 
    	ExprType mostSpecific = argType;
    	for (Expression e : args) {
    		mostSpecific = argType.getMoreSpecific(e.getType());
    	}
    	return mostSpecific;
    }
    
    @Override
    public List<Expression> getArguments() {
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
		for (Expression arg : this.getArguments()) {
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

    
    public Expression getSingleExpectedArgument() {
    	if (op.expectedArgs == 1 || args.size() == 1) {
    		return args.get(0);
    	} 
    	throw new RuntimeException("Single argument was expected");
    }
    
    public Pair<Expression,Expression> getBinaryExpectedArguments() {
    	if (op.expectedArgs == 2 || args.size() == 2) {
    		return new Pair<>(args.get(0), args.get(1));
    	} 
    	throw new RuntimeException("Two arguments expected");
    }

    @Override
    public ASTOperation getCloneWithArgs(List<Expression> args) {
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
		return false;
	}

}
