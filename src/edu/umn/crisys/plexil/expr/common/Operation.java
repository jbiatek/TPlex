package edu.umn.crisys.plexil.expr.common;

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
public class Operation extends ExpressionBase {
    
    public static ExprType getArgType(String opName) {
        return Operator.valueOf(opName.toUpperCase()).argType;
    }
    
    public static Operation construct(String opName, Expression...args) {
        return new Operation(Operator.valueOf(opName.toUpperCase()), args);
    }
    
    public static Operation construct(String opName, List<Expression> args) {
        return new Operation(Operator.valueOf(opName.toUpperCase()), args);
    }
    
    public static Operation and(Expression...args) {
        return new Operation(Operator.AND, args);
    }
    
    public static Operation and(List<Expression> args) {
        return new Operation(Operator.AND, args);
    }
    
    public static Operation or(Expression...args) {
        return new Operation(Operator.OR, args);
    }
    
    public static Operation or(List<Expression> args) {
        return new Operation(Operator.OR, args);
    }
    
    public static Operation xor(Expression...args) {
        return new Operation(Operator.XOR, args);
    }

    public static Operation xor(List<Expression> args) {
        return new Operation(Operator.XOR, args);
    }
    
    public static Operation not(Expression arg) {
        return new Operation(Operator.NOT, arg);
    }
    
    public static Operation eq(Expression one, Expression two) {
        return new Operation(Operator.EQ, one, two);
    }
    
    public static Operation ne(Expression one, Expression two) {
        return new Operation(Operator.NE, one, two);
    }
    
    public static Operation eq(Expression one, Expression two, ExprType extraInfo) {
        return new Operation(Operator.EQ, extraInfo, one, two);
    }
    
    public static Operation ne(Expression one, Expression two, ExprType extraInfo) {
        return new Operation(Operator.NE, extraInfo, one, two);
    }

    
    public static Operation ge(Expression one, Expression two) {
        return new Operation(Operator.GE, one, two);
    }
    
    public static Operation gt(Expression one, Expression two) {
        return new Operation(Operator.GT, one, two);
    }
    
    public static Operation le(Expression one, Expression two) {
        return new Operation(Operator.LE, one, two);
    }
    
    public static Operation lt(Expression one, Expression two) {
        return new Operation(Operator.LT, one, two);
    }
    
    public static Operation isKnown(Expression arg) {
        return new Operation(Operator.ISKNOWN, arg);
    }
    
    public static Operation castToBoolean(Expression arg) {
        return new Operation(Operator.CAST_BOOL, arg);
    }
    
    public static Operation castToNumeric(Expression arg) {
        return new Operation(Operator.CAST_NUMERIC, arg);
    }
    
    public static Operation castToInteger(Expression arg) {
        return new Operation(Operator.CAST_INT, arg);
    }
    
    public static Operation castToReal(Expression arg) {
        return new Operation(Operator.CAST_REAL, arg);
    }
    
    public static Operation castToString(Expression arg) {
        return new Operation(Operator.CAST_STRING, arg);
    }
    
    public static Operation abs(Expression arg) {
        return new Operation(Operator.ABS, arg);
    }
    
    public static Operation add(Expression...args) {
        return new Operation(Operator.ADD, args);
    }

    public static Operation add(List<Expression> args) {
        return new Operation(Operator.ADD, args);
    }
    
    public static Operation div(Expression one, Expression two) {
        return new Operation(Operator.DIV, one, two);
    }
    
    public static Operation max(Expression one, Expression two) {
        return new Operation(Operator.MAX, one, two);
    }
    
    public static Operation min(Expression one, Expression two) {
        return new Operation(Operator.MIN, one, two);
    }
    
    public static Operation mod(Expression one, Expression two) {
        return new Operation(Operator.MOD, one, two);
    }
    
    public static Operation mul(Expression...args) {
        return new Operation(Operator.MUL, args);
    }

    public static Operation mul(List<Expression> args) {
        return new Operation(Operator.MUL, args);
    }

    public static Operation sqrt(Expression arg) {
        return new Operation(Operator.SQRT, arg);
    }
    
