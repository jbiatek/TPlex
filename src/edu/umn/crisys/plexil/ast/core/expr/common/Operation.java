package edu.umn.crisys.plexil.ast.core.expr.common;

import java.util.Arrays;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PString;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.StringValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;

/**
 * Almost all of Plexil's native operations. 
 * 
 * @author jbiatek
 *
 */
public class Operation extends CompositeExpr {
    
    public static PlexilType getArgType(String opName) {
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
        AND(-1, "&&", PlexilType.BOOLEAN, PlexilType.BOOLEAN),
        OR(-1, "||", PlexilType.BOOLEAN, PlexilType.BOOLEAN),
        XOR(-1, "XOR", PlexilType.BOOLEAN, PlexilType.BOOLEAN),
        NOT(1, "!(", PlexilType.BOOLEAN, PlexilType.BOOLEAN),
        
        EQ(2, "==", PlexilType.UNKNOWN, PlexilType.BOOLEAN),
        NE(2, "!=", PlexilType.UNKNOWN, PlexilType.BOOLEAN),

        GE(2, ">=", PlexilType.NUMERIC, PlexilType.BOOLEAN),
        GT(2, ">", PlexilType.NUMERIC, PlexilType.BOOLEAN),
        LE(2, "<=", PlexilType.NUMERIC, PlexilType.BOOLEAN),
        LT(2, "<", PlexilType.NUMERIC, PlexilType.BOOLEAN),
        
        ISKNOWN(1, "isKnown(", PlexilType.UNKNOWN, PlexilType.BOOLEAN),
                
        // TODO: Do we need casting?
        CAST_BOOL(1, "(PBoolean) (", PlexilType.UNKNOWN, PlexilType.BOOLEAN),
        CAST_NUMERIC(1, "(PNumeric) (", PlexilType.UNKNOWN, PlexilType.NUMERIC),
        CAST_STRING(1, "(PString) (", PlexilType.UNKNOWN, PlexilType.STRING),
        
        ABS(1, "abs(", PlexilType.NUMERIC, PlexilType.NUMERIC),
        ADD(-1, "+", PlexilType.NUMERIC, PlexilType.NUMERIC),
        DIV(2, "/", PlexilType.NUMERIC, PlexilType.NUMERIC),
        MAX(2, "max(", PlexilType.NUMERIC, PlexilType.NUMERIC),
        MIN(2, "min(", PlexilType.NUMERIC, PlexilType.NUMERIC),
        MOD(2, "%", PlexilType.NUMERIC, PlexilType.NUMERIC),
        MUL(-1, "*", PlexilType.NUMERIC, PlexilType.NUMERIC),
        SQRT(1, "sqrt(", PlexilType.NUMERIC, PlexilType.NUMERIC),
        SUB(2, "-", PlexilType.NUMERIC, PlexilType.NUMERIC),
        
        CONCAT(-1, "+", PlexilType.STRING, PlexilType.STRING),
        
        GET_COMMAND_HANDLE(1, ".command_handle", PlexilType.NODEREF, PlexilType.COMMAND_HANDLE),
        GET_STATE(1, ".state", PlexilType.NODEREF, PlexilType.STATE),
        GET_OUTCOME(1, ".outcome", PlexilType.NODEREF, PlexilType.OUTCOME),
        GET_FAILURE(1, ".failure", PlexilType.NODEREF, PlexilType.FAILURE),
        
        ;
        private int expectedArgs;
        private String symbol;
        private PlexilType argType;
        private PlexilType returnType;
        
        private Operator(int expectedArgs, String symbol, PlexilType argType, PlexilType returnType) {
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
        		}
        		return ret;
        	case CONCAT:
        		PString str = StringValue.get("");
        		for (PValue s : values) {
        			str = str.concat(str(s));
        		}
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
        
        private void checkArgs(List<Expression>args) {
            if (args.size() == 0) {
                throw new RuntimeException("No arguments at all to "+this+"?");
            } else if (expectedArgs != -1 && expectedArgs != args.size()) {
                throw new RuntimeException("Expected "+expectedArgs+" args, not "+args.size());
            }
            
            for (Expression e : args) {
                argType.typeCheck(e.getType());
            }
        }
        
    }
    
    private Operator myOperator;
    private List<Expression> args;
    
    private Operation(Operator op, List<Expression> args) {
        this.myOperator = op;
        this.args = args;
        op.checkArgs(args);
    }
    
    private Operation(Operator op, Expression... args) {
        this(op, Arrays.asList(args));
    }
    
    public Operator getOperator() {
        return myOperator;
    }
    
    @Override
    public PlexilType getType() {
        return myOperator.returnType;
    }
    
    public PlexilType getExpectedArgumentType() {
        return myOperator.argType;
    }

    @Override
    public List<Expression> getArguments() {
        return args;
    }

    @Override
    public Operation getCloneWithArgs(List<Expression> args) {
        return new Operation(myOperator, args);
    }

    @Override
    public String toString() {
        return myOperator.toString(args);
    }
    
    @Override
    public String asString() {
        return toString();
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitOperation(this, param);
    }

}
