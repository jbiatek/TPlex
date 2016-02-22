package edu.umn.crisys.plexil.expr.il;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

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

public enum ILOperator {

    PBOOL_EQ(2, "==", ILType.BOOLEAN, ILType.BOOLEAN) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pbool(a).equalTo(pbool(b)));
		}
    },
    PINT_EQ(2, "==", ILType.INTEGER, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).equalTo(pint(b)));
		}
    },
    PREAL_EQ(2, "==", ILType.REAL, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).equalTo(preal(b)));
		}
    },
    PSTRING_EQ(2, "==", ILType.STRING, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pstring(a).equalTo(pstring(b)));
		}
    },
    PSTATE_EQ(2, "==", ILType.STATE, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pstate(a).equalTo(pstate(b)));
		}
    },
    POUTCOME_EQ(2, "==", ILType.OUTCOME, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> poutcome(a).equalTo(poutcome(b)));
		}
    },
    PFAILURE_EQ(2, "==", ILType.FAILURE, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pfailure(a).equalTo(pfailure(b)));
		}
    },
    PHANDLE_EQ(2, "==", ILType.COMMAND_HANDLE, ILType.BOOLEAN) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((a,b) -> phandle(a).equalTo(phandle(b)));
    	}
    },
    
    PAND(2, "&&", ILType.BOOLEAN, ILType.BOOLEAN) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pbool(a).and(pbool(b)));
		}
		@Override
		public Optional<PValue> getShortCircuitValue() {
			return Optional.of(BooleanValue.get(false));
		}
    },
    POR(2, "||", ILType.BOOLEAN, ILType.BOOLEAN) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pbool(a).or(pbool(b)));
		}
		@Override
		public Optional<PValue> getShortCircuitValue() {
			return Optional.of(BooleanValue.get(true));
		}
    },
    PXOR(2, "XOR", ILType.BOOLEAN, ILType.BOOLEAN) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pbool(a).xor(pbool(b)));
		}
    },
    PNOT(1, "!(", ILType.BOOLEAN, ILType.BOOLEAN) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> pbool(v).not());
		}
    },
    
    PINT_GE(2, ">=", ILType.INTEGER, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).ge(pint(b)));
		}
    },
    PINT_GT(2, ">", ILType.INTEGER, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).gt(pint(b)));
		}
    },
    PINT_LE(2, "<=", ILType.INTEGER, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).le(pint(b)));
		}
    },
    PINT_LT(2, "<", ILType.INTEGER, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).lt(pint(b)));
		}
    },

    PREAL_GE(2, ">=", ILType.REAL, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).ge(preal(b)));
		}
    },
    PREAL_GT(2, ">", ILType.REAL, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).gt(preal(b)));
		}
    },
    PREAL_LE(2, "<=", ILType.REAL, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).le(preal(b)));
		}
    },
    PREAL_LT(2, "<", ILType.REAL, ILType.BOOLEAN){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).lt(preal(b)));
		}
    },

    PINT_ABS(1, "abs(", ILType.INTEGER, ILType.INTEGER) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> pint(v).abs());
		}
    },
    PINT_ADD(2, "+", ILType.INTEGER, ILType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).add(pint(b)));
		}
    },
    PINT_DIV(2, "/", ILType.INTEGER, ILType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).div(pint(b)));
		}
    },
    PINT_MAX(2, "max(", ILType.INTEGER, ILType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).max(pint(b)));
		}
    },
    PINT_MIN(2, "min(", ILType.INTEGER, ILType.INTEGER){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).min(pint(b)));
		}
    },
    PINT_MOD(2, "%", ILType.INTEGER, ILType.INTEGER){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).mod(pint(b)));
		}
    },
    PINT_MUL(2, "*", ILType.INTEGER, ILType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).mul(pint(b)));
		}
    },
    PINT_SUB(2, "-", ILType.INTEGER, ILType.INTEGER) {
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pint(a).sub(pint(b)));
		}
    },
    
    PREAL_ABS(1, "abs(", ILType.REAL, ILType.REAL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> preal(v).abs());
		}
    },
    PREAL_ADD(2, "+", ILType.REAL, ILType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).add(preal(b)));
		}
    },
    PREAL_DIV(2, "/", ILType.REAL, ILType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).div(preal(b)));
		}
    },
    PREAL_MAX(2, "max(", ILType.REAL, ILType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).max(preal(b)));
		}
    },
    PREAL_MIN(2, "min(", ILType.REAL, ILType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).min(preal(b)));
		}
    },
    PREAL_MOD(2, "%", ILType.REAL, ILType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).mod(preal(b)));
		}
    },
    PREAL_MUL(2, "*", ILType.REAL, ILType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).mul(preal(b)));
		}
    },
    PREAL_SQRT(1, "sqrt(", ILType.REAL, ILType.REAL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> preal(v).sqrt());
		}
    },
    PREAL_SUB(2, "-", ILType.REAL, ILType.REAL){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> preal(a).sub(preal(b)));
		}
    },
    
    PCEIL(1, "ceil(", ILType.REAL, ILType.INTEGER),
    PFLOOR(1, "floor(", ILType.REAL, ILType.INTEGER),
    PROUND(1, "round(", ILType.REAL, ILType.INTEGER),
    PTRUNC(1, "trunc(", ILType.REAL, ILType.INTEGER),
    PREAL_TO_PINT(1, "real_to_int(", ILType.REAL, ILType.INTEGER) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> preal(v).castToInteger());
		}
    },
    
    TO_PREAL(1, "real(", ILType.INTEGER, ILType.REAL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> pint(v).castToReal());
		}
    },
    
    PSTR_CONCAT(2, "+", ILType.STRING, ILType.STRING){
		@Override
		public Optional<BinaryOperator<PValue>> getBinaryEval() {
			return Optional.of((a,b) -> pstring(a).concat(pstring(b)));
		}
    },    
    
    PBOOL_INDEX(2, "[]", asList(ILType.BOOLEAN_ARRAY, 
    		ILType.INTEGER), ILType.BOOLEAN) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((arr, i) ->
    					pbool(array(arr).get(pint(i)))
    				);
    	}
    },
    PINT_INDEX(2, "[]", asList(ILType.INTEGER_ARRAY, 
    		ILType.INTEGER), ILType.INTEGER) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((arr, i) ->
    					pint(array(arr).get(pint(i)))
    				);
    	}
    },
    PREAL_INDEX(2, "[]", asList(ILType.REAL_ARRAY, 
    		ILType.INTEGER), ILType.REAL) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((arr, i) ->
    					preal(array(arr).get(pint(i)))
    				);
    	}
    },
    PSTRING_INDEX(2, "[]", asList(ILType.STRING_ARRAY, 
    		ILType.INTEGER), ILType.STRING) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((arr, i) ->
    					pstring(array(arr).get(pint(i)))
    				);
    	}
    },
    
    ISKNOWN_OPERATOR(1, "isKnown(", ILType.UNKNOWN, ILType.NATIVE_BOOL){
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of(
    					(v) -> NativeBool.wrap(v.isKnown())
    				);
    	}
    },
    CAST_PBOOL(1, "(PBoolean) (", ILType.UNKNOWN, ILType.BOOLEAN) {
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of((v) -> pbool(v));
    	}
    },
    CAST_PINT(1, "(PInteger) (", ILType.UNKNOWN, ILType.INTEGER){
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of((v) -> pint(v));
    	}
    },
    CAST_PREAL(1, "(PReal) (", ILType.UNKNOWN, ILType.REAL){
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of((v) -> preal(v));
    	}
    },
    CAST_PSTRING(1, "(PString) (", ILType.UNKNOWN, ILType.STRING){
    	@Override
    	public Optional<Function<PValue, PValue>> getUnaryEval() {
    		return Optional.of((v) -> pstring(v));
    	}
    },
    
	AND(2, "and", ILType.NATIVE_BOOL, ILType.NATIVE_BOOL) {
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
	OR(2, "or", ILType.NATIVE_BOOL, ILType.NATIVE_BOOL) {
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
	NOT(1, "not", ILType.NATIVE_BOOL, ILType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap( ! bool(v)));
		}
    },
	EQ(2, "eq", ILType.NATIVE_BOOL, ILType.NATIVE_BOOL) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		return Optional.of((a,b) -> NativeBool.wrap(
    				bool(a) == bool(b)));
    	}
    },
	DIRECT_COMPARE(2, "eq", ILType.UNKNOWN, ILType.NATIVE_BOOL) {
    	@Override
    	public Optional<BinaryOperator<PValue>> getBinaryEval() {
    		// This operator directly compares 2 PValues with a native operation
    		// like "=" or ".equals()". 
    		return Optional.of((a,b) -> NativeBool.wrap(a.equals(b)));
    	}
    },
	
	IS_TRUE(1, ".isTrue()", ILType.BOOLEAN, ILType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isTrue()));
		}
    },
	IS_FALSE(1, ".isFalse()", ILType.BOOLEAN, ILType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isFalse()));
		}
    },
	IS_UNKNOWN(1, ".isUnknown()", ILType.BOOLEAN, ILType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isUnknown()));
		}
    },
	IS_NOT_TRUE(1, ".isNotTrue()", ILType.BOOLEAN, ILType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isNotTrue()));
		}
    },
	IS_NOT_FALSE(1, ".isNotFalse()", ILType.BOOLEAN, ILType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isNotFalse()));
		}
    },
	IS_KNOWN(1, ".isKnown()", ILType.BOOLEAN, ILType.NATIVE_BOOL) {
		@Override
		public Optional<Function<PValue, PValue>> getUnaryEval() {
			return Optional.of(v -> NativeBool.wrap(pbool(v).isKnown()));
		}
    },
	WRAP_BOOLEAN(1, "pboolean(", ILType.NATIVE_BOOL, ILType.BOOLEAN) {
    	@Override
    	public Optional<Function<PValue,PValue>> getUnaryEval() {
    		return Optional.of(v -> BooleanValue.get(bool(v)));
    	}
    }
	
	;
	
	private int argSize;
	private String symbol;
	private List<ILType> argTypes;
	private ILType returnType;
    
	private static <T> List<T> repeatList(T item, int times) {
		List<T> list = new ArrayList<>();
		for (int i = 0; i < times; i++) {
			list.add(item);
		}
		return list;
	}
	
	/**
	 * Create an IL Operator where the arguments are all the same type.
	 * 
	 * @param args Number of args expected.
	 * @param symbol The string symbol to use.
	 * @param argType The type of all args to this operator
	 * @param returnType The type returned by this operator. 
	 */
	private ILOperator(int args, String symbol, 
    		ILType argType, 
    		ILType returnType) {
		this(args, symbol, repeatList(argType, args), returnType);
    }
	
	/**
	 * Create an IL operator as specified. 
	 * 
	 * @param args Number of args expected.
	 * @param symbol The string symbol to use.
	 * @param argTypes The type of all args to this operator
	 * @param returnType The type returned by this operator. 
	 */
    private ILOperator(int args, String symbol, 
    		List<ILType> argTypes, 
    		ILType returnType) {
    	this.argSize = args;
    	this.symbol = symbol;
    	this.argTypes = argTypes;
    	this.returnType = returnType;
    	
    	if (argSize < 1) { 
    		throw new RuntimeException("Args for "+this+" is "+argSize);
    	}
    	if (args != argTypes.size()) {
    		throw new RuntimeException(this+" expects "+args+" args, but "
    				+argTypes.size()+" types were provided");
    	}
    	
    }
    
    public static ILOperation arrayIndex(ILExpr array, ILExpr index) {
    	switch (array.getType()) {
		case BOOLEAN_ARRAY:
			return ILOperator.PBOOL_INDEX.expr(array, index);
		case INTEGER_ARRAY:
			return ILOperator.PINT_INDEX.expr(array, index);
		case REAL_ARRAY:
			return ILOperator.PREAL_INDEX.expr(array, index);
		case STRING_ARRAY:
			return ILOperator.PSTRING_INDEX.expr(array, index);
		default:
			throw new RuntimeException("Cannot have an array of type "+array.getType());
		}
    }
    
    public ILOperation expr(ILExpr...args) {
    	return expr(Arrays.asList(args));
    }
    
    public ILOperation expr(List<ILExpr> args) {
    	if (argSize == 2 && args.size() > 2) {
    		// We know what they meant, reduce it for them. 
    		return (ILOperation) args.stream().reduce(
    				(a,b) -> this.expr(a,b)).get();
    	}
    	
    	return new ILOperation(this, args);
    }
    
    public ILType getReturnType() {
    	return returnType;
    }
    
    /**
     * Get a list that represents expected argument types. For the most part,
     * these should match exactly with their corresponding argument. The one
     * exception is the CAST operators -- they have an argument type of 
     * UNKNOWN, but their argument should be any PLEXIL type.  
     * 
     * 
     * @return
     */
    public List<ILType> getArgumentTypeList() {
    	return argTypes;
    }
    
	public boolean isAcceptableArgNumber(int argNumber) {
		return argNumber == argSize;
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
	
	public Optional<PValue> eval(List<ILExpr> args) {
		if (args.isEmpty()) {
			throw new RuntimeException("No args passed to "+this);
		}
		
		List<Optional<PValue>> eval = args.stream()
				.map(ILExpr::eval)
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