    public static Operation sub(Expression one, Expression two) {
        return new Operation(Operator.SUB, one, two);
    }
    
    public static Operation concat(Expression...args) {
        return new Operation(Operator.CONCAT, args);
    }
    
    public static Operation concat(List<Expression> args) {
        return new Operation(Operator.CONCAT, args);
    }
    
    public static Operation getState(Expression node) {
        return new Operation(Operator.GET_STATE, node);
    }
    public static Operation getOutcome(Expression node) {
        return new Operation(Operator.GET_OUTCOME, node);
    }
    public static Operation getFailure(Expression node) {
        return new Operation(Operator.GET_FAILURE, node);
    }
    public static Operation getCommandHandle(Expression node) {
        return new Operation(Operator.GET_COMMAND_HANDLE, node);
    }

    public static enum Operator {
        AND(-1, "&&", ExprType.BOOLEAN, ExprType.BOOLEAN),
        OR(-1, "||", ExprType.BOOLEAN, ExprType.BOOLEAN),
        XOR(-1, "XOR", ExprType.BOOLEAN, ExprType.BOOLEAN),
        NOT(1, "!(", ExprType.BOOLEAN, ExprType.BOOLEAN),
        
        EQ(2, "==", ExprType.UNKNOWN, ExprType.BOOLEAN),
        NE(2, "!=", ExprType.UNKNOWN, ExprType.BOOLEAN),

        GE(2, ">=", ExprType.NUMERIC, ExprType.BOOLEAN),
        GT(2, ">", ExprType.NUMERIC, ExprType.BOOLEAN),
        LE(2, "<=", ExprType.NUMERIC, ExprType.BOOLEAN),
        LT(2, "<", ExprType.NUMERIC, ExprType.BOOLEAN),
        
        ISKNOWN(1, "isKnown(", ExprType.UNKNOWN, ExprType.BOOLEAN),
                
        // TODO: Do we need casting?
        CAST_BOOL(1, "(PBoolean) (", ExprType.BOOLEAN, ExprType.BOOLEAN),
        CAST_NUMERIC(1, "(PNumeric) (", ExprType.NUMERIC, ExprType.NUMERIC),
        CAST_INT(1, "(PInteger) (", ExprType.NUMERIC, ExprType.INTEGER),
        CAST_REAL(1, "(PReal) (", ExprType.NUMERIC, ExprType.REAL),
        CAST_STRING(1, "(PString) (", ExprType.STRING, ExprType.STRING),
        
        ABS(1, "abs(", ExprType.NUMERIC, ExprType.NUMERIC),
        ADD(-1, "+", ExprType.NUMERIC, ExprType.NUMERIC),
        DIV(2, "/", ExprType.NUMERIC, ExprType.NUMERIC),
        MAX(2, "max(", ExprType.NUMERIC, ExprType.NUMERIC),
        MIN(2, "min(", ExprType.NUMERIC, ExprType.NUMERIC),
        MOD(2, "%", ExprType.NUMERIC, ExprType.NUMERIC),
        MUL(-1, "*", ExprType.NUMERIC, ExprType.NUMERIC),
        SQRT(1, "sqrt(", ExprType.NUMERIC, ExprType.NUMERIC),
        SUB(2, "-", ExprType.NUMERIC, ExprType.NUMERIC),
        
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
        	case CAST_NUMERIC:
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
    
    private Operation(Operator op, ExprType argType, List<Expression> args) {
    	super(op.returnType);
        this.op = op;
        this.args = args;
        this.argType = argType;
        checkArgs();
    }
    
    private Operation(Operator op, List<Expression> args) {
    	this(op, op.argType, args);
    }
    
    private Operation(Operator op, Expression... args) {
        this(op, op.argType, Arrays.asList(args));
    }
    
    private Operation(Operator op, ExprType argType, Expression... args) {
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
        		op != Operator.CAST_NUMERIC &&
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
    			argType != ExprType.NUMERIC) {
    		return argType;
    	}
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
    public Operation getCloneWithArgs(List<Expression> args) {
        return new Operation(op, args);
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
