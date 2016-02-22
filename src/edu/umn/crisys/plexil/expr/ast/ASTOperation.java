package edu.umn.crisys.plexil.expr.ast;

import java.util.Arrays;
import java.util.List;

/**
 * Almost all of Plexil's native operations. 
 * 
 * @author jbiatek
 *
 */
public class ASTOperation extends PlexilExprBase {
    
    public static PlexilType getArgType(String opName) {
        return Operator.valueOf(opName.toUpperCase()).argType;
    }
    
    public static ASTOperation construct(String opName, PlexilExpr...args) {
        return new ASTOperation(Operator.valueOf(opName.toUpperCase()), args);
    }
    
    public static ASTOperation construct(String opName, List<PlexilExpr> args) {
        return new ASTOperation(Operator.valueOf(opName.toUpperCase()), args);
    }
    
    public static ASTOperation and(PlexilExpr...args) {
        return new ASTOperation(Operator.AND, args);
    }
    
    public static ASTOperation and(List<PlexilExpr> args) {
        return new ASTOperation(Operator.AND, args);
    }
    
    public static ASTOperation or(PlexilExpr...args) {
        return new ASTOperation(Operator.OR, args);
    }
    
    public static ASTOperation or(List<PlexilExpr> args) {
        return new ASTOperation(Operator.OR, args);
    }
    
    public static ASTOperation xor(PlexilExpr...args) {
        return new ASTOperation(Operator.XOR, args);
    }

    public static ASTOperation xor(List<PlexilExpr> args) {
        return new ASTOperation(Operator.XOR, args);
    }
    
    public static ASTOperation not(PlexilExpr arg) {
        return new ASTOperation(Operator.NOT, arg);
    }
    
