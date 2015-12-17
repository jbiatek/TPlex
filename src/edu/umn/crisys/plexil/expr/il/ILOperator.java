package edu.umn.crisys.plexil.expr.il;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.PInteger;
import edu.umn.crisys.plexil.runtime.values.PReal;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public enum ILOperator {

    PBOOL_EQ(2, "==", ExprType.BOOLEAN, ExprType.BOOLEAN) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pbool(a).equalTo(pbool(b)));
		}
    },
    PINT_EQ(2, "==", ExprType.INTEGER, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).equalTo(pint(b)));
		}
    },
    PREAL_EQ(2, "==", ExprType.REAL, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).equalTo(preal(b)));
		}
    },
    PSTRING_EQ(2, "==", ExprType.STRING, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pstring(a).equalTo(pstring(b)));
		}
    },
    PSTATE_EQ(2, "==", ExprType.STATE, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pstate(a).equalTo(pstate(b)));
		}
    },
    POUTCOME_EQ(2, "==", ExprType.OUTCOME, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> poutcome(a).equalTo(poutcome(b)));
		}
    },
    PFAILURE_EQ(2, "==", ExprType.FAILURE, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pfailure(a).equalTo(pfailure(b)));
		}
    },
    PHANDLE_EQ(2, "==", ExprType.COMMAND_HANDLE, ExprType.BOOLEAN) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((a,b) -> phandle(a).equalTo(phandle(b)));
    	}
    },
    
    PAND(2, "&&", ExprType.BOOLEAN, ExprType.BOOLEAN) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pbool(a).and(pbool(b)));
		}
		@Override
		public Optional<PValue> getShortCircuitValue() {
			return Optional.of(BooleanValue.get(false));
		}
    },
    POR(2, "||", ExprType.BOOLEAN, ExprType.BOOLEAN) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pbool(a).or(pbool(b)));
		}
		@Override
		public Optional<PValue> getShortCircuitValue() {
			return Optional.of(BooleanValue.get(true));
		}
    },
    PXOR(2, "XOR", ExprType.BOOLEAN, ExprType.BOOLEAN) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pbool(a).xor(pbool(b)));
		}
    },
    PNOT(1, "!(", ExprType.BOOLEAN, ExprType.BOOLEAN) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> pbool(v).not());
		}
    },
    
    PINT_GE(2, ">=", ExprType.INTEGER, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).ge(pint(b)));
		}
    },
    PINT_GT(2, ">", ExprType.INTEGER, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).gt(pint(b)));
		}
    },
    PINT_LE(2, "<=", ExprType.INTEGER, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).le(pint(b)));
		}
    },
    PINT_LT(2, "<", ExprType.INTEGER, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).lt(pint(b)));
		}
    },

    PREAL_GE(2, ">=", ExprType.REAL, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).ge(preal(b)));
		}
    },
    PREAL_GT(2, ">", ExprType.REAL, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).gt(preal(b)));
		}
    },
    PREAL_LE(2, "<=", ExprType.REAL, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).le(preal(b)));
		}
    },
    PREAL_LT(2, "<", ExprType.REAL, ExprType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).lt(preal(b)));
		}
    },

    PINT_ABS(1, "abs(", ExprType.INTEGER, ExprType.INTEGER) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> pint(v).abs());
		}
    },
    PINT_ADD(2, "+", ExprType.INTEGER, ExprType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).add(pint(b)));
		}
    },
    PINT_DIV(2, "/", ExprType.INTEGER, ExprType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).div(pint(b)));
		}
    },
    PINT_MAX(2, "max(", ExprType.INTEGER, ExprType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).max(pint(b)));
		}
    },
    PINT_MIN(2, "min(", ExprType.INTEGER, ExprType.INTEGER){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).min(pint(b)));
		}
    },
    PINT_MOD(2, "%", ExprType.INTEGER, ExprType.INTEGER){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).mod(pint(b)));
		}
    },
    PINT_MUL(2, "*", ExprType.INTEGER, ExprType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).mul(pint(b)));
		}
    },
    PINT_SUB(2, "-", ExprType.INTEGER, ExprType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).sub(pint(b)));
		}
    },
    
    PREAL_ABS(1, "abs(", ExprType.REAL, ExprType.REAL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> preal(v).abs());
		}
    },
    PREAL_ADD(2, "+", ExprType.REAL, ExprType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).add(preal(b)));
		}
    },
    PREAL_DIV(2, "/", ExprType.REAL, ExprType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).div(preal(b)));
		}
    },
    PREAL_MAX(2, "max(", ExprType.REAL, ExprType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).max(preal(b)));
		}
    },
    PREAL_MIN(2, "min(", ExprType.REAL, ExprType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).min(preal(b)));
		}
    },
    PREAL_MOD(2, "%", ExprType.REAL, ExprType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).mod(preal(b)));
		}
    },
    PREAL_MUL(2, "*", ExprType.REAL, ExprType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).mul(preal(b)));
		}
    },
    PREAL_SQRT(1, "sqrt(", ExprType.REAL, ExprType.REAL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> preal(v).sqrt());
		}
    },
    PREAL_SUB(2, "-", ExprType.REAL, ExprType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).sub(preal(b)));
		}
    },
    
    PCEIL(1, "ceil(", ExprType.REAL, ExprType.INTEGER),
    PFLOOR(1, "floor(", ExprType.REAL, ExprType.INTEGER),
    PROUND(1, "round(", ExprType.REAL, ExprType.INTEGER),
    PTRUNC(1, "trunc(", ExprType.REAL, ExprType.INTEGER),
    PREAL_TO_PINT(1, "real_to_int(", ExprType.REAL, ExprType.INTEGER) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> preal(v).castToInteger());
		}
    },
    
    TO_PREAL(1, "real(", ExprType.INTEGER, ExprType.REAL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> pint(v).castToReal());
		}
    },
    
    PSTR_CONCAT(2, "+", ExprType.STRING, ExprType.STRING){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pstring(a).concat(pstring(b)));
		}
    },    
    
    PBOOL_INDEX(2, "[]", asList(ExprType.BOOLEAN_ARRAY, 
    		ExprType.INTEGER), ExprType.BOOLEAN) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((arr, i) ->
    					pbool(array(arr).get(pint(i)))
    				);
    	}
    },
    PINT_INDEX(2, "[]", asList(ExprType.INTEGER_ARRAY, 
    		ExprType.INTEGER), ExprType.INTEGER) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((arr, i) ->
    					pint(array(arr).get(pint(i)))
    				);
    	}
    },
    PREAL_INDEX(2, "[]", asList(ExprType.REAL_ARRAY, 
    		ExprType.INTEGER), ExprType.REAL) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((arr, i) ->
    					preal(array(arr).get(pint(i)))
    				);
    	}
    },
    PSTRING_INDEX(2, "[]", asList(ExprType.STRING_ARRAY, 
    		ExprType.INTEGER), ExprType.STRING) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((arr, i) ->
    					pstring(array(arr).get(pint(i)))
    				);
    	}
    },
    
    ISKNOWN_OPERATOR(1, "isKnown(", ExprType.UNKNOWN, ExprType.NATIVE_BOOL){
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of(
    					(v) -> NativeBool.wrap(v.isKnown())
    				);
    	}
    },
    CAST_PBOOL(1, "(PBoolean) (", ExprType.UNKNOWN, ExprType.BOOLEAN) {
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of((v) -> pbool(v));
    	}
    },
    CAST_PINT(1, "(PInteger) (", ExprType.UNKNOWN, ExprType.INTEGER){
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of((v) -> pint(v));
    	}
    },
    CAST_PREAL(1, "(PReal) (", ExprType.UNKNOWN, ExprType.REAL){
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of((v) -> preal(v));
    	}
    },
    CAST_PSTRING(1, "(PString) (", ExprType.UNKNOWN, ExprType.STRING){
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of((v) -> pstring(v));
    	}
    },
    
	AND(2, "and", ExprType.NATIVE_BOOL, ExprType.NATIVE_BOOL) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of(
						(a, b) -> NativeBool.wrap(bool(a) && bool(b))
					);
		}
		@Override
		public Optional<PValue> getShortCircuitValue() {
			return Optional.of(NativeBool.FALSE);
		}
    },
	OR(2, "or", ExprType.NATIVE_BOOL, ExprType.NATIVE_BOOL) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of(
						(a, b) -> NativeBool.wrap(bool(a) || bool(b))
					);
		}
		@Override
		public Optional<PValue> getShortCircuitValue() {
			return Optional.of(NativeBool.TRUE);
		}
    },
	NOT(1, "not", ExprType.NATIVE_BOOL, ExprType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap( ! bool(v)));
		}
    },
	EQ(2, "eq", ExprType.NATIVE_BOOL, ExprType.NATIVE_BOOL) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((a,b) -> NativeBool.wrap(
    				bool(a) == bool(b)));
    	}
    },
	DIRECT_COMPARE(2, "eq", ExprType.UNKNOWN, ExprType.NATIVE_BOOL) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		// This operator directly compares 2 PValues with a native operation
    		// like "=" or ".equals()". 
    		return Optional.of((a,b) -> NativeBool.wrap(a.equals(b)));
    	}
    },
	
	IS_TRUE(1, ".isTrue()", ExprType.BOOLEAN, ExprType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isTrue()));
		}
    },
	IS_FALSE(1, ".isFalse()", ExprType.BOOLEAN, ExprType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isFalse()));
		}
    },
	IS_UNKNOWN(1, ".isUnknown()", ExprType.BOOLEAN, ExprType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isUnknown()));
		}
    },
	IS_NOT_TRUE(1, ".isNotTrue()", ExprType.BOOLEAN, ExprType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isNotTrue()));
		}
    },
	IS_NOT_FALSE(1, ".isNotFalse()", ExprType.BOOLEAN, ExprType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isNotFalse()));
		}
    },
	IS_KNOWN(1, ".isKnown()", ExprType.BOOLEAN, ExprType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isKnown()));
		}
    },
	WRAP_BOOLEAN(1, "pboolean(", ExprType.NATIVE_BOOL, ExprType.BOOLEAN) {
    	@Override
    	public Optional<Function<PValue,PValue>> getUnaryEval() {
    		return Optional.of(v -> BooleanValue.get(bool(v)));
    	}
    }
	
	;
	
	private int argSize;
	private String symbol;
	private List<ExprType> argTypes;
	private ExprType returnType;
    
	private ILOperator(int args, String symbol, 
    		ExprType argType, 
    		ExprType returnType) {
		this(args, symbol, asList(argType), returnType);
    }
    private ILOperator(int args, String symbol, 
    		List<ExprType> argTypes, 
    		ExprType returnType) {
    	this.argSize = args;
    	this.symbol = symbol;
    	this.argTypes = argTypes;
    	this.returnType = returnType;
    }
    
    public ILOperation expr(Expression...args) {
    	return expr(Arrays.asList(args));
    }
    
    public ILOperation expr(List<Expression> args) {
    	if (argSize == 2 && args.size() > 2) {
    		// We know what they meant, reduce it for them. 
    		return (ILOperation) args.stream().reduce(
    				(a,b) -> this.expr(a,b)).get();
    	}
    	
    	return new ILOperation(this, args);
    }
    
    public ExprType getReturnType() {
    	return returnType;
    }
    
	public boolean isAcceptableArgNumber(int argNumber) {
		if (argNumber < 1) return false;
		else return argNumber == argSize;
	}
	
	public boolean isTypeSafe(List<Expression> args) {
		if (argTypes.size() == 1) {
			// Same type for all of them. 
			// They should all match the type that we have.
			return args.stream().allMatch(e -> checkArgType(argTypes.get(0), e));
		} else {
			 if (argTypes.size() != args.size()) {
				 throw new RuntimeException("Type number and arg number don't match for "+this+"!");
			 }
			 boolean allValid = true;
			 for (int i=0; i < argTypes.size(); i++) {
				 if ( ! checkArgType(argTypes.get(i), args.get(i))) {
					 allValid = false;
					 break;
				 }
			 }
			 return allValid;
		}
	}
	
	private boolean checkArgType(ExprType type, Expression arg) {
		if (arg instanceof UnknownValue) {
			// This is fine as long as it's one of these types
			return type == ExprType.BOOLEAN 
					|| type == ExprType.INTEGER
					|| type == ExprType.REAL
					|| type == ExprType.STRING;
		}
		
		if (type == ExprType.UNKNOWN) {
			// We use UNKNOWN for any type of PValue here
			return ! arg.getType().equals(ExprType.NATIVE_BOOL);
		} else {
			return arg.getType().equals(type);
		}
	}
	
	public String getStringPrefix() {
		if (symbol.endsWith("(")) return symbol;
		else if (symbol.equals("[]")) return "";
		else return "(";
	}
	
	public String getStringDeliminator() {
		if (symbol.endsWith("(")) return ", ";
		else if (symbol.equals("[]")) return "[";
		else return " "+symbol+" ";
	}
	
	public String getStringSuffix() {
		if (symbol.startsWith(".")) return ")"+symbol;
		else if (symbol.endsWith("(")) return ")";
		else if (symbol.equals("[]")) return "]";
		else return ")";
	}
	
	public boolean isAssignable() {
		// The only operator that's assignable is an array index.
		return this == PBOOL_INDEX 
				|| this == PINT_INDEX
				|| this == PREAL_INDEX
				|| this == PSTRING_INDEX;
	}
	
	public Optional<PValue> eval(List<Expression> args) {
		if (args.isEmpty()) {
			throw new RuntimeException("No args passed to "+this);
		}
		
		List<Optional<PValue>> eval = args.stream()
				.map(Expression::eval)
				.collect(Collectors.toList());
		
		if (getShortCircuitValue().isPresent()) {
			PValue shortCircuiter = getShortCircuitValue().get();
			boolean hasShortCircuit = eval.stream()
					.anyMatch(v -> v.isPresent() && shortCircuiter.equals(v.get()));
			if (hasShortCircuit) {
				return Optional.of(shortCircuiter);
			}
		}
		
		// It isn't a short-circuit situation. An unpresent value means
		// that we can't evaluate this set of arguments. 
		if (eval.stream().anyMatch(v -> ! v.isPresent())) {
			return Optional.empty();
		}
		
		// They're all present. Let's map to that then. 
		List<PValue> concreteEval = eval.stream().map(o -> o.get())
				.collect(Collectors.toList());
		
		// In order from most specific to least specific
		if (concreteEval.size() == 1 && getUnaryEval().isPresent()) {
			PValue arg = concreteEval.get(0);
			return Optional.of(getUnaryEval().get().apply(arg));
		} else if (concreteEval.size() == 2 && getBinaryEval().isPresent()) {
			PValue arg0 = concreteEval.get(0);
			PValue arg1 = concreteEval.get(1);
			return Optional.of(getBinaryEval().get().apply(arg0, arg1));
		} else if (getReducerEval().isPresent()) {
			return concreteEval.stream()
					.reduce(getReducerEval().get());
		} else {
			return Optional.of(getEval().apply(concreteEval));
		}
	}
	
	public Optional<Function<PValue, PValue>> getUnaryEval() {
		return Optional.empty();
	}

	public Optional<BinaryOperator<PValue>> getBinaryEval() {
		return Optional.empty();
	}

	public Optional<BinaryOperator<PValue>> getReducerEval() {
		return Optional.empty();
	}
	
	public Optional<PValue> getShortCircuitValue() {
		return Optional.empty();
	}
		
	public Function<List<PValue>, PValue> getEval() {
		throw new RuntimeException("No eval method was specified for "+this);
	}
	
	// Static casting methods
	
	private static boolean bool(PValue v) {
		return ((NativeBool) v).getValue();
	}
	
	private static PBoolean pbool(PValue e) {
		return (PBoolean) e;
	}
	
	private static PInteger pint(PValue e) {
		return (PInteger) e;
	}
	
	private static PReal preal(PValue v) {
		return (PReal) v;
	}
	
	private static PString pstring(PValue v) {
		return (PString) v;
	}
	
	private static NodeState pstate(PValue v) {
		return (NodeState) v;
	}
	
	private static NodeOutcome poutcome(PValue v) {
		return (NodeOutcome) v;
	}
	
	private static NodeFailureType pfailure(PValue v) {
		return (NodeFailureType) v;
	}
	
	private static CommandHandleState phandle(PValue v) {
		return (CommandHandleState) v;
	}
	
	private static PValueList<?> array(PValue v) {
		return (PValueList<?>) v;
	}
	
}
