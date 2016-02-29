package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jkind.lustre.ArrayAccessExpr;
import jkind.lustre.ArrayExpr;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Expr;
import jkind.lustre.IntExpr;
import jkind.lustre.LustreUtil;
import jkind.lustre.NodeCallExpr;
import jkind.lustre.RealExpr;
import jkind.lustre.TupleExpr;
import jkind.lustre.visitors.PrettyPrintVisitor;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILOperation;
import edu.umn.crisys.plexil.expr.il.ILType;
import edu.umn.crisys.plexil.expr.il.LookupExpr;
import edu.umn.crisys.plexil.expr.il.NamedCondition;
import edu.umn.crisys.plexil.expr.il.RootAncestorExpr;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class ILExprToLustre extends ILExprVisitor<ILType, jkind.lustre.Expr>{
	
	public static String exprToString(Expr lustre) {
		PrettyPrintVisitor pp = new PrettyPrintVisitor();
		lustre.accept(pp);
		return pp.toString();
	}
	
	private ReverseTranslationMap mapper;

	public ILExprToLustre() { }
	public ILExprToLustre(ReverseTranslationMap mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public Expr visit(NamedCondition named, ILType expected) {
		// This means that the contents of this expression are de-inlined.
		// We can just return the variable id. 
		return id(LustreNamingConventions.getNamedConditionId(named));
	}
	
	@Override
	public Expr visit(LookupExpr lookup, ILType expectedType) {
		if (LustreNamingConventions.hasValueAndKnownSplit(expectedType)
				|| LustreNamingConventions.hasValueAndKnownSplit(lookup.getType())) {
			// This value has a value part and a known part.
			return tuple(
					id(LustreNamingConventions.getLookupIdValuePart(
							lookup.getLookupNameAsString())),
					id(LustreNamingConventions.getLookupIdKnownPart(
							lookup.getLookupNameAsString()))
					);
		} else {
			return id(LustreNamingConventions.getLookupId(
					lookup.getLookupNameAsString()));
		}
	}

	private static Expr toPBoolean(Expr e) {
		return new NodeCallExpr("to_pboolean", e);

	}
	
	private static Expr tuple(Expr one, Expr two) {
		return new TupleExpr(Arrays.asList(one, two));
	}
	
	/**
	 * Numeric expressions always return a tuple containing the value itself
	 * and an unknown flag. When you need to do an operation on one, though,
	 * it won't work since tuples can't be added, divided, etc., and since
	 * nodes can't take tuples either, we're a little stuck. What we have to
	 * do is at translation time, when we need to operate on a number, split it
	 * into a numeric component and a known-ness component. Luckily, none of
	 * the numeric operators have any complex behavior, so they can be 
	 * entirely separated. 
	 * 
	 * @param e
	 * @return
	 */
	public static Expr getValueComponent(Expr e) {
		if (e instanceof TupleExpr) {
			return ((TupleExpr) e).elements.get(0);
		} else {
			throw new RuntimeException("Not a tuple: "+e);
		}
	}
	
	/**
	 * Numeric expressions always return a tuple containing the value itself
	 * and an unknown flag. When you need to do an operation on one, though,
	 * it won't work since tuples can't be added, divided, etc., and since
	 * nodes can't take tuples either, we're a little stuck. What we have to
	 * do is at translation time, when we need to operate on a number, split it
	 * into a numeric component and a known-ness component. Luckily, none of
	 * the numeric operators have any complex behavior, so they can be 
	 * entirely separated. 
	 * 
	 * @param e
	 * @return
	 */

	public static Expr getKnownComponent(Expr e) {
		if (e instanceof TupleExpr) {
			return ((TupleExpr) e).elements.get(1);
		} else {
			throw new RuntimeException("Not a tuple: "+e);
		}
	}
	
	@Override
	public Expr visit(NativeBool b, ILType param) {
		return b.getValue() ? LustreUtil.TRUE : LustreUtil.FALSE;
	}
	
	
	@Override
	public Expr visit(ILOperation op, ILType expectedType) {
		switch (op.getOperator()) {
		// ---------------- Native boolean operators
		case AND:
			return binary(op.getArguments(), BinaryOp.AND, ILType.NATIVE_BOOL);
		case OR:
			return binary(op.getArguments(), BinaryOp.OR, ILType.NATIVE_BOOL);
		case NOT:
			return LustreUtil.not(op.getUnaryArg().accept(this, ILType.NATIVE_BOOL));
		case IS_TRUE:
			return new BinaryExpr(op.getUnaryArg().accept(this, ILType.BOOLEAN), 
					BinaryOp.EQUAL, LustreNamingConventions.P_TRUE);
		case IS_FALSE:
			return new BinaryExpr(op.getUnaryArg().accept(this, ILType.BOOLEAN), 
					BinaryOp.EQUAL, LustreNamingConventions.P_FALSE);
		case IS_NOT_TRUE:
			return new BinaryExpr(op.getUnaryArg().accept(this, ILType.BOOLEAN), 
					BinaryOp.NOTEQUAL, LustreNamingConventions.P_TRUE);
		case IS_NOT_FALSE:
			return new BinaryExpr(op.getUnaryArg().accept(this, ILType.BOOLEAN), 
					BinaryOp.NOTEQUAL, LustreNamingConventions.P_FALSE);
		case IS_UNKNOWN:
			return new BinaryExpr(op.getUnaryArg().accept(this, ILType.BOOLEAN), 
					BinaryOp.EQUAL, LustreNamingConventions.P_UNKNOWN);
		case IS_KNOWN:
			return new BinaryExpr(op.getUnaryArg().accept(this, ILType.BOOLEAN), 
					BinaryOp.NOTEQUAL, LustreNamingConventions.P_UNKNOWN);
		case DIRECT_COMPARE:
			return binary(op.getArguments(), BinaryOp.EQUAL, ILType.UNKNOWN);
		case WRAP_BOOLEAN:
			return toPBoolean(op.getUnaryArg().accept(this, ILType.BOOLEAN));
		case PBOOL_INDEX:
			return arrayIndexer(op, ILType.BOOLEAN_ARRAY);
		case PINT_INDEX:
			return arrayIndexer(op, ILType.INTEGER_ARRAY);
		case PREAL_INDEX:
			return arrayIndexer(op, ILType.REAL_ARRAY);
		case PSTRING_INDEX:
			return arrayIndexer(op, ILType.STRING_ARRAY);
			
		// ---------------- Plexil boolean operators
		case PAND:
			return binary(op.getArguments(), LustreNamingConventions.AND_OPERATOR, ILType.BOOLEAN);
		case PNOT:
			return unary(op.getArguments(), LustreNamingConventions.NOT_OPERATOR, ILType.BOOLEAN);
		case POR:
			return binary(op.getArguments(), LustreNamingConventions.OR_OPERATOR, ILType.BOOLEAN);
		case PXOR:
			return binary(op.getArguments(), LustreNamingConventions.XOR_OPERATOR, ILType.BOOLEAN);
		case PBOOL_EQ:
			return binary(op.getArguments(), LustreNamingConventions.EQ_BOOL_OPERATOR, ILType.BOOLEAN);
		case PSTRING_EQ: 
		case PSTATE_EQ:
		case POUTCOME_EQ:
		case PFAILURE_EQ:
		case PHANDLE_EQ:
			// TODO: This is wrong, it doesn't handle unknowns correctly at all
				return toPBoolean(binary(op.getArguments(), BinaryOp.EQUAL, 
						op.getBinaryFirst().getType()));
		case ISKNOWN_OPERATOR:
			// note: this operator returns a *native* bool
			switch (op.getUnaryArg().getType()) {
			case BOOLEAN:
				return new BinaryExpr(
						op.getArguments().get(0).accept(this, ILType.BOOLEAN), 
						BinaryOp.NOTEQUAL, 
						LustreNamingConventions.P_UNKNOWN);
			case STRING:
			case OUTCOME:
			case FAILURE:
			case COMMAND_HANDLE:
				// All of these are simple enums
				return new BinaryExpr(
						op.getArguments().get(0).accept(this, op.getUnaryArg().getType()),
						BinaryOp.NOTEQUAL,
						op.getUnaryArg().getType().getUnknown()
							.accept(this, op.getUnaryArg().getType()));
			case ARRAY:
			case BOOLEAN_ARRAY:
			case INTEGER_ARRAY:
			case REAL_ARRAY:
			case STRING_ARRAY:
			case STATE:
			case INTEGER: //TODO: these shouldn't be just "true"
			case REAL:
				// All of these are always known
				return LustreUtil.TRUE;
			default:
				System.err.println("Missing case in isKnown translation: "+op.getUnaryArg().getType());
			}

		// ---------------- Numeric comparators
		// Each one has a special node operator that takes all 4 components
		// of the 2 numbers being compared. We can deal with them all the same
		// way. 
		case PINT_EQ:
		case PREAL_EQ:
		case PINT_GE:
		case PREAL_GE:
		case PINT_GT:
		case PREAL_GT:
		case PINT_LE:
		case PREAL_LE:
		case PINT_LT:
		case PREAL_LT:
			{
				String nodeName = op.getOperator().toString().toLowerCase();
				ILType type = ILType.INTEGER;
				if (nodeName.startsWith("preal")) {
					type = ILType.REAL;
				}
				Expr first = op.getBinaryFirst().accept(this, type);
				Expr second = op.getBinarySecond().accept(this, type);
				return new NodeCallExpr(nodeName, 
						getValueComponent(first),
						getKnownComponent(first),
						getValueComponent(second),
						getKnownComponent(second));
			}
			

			
		// ---------------- Numeric operators
		// These need special tuple handling too, but the unary() and binary()
		// methods all handle that for us. 
		case PINT_ABS:
		case PREAL_ABS:
			return unary(op.getArguments(), "abs", op.getUnaryArg().getType());
		case PINT_ADD:
		case PREAL_ADD:
			return binary(op.getArguments(), BinaryOp.PLUS, op.getBinaryFirst().getType());
		case PINT_DIV:
		case PREAL_DIV:
			return binary(op.getArguments(), BinaryOp.DIVIDE, op.getBinaryFirst().getType());
		case PINT_MAX:
		case PREAL_MAX:
			return binary(op.getArguments(), "max", 
					op.getBinaryFirst().getType());
		case PINT_MIN:
		case PREAL_MIN:
			return binary(op.getArguments(), "min", 
					op.getBinaryFirst().getType());
		case PINT_MOD:
		case PREAL_MOD:
			return binary(op.getArguments(), BinaryOp.MODULUS, 
					op.getBinaryFirst().getType());
		case PINT_MUL:
		case PREAL_MUL:
			return binary(op.getArguments(), BinaryOp.MULTIPLY, 
					op.getBinaryFirst().getType());
		case PREAL_SQRT:
			return unary(op.getArguments(), "sqrt", ILType.REAL);
		case PINT_SUB:
		case PREAL_SUB:
			return binary(op.getArguments(), BinaryOp.MINUS, 
					op.getBinaryFirst().getType());
		case TO_PREAL:
			Expr intExpr = op.getUnaryArg().accept(this, ILType.INTEGER);
			return tuple(castReal(getValueComponent(intExpr)), 
					getKnownComponent(intExpr));
		// ---------------- String operators (not supported)
		case PSTR_CONCAT:
			throw new RuntimeException("String concatenation not supported when translating to Lustre");
		default:
			throw new RuntimeException("Missing operator: "+op.getOperator());
		}
	}
	
	private Expr arrayIndexer(ILOperation op, ILType arrayType) {
		Expr array = op.getBinaryFirst().accept(this, arrayType);
		Expr indexTuple = op.getBinarySecond().accept(this, ILType.INTEGER);

		// The index is split, we need both parts of it separately.
		Expr indexValue = getValueComponent(indexTuple);
		Expr indexKnown = getKnownComponent(indexTuple);
		Expr unknownValue = UnknownValue.get().accept(this, op.getType());
		
		// Is the array also split? 
		if (LustreNamingConventions.hasValueAndKnownSplit(op.getType())) {
			// Yes. 
			Expr arrayValue = getValueComponent(array);
			Expr arrayKnown = getKnownComponent(array);

			// If the index is unknown... return unknown I guess?
			// Keep in mind that whoever is getting this might be expecting
			// a tuple. 
			// TODO: Figure out the correct semantics for this.
			return tuple(new ArrayAccessExpr(arrayValue, indexValue),
					// If the index was unknown, the above value is garbage
				      ite(indexKnown, 
				    		  new ArrayAccessExpr(arrayKnown, indexValue),
				    		  LustreUtil.FALSE));
		} else {
			// No, just the index is split.
			//TODO: Same problem here as above. 
			return ite(indexKnown,
					new ArrayAccessExpr(array, indexValue),
					unknownValue);
		}
		
	}
	
	private Expr unary(List<ILExpr> args, String fn, ILType argType) {
		if (args.size() != 1) {
			throw new RuntimeException("Expected 1 arg here for "+fn);
		}
		Expr arg = args.get(0).accept(this, argType);
		if (argType.isNumeric()) {
			// Do the actual operation on the numeric component.
			return tuple(new NodeCallExpr(fn, getValueComponent(arg)), 
					getKnownComponent(arg));
		} else {
			return new NodeCallExpr(fn, arg);
		}
	}
	
	private Expr binary(List<ILExpr> args, String fn, ILType argType) {
		if (args.size() != 2) {
			throw new RuntimeException("Expected 2 args here for "+fn);
		}
		Expr first = args.get(0).accept(this, argType);
		Expr second = args.get(1).accept(this, argType);
		if (argType.isNumeric()) {
			// Hold up, we actually need to un-tuple these and re-tuple the
			// result.
			Expr firstValue = getValueComponent(first);
			Expr firstKnown = getKnownComponent(first);
			Expr secondValue = getValueComponent(second);
			Expr secondKnown = getValueComponent(secondValue);
			
			return tuple(new NodeCallExpr(fn, firstValue, secondValue), 
					and(firstKnown, secondKnown));
			
		} else {
			return new NodeCallExpr(fn, first, second); 
		}
	}
	
	
	private Expr binary(List<ILExpr> args, BinaryOp op,
			ILType argType) {
		if (args.size() != 2) {
			throw new RuntimeException("Expected 2 args here for "+op);
		}
		Expr first = args.get(0).accept(this, argType);
		Expr second = args.get(1).accept(this, argType);
		if (argType.isNumeric()) {
			// Wait, we need to un-tuple these and re-tuple the result.
			Expr firstValue = getValueComponent(first);
			Expr firstKnown = getKnownComponent(first);
			Expr secondValue = getValueComponent(second);
			Expr secondKnown = getKnownComponent(second);
			
			return tuple(new BinaryExpr(firstValue, op, secondValue), 
					and(firstKnown, secondKnown));
		}
		
		
		return new BinaryExpr(first, op, second);
	}
	
	@Override
	public Expr visit(BooleanValue bool, ILType expectedType) {
		if (bool.isTrue()) {
			return LustreNamingConventions.P_TRUE;
		}
		if (bool.isFalse()) {
			return LustreNamingConventions.P_FALSE;
		}
		return LustreNamingConventions.P_UNKNOWN;
	}

	@Override
	public Expr visit(IntegerValue integer, ILType expectedType) {
		return tuple(new IntExpr(integer.getIntValue()), LustreUtil.TRUE);
	}

	@Override
	public Expr visit(RealValue real, ILType expectedType) {
		// Make sure to use the string constructor, since the one that takes
		// an actual number behaves unintuitively (as noted in its JavaDoc). 
		return tuple(new RealExpr(new BigDecimal(real.getRealValue()+"")), 
				LustreUtil.TRUE);
	}

	@Override
	public Expr visit(StringValue string, ILType expectedType) {
		return id(mapper.stringToEnum(string));
	}

	@Override
	public Expr visit(UnknownValue unk, ILType expectedType) {
		switch(expectedType) {
		case BOOLEAN:
			return LustreNamingConventions.P_UNKNOWN;
		case INTEGER:
			return tuple(new IntExpr(0), LustreUtil.FALSE);
		case REAL:
			return tuple(new RealExpr(new BigDecimal(0)), LustreUtil.FALSE);
		case STRING:
			return id(mapper.stringToEnum(unk));
		default: 
			throw new RuntimeException("Need an expected type here, but got "+expectedType);	
		}
	}

	@Override
	public Expr visit(PValueList<?> list, ILType expectedType) {
		List<Expr> values = new ArrayList<Expr>();
		for (PValue v : list) {
			values.add(v.accept(this, list.getType().elementType()));
		}
		// For numerics, we've now got an array of tuples. We need to flip that
		// around into a tuple of two arrays. 
		if (LustreNamingConventions.hasValueAndKnownSplit(list.getType())) {
			List<Expr> actualValues = new ArrayList<>();
			List<Expr> knownValues = new ArrayList<>();
			for (Expr e : values) {
				actualValues.add(getValueComponent(e));
				knownValues.add(getKnownComponent(e));
			}
			return tuple(new ArrayExpr(actualValues), 
					new ArrayExpr(knownValues));
		}
		
		return new ArrayExpr(values);
	}

	@Override
	public Expr visit(CommandHandleState state, ILType expectedType) {
		return id(LustreNamingConventions.getEnumId(state));
	}

	@Override
	public Expr visit(NodeFailureType type, ILType expectedType) {
		return id(LustreNamingConventions.getEnumId(type));
	}

	@Override
	public Expr visit(NodeOutcome outcome, ILType expectedType) {
		return id(LustreNamingConventions.getEnumId(outcome));
	}

	@Override
	public Expr visit(NodeState state, ILType expectedType) {
		return id(LustreNamingConventions.getEnumId(state));
	}

	@Override
	public Expr visit(ILVariable var, ILType expectedType) {
		if (LustreNamingConventions.hasValueAndKnownSplit(var)) {
			// Numerics have a known bit that must also be there
			return tuple(
					id(LustreNamingConventions.getNumericVariableValueId(var)),
					id(LustreNamingConventions.getNumericVariableKnownId(var))
					);
		}
		// Everything else is an enum, which includes an UNKNOWN entry. 
		return id(LustreNamingConventions.getVariableId(var));
	}

	@Override
	public Expr visit(LibraryVar lib, ILType expectedType) {
		throw new RuntimeException("Libraries not supported yet");
	}

	@Override
	public Expr visit(GetNodeStateExpr state, ILType expectedType) {
		return PlanToLustre.getPlexilStateExprForNode(state.getNodeUid());
	}

	@Override
	public Expr visit(AliasExpr alias, ILType expectedType) {
		throw new RuntimeException("Aliases not supported yet");
	}

	@Override
	public Expr visit(RootAncestorExpr root, ILType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

}