    public static ASTOperation eq(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.EQ, one, two);
    }
    
    public static ASTOperation ne(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.NE, one, two);
    }
    
    public static ASTOperation eq(PlexilExpr one, PlexilExpr two, PlexilType extraInfo) {
        return new ASTOperation(Operator.EQ, extraInfo, one, two);
    }
    
    public static ASTOperation ne(PlexilExpr one, PlexilExpr two, PlexilType extraInfo) {
        return new ASTOperation(Operator.NE, extraInfo, one, two);
    }

    
    public static ASTOperation ge(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.GE, one, two);
    }
    
    public static ASTOperation gt(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.GT, one, two);
    }
    
    public static ASTOperation le(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.LE, one, two);
    }
    
    public static ASTOperation lt(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.LT, one, two);
    }
    
    public static ASTOperation isKnown(PlexilExpr arg) {
        return new ASTOperation(Operator.ISKNOWN, arg);
    }
    
    public static ASTOperation castToBoolean(PlexilExpr arg) {
        return new ASTOperation(Operator.CAST_BOOL, arg);
    }
    
    public static ASTOperation castToInteger(PlexilExpr arg) {
        return new ASTOperation(Operator.CAST_INT, arg);
    }
    
    public static ASTOperation castToReal(PlexilExpr arg) {
        return new ASTOperation(Operator.CAST_REAL, arg);
    }
    
    public static ASTOperation castToString(PlexilExpr arg) {
        return new ASTOperation(Operator.CAST_STRING, arg);
    }
    
    public static ASTOperation abs(PlexilExpr arg) {
        return new ASTOperation(Operator.ABS, arg);
    }
    
    public static ASTOperation add(PlexilExpr...args) {
        return new ASTOperation(Operator.ADD, args);
    }

    public static ASTOperation add(List<PlexilExpr> args) {
        return new ASTOperation(Operator.ADD, args);
    }
    
    public static ASTOperation div(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.DIV, one, two);
    }
    
    public static ASTOperation max(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.MAX, one, two);
    }
    
    public static ASTOperation min(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.MIN, one, two);
    }
    
    public static ASTOperation mod(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.MOD, one, two);
    }
    
    public static ASTOperation mul(PlexilExpr...args) {
        return new ASTOperation(Operator.MUL, args);
    }

    public static ASTOperation mul(List<PlexilExpr> args) {
        return new ASTOperation(Operator.MUL, args);
    }

    public static ASTOperation sqrt(PlexilExpr arg) {
        return new ASTOperation(Operator.SQRT, arg);
    }
    
    public static ASTOperation sub(PlexilExpr one, PlexilExpr two) {
        return new ASTOperation(Operator.SUB, one, two);
    }
    
    public static ASTOperation concat(PlexilExpr...args) {
        return new ASTOperation(Operator.CONCAT, args);
    }
    
    public static ASTOperation concat(List<PlexilExpr> args) {
        return new ASTOperation(Operator.CONCAT, args);
    }
    
    public static ASTOperation getState(PlexilExpr node) {
        return new ASTOperation(Operator.GET_STATE, node);
    }
    public static ASTOperation getOutcome(PlexilExpr node) {
        return new ASTOperation(Operator.GET_OUTCOME, node);
    }
    public static ASTOperation getFailure(PlexilExpr node) {
        return new ASTOperation(Operator.GET_FAILURE, node);
    }
    public static ASTOperation getCommandHandle(PlexilExpr node) {
        return new ASTOperation(Operator.GET_COMMAND_HANDLE, node);
    }
    
    public static ASTOperation arrayIndex(PlexilExpr array, PlexilExpr index) {
    	return new ASTOperation(Operator.ARRAY_INDEX, array, index);
    }

    public static enum Operator {
        AND(-1, "&&", PlexilType.BOOLEAN, PlexilType.BOOLEAN),
        OR(-1, "||", PlexilType.BOOLEAN, PlexilType.BOOLEAN),
        XOR(-1, "XOR", PlexilType.BOOLEAN, PlexilType.BOOLEAN),
        NOT(1, "!(", PlexilType.BOOLEAN, PlexilType.BOOLEAN),
        
        EQ(2, "==", PlexilType.UNKNOWN, PlexilType.BOOLEAN),
        NE(2, "!=", PlexilType.UNKNOWN, PlexilType.BOOLEAN),

        GE(2, ">=", PlexilType.INTEGER, PlexilType.BOOLEAN),
        GT(2, ">", PlexilType.INTEGER, PlexilType.BOOLEAN),
        LE(2, "<=", PlexilType.INTEGER, PlexilType.BOOLEAN),
        LT(2, "<", PlexilType.INTEGER, PlexilType.BOOLEAN),
        
        ISKNOWN(1, "isKnown(", PlexilType.UNKNOWN, PlexilType.BOOLEAN),
        ARRAY_INDEX(2, "[]", PlexilType.ARRAY, PlexilType.UNKNOWN),
                
        // TODO: Do we need casting?
        CAST_BOOL(1, "(PBoolean) (", PlexilType.BOOLEAN, PlexilType.BOOLEAN),
        CAST_INT(1, "(PInteger) (", PlexilType.INTEGER, PlexilType.INTEGER),
        CAST_REAL(1, "(PReal) (", PlexilType.INTEGER, PlexilType.REAL),
        CAST_STRING(1, "(PString) (", PlexilType.STRING, PlexilType.STRING),
        
        ABS(1, "abs(", PlexilType.INTEGER, PlexilType.INTEGER),
        ADD(-1, "+", PlexilType.INTEGER, PlexilType.INTEGER),
        DIV(2, "/", PlexilType.INTEGER, PlexilType.INTEGER),
        MAX(2, "max(", PlexilType.INTEGER, PlexilType.INTEGER),
        MIN(2, "min(", PlexilType.INTEGER, PlexilType.INTEGER),
        MOD(2, "%", PlexilType.INTEGER, PlexilType.INTEGER),
        MUL(-1, "*", PlexilType.INTEGER, PlexilType.INTEGER),
        SQRT(1, "sqrt(", PlexilType.INTEGER, PlexilType.INTEGER),
        SUB(2, "-", PlexilType.INTEGER, PlexilType.INTEGER),
        
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
        
        private String toString(List<PlexilExpr> args) {
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
    private List<PlexilExpr> args;
    private PlexilType argType;
    
    private ASTOperation(Operator op, PlexilType argType, List<PlexilExpr> args) {
    	super(op.returnType);
        this.op = op;
        this.args = args;
        this.argType = argType;
        checkArgs();
    }
    
    private ASTOperation(Operator op, List<PlexilExpr> args) {
    	this(op, op.argType, args);
    }
    
    private ASTOperation(Operator op, PlexilExpr... args) {
        this(op, op.argType, Arrays.asList(args));
    }
    
    private ASTOperation(Operator op, PlexilType argType, PlexilExpr... args) {
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
        	PlexilType.ARRAY.typeCheck(args.get(0).getPlexilType());
        	PlexilType.INTEGER.typeCheck(args.get(1).getPlexilType());
        	return;
        }
        
        // For casts, we're obviously skipping this. Everything else should 
        // check out, though. 
        if (op != Operator.CAST_BOOL && 
        		op != Operator.CAST_STRING) {
	        for (PlexilExpr e : args) {
	            argType.typeCheck(e.getPlexilType());
	        }
        }
    }

    
    public Operator getOperator() {
        return op;
    }
    
    public PlexilType getExpectedArgumentType() {
        return argType;
    }

    public PlexilType getActualArgumentType() {
    	if (argType != PlexilType.UNKNOWN &&
    			argType != PlexilType.INTEGER) {
    		return argType;
    	}
    	if (op == Operator.ARRAY_INDEX) {
    		return args.get(0).getPlexilType().elementType();
    	}
    	
    	// Numbers might actually be reals, and we might be able to do better
    	// than unknown. 
    	PlexilType mostSpecific = argType;
    	for (PlexilExpr e : args) {
    		mostSpecific = argType.getMoreSpecific(e.getPlexilType());
    	}
    	return mostSpecific;
    }
    
    @Override
    public List<PlexilExpr> getPlexilArguments() {
        return args;
    }
    
    @Override
    public String asString() {
        return op.toString(args);
    }

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}
    
}
