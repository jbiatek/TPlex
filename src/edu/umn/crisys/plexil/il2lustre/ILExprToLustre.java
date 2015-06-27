package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jkind.lustre.ArrayAccessExpr;
import jkind.lustre.ArrayExpr;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Expr;
import jkind.lustre.NodeCallExpr;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;
import jkind.lustre.visitors.PrettyPrintVisitor;
import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.expr.common.Operation;
import edu.umn.crisys.plexil.il.expr.AliasExpr;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class ILExprToLustre extends ILExprVisitor<PlexilType, jkind.lustre.Expr>{
	
	public static String exprToString(Expr lustre) {
		PrettyPrintVisitor pp = new PrettyPrintVisitor();
		lustre.accept(pp);
		return pp.toString();
	}
	
	private static Expr pre(Expr arg) {
		return new UnaryExpr(UnaryOp.PRE, arg);
	}

	private Map<String, PString> allExpectedStrings = new HashMap<>();

	public ILExprToLustre() { }
	public ILExprToLustre(Map<String, PString> existingMap) {
		allExpectedStrings = existingMap;
	}
	
	public Map<String, PString> getAllExpectedStrings() {
		return allExpectedStrings;
	}
	
	public String stringToEnum(StringValue v) {
		if (v.getString().equals("")) {
			allExpectedStrings.put(LustreNamingConventions.EMPTY_STRING, v);
			return LustreNamingConventions.EMPTY_STRING;
		}
		
		String toReturn = NameUtils.clean(v.getString().replaceAll(" ", "_"));
		if (toReturn.equals("")) {
			throw new RuntimeException("Enumerated string was empty: "+v);
		}
		if (allExpectedStrings.containsKey(toReturn)) {
			PString prev = allExpectedStrings.get(toReturn);
			if (prev.equalTo(v).isNotTrue()) {
				throw new RuntimeException("Name clash: \""+v+"\" and \""+prev+"\"");
			}
		}
		// Okay, it's all good. Save this mapping.
		allExpectedStrings.put(toReturn, v);
		return toReturn;
	}
	
	public String stringToEnum(UnknownValue unk) {
		allExpectedStrings.put(LustreNamingConventions.UNKNOWN_STRING, unk);
		return LustreNamingConventions.UNKNOWN_STRING;
	}
	
	@Override
	public ArrayAccessExpr visitArrayIndex(ArrayIndexExpr array, PlexilType expectedType) {
		return new ArrayAccessExpr(array.getArray().accept(this, null), 
				array.getIndex().accept(this, null));
	}

	@Override
	public Expr visitLookupNow(LookupNowExpr lookup, PlexilType expectedType) {
		return id(LustreNamingConventions.getInputName(lookup));
	}

	@Override
	public Expr visitLookupOnChange(LookupOnChangeExpr lookup, PlexilType expectedType) {
		return id(LustreNamingConventions.getInputName(lookup));
	}

	private static Expr toPBoolean(Expr e) {
		return new NodeCallExpr("to_pboolean", e);

	}
	
	@Override
	public Expr visitOperation(Operation op, PlexilType expectedType) {
		switch (op.getOperator()) {
		// ---------------- Boolean operators
		case AND:
			return multi(op.getArguments(), LustreNamingConventions.AND_OPERATOR, PlexilType.BOOLEAN);
		case NOT:
			return unary(op.getArguments(), LustreNamingConventions.NOT_OPERATOR, PlexilType.BOOLEAN);
		case OR:
			return multi(op.getArguments(), LustreNamingConventions.OR_OPERATOR, PlexilType.BOOLEAN);
		case XOR:
			return binary(op.getArguments(), LustreNamingConventions.XOR_OPERATOR, PlexilType.BOOLEAN);
		case EQ:
			switch (op.getActualArgumentType()) {
			case BOOLEAN: 
				return binary(op.getArguments(), LustreNamingConventions.EQ_BOOL_OPERATOR, PlexilType.BOOLEAN);
			default:
				return toPBoolean(binary(op.getArguments(), BinaryOp.EQUAL, op.getExpectedArgumentType()));
			}
		case NE:
			switch (op.getActualArgumentType()) {
			case BOOLEAN:
				return new NodeCallExpr(LustreNamingConventions.NOT_OPERATOR, 
						binary(op.getArguments(), LustreNamingConventions.EQ_BOOL_OPERATOR, PlexilType.BOOLEAN));
			default:
				return toPBoolean(binary(op.getArguments(), BinaryOp.NOTEQUAL, op.getExpectedArgumentType()));
			}
		case ISKNOWN:
			// TODO: get stricter types so this wrapping and unwrapping goes away!
			switch (op.getActualArgumentType()) {
			case BOOLEAN:
				return toPBoolean(new BinaryExpr(
						op.getArguments().get(0).accept(this, PlexilType.BOOLEAN), 
						BinaryOp.NOTEQUAL, 
						LustreNamingConventions.P_UNKNOWN));
			case OUTCOME:
			case FAILURE:
			case COMMAND_HANDLE:
				// All of these are simple enums
				return toPBoolean(new BinaryExpr(
						op.getArguments().get(0).accept(this, op.getActualArgumentType()),
						BinaryOp.NOTEQUAL,
						op.getActualArgumentType().getUnknown().accept(this, op.getActualArgumentType())));
			case ARRAY:
			case BOOLEAN_ARRAY:
			case INTEGER_ARRAY:
			case REAL_ARRAY:
			case STRING_ARRAY:
			case STATE:
				// All of these are always known
				return LustreNamingConventions.P_TRUE;
			default:
				System.err.println("Missing case in isKnown translation: "+op.getActualArgumentType());
			}

		// ---------------- Numeric operators
		case ABS:
			return unary(op.getArguments(), "abs", op.getActualArgumentType());
		case ADD:
			return reduce(op.getArguments(), BinaryOp.PLUS, op.getActualArgumentType());
		case DIV:
			return binaryReduce(op.getArguments(), BinaryOp.DIVIDE, op.getActualArgumentType());
		case GE:
			return toPBoolean(binary(op.getArguments(), BinaryOp.GREATEREQUAL, PlexilType.NUMERIC));
		case GT:
			return toPBoolean(binary(op.getArguments(), BinaryOp.GREATER, op.getActualArgumentType()));
		case LE:
			return toPBoolean(binary(op.getArguments(), BinaryOp.LESSEQUAL, op.getActualArgumentType()));
		case LT:
			return toPBoolean(binary(op.getArguments(), BinaryOp.LESS, op.getActualArgumentType()));
		case MAX:
			return binary(op.getArguments(), "max", op.getActualArgumentType());
		case MIN:
			return binary(op.getArguments(), "min", op.getActualArgumentType());
		case MOD:
			return binary(op.getArguments(), BinaryOp.MODULUS, op.getActualArgumentType());
		case MUL:
			return multi(op.getArguments(), BinaryOp.MULTIPLY, op.getActualArgumentType());
		case SQRT:
			return unary(op.getArguments(), "sqrt", op.getActualArgumentType());
		case SUB:
			return binary(op.getArguments(), BinaryOp.MINUS, op.getActualArgumentType());

		// ---------------- String operators (not supported)
		case CONCAT:
			throw new RuntimeException("String concatenation not supported when translating to Lustre");
		// ---------------- Casting operators (are these needed?)
		case CAST_BOOL:
			return op.getArguments().get(0).accept(this, PlexilType.BOOLEAN);
		case CAST_NUMERIC:
			return op.getSingleExpectedArgument().accept(this, op.getActualArgumentType());
		case CAST_INT:
			return new NodeCallExpr("floor", op.getSingleExpectedArgument().accept(this, PlexilType.NUMERIC));
		case CAST_REAL:
			return new NodeCallExpr("real", op.getSingleExpectedArgument().accept(this, PlexilType.NUMERIC));
		case CAST_STRING:
			return op.getArguments().get(0).accept(this, PlexilType.STRING);
		// ---------------- Node operators, which don't belong in the IL
		case GET_COMMAND_HANDLE:
			throw new RuntimeException("Handles should be resolved by now!");
		case GET_FAILURE:
			throw new RuntimeException("Node failure variables should be resolved by now!");
		case GET_OUTCOME:
			throw new RuntimeException("Node outcome variables should be resolved by now!");
		case GET_STATE:
			throw new RuntimeException("Node states should be resolved to an expression by now!");
		default:
			throw new RuntimeException("Missing operator: "+op.getOperator());
		}
	}
	
	private Expr binaryReduce(List<Expression> args, BinaryOp op, PlexilType argType) {
		if (args.size() != 2) {
			throw new RuntimeException("Expected 2 args, found "+args.size());
		}
		return reduce(args, op, argType);
	}
	
	private Expr reduce(List<Expression> args, BinaryOp op, PlexilType argType) {
		return args.stream()
				.map((arg) -> arg.ensureType(argType).accept(this, argType))
				.reduce((l, r) -> new BinaryExpr(l, op, r))
				.orElseThrow(() -> new RuntimeException("No arguments passed in!"));
	}


	private Expr unary(List<Expression> args, String fn, PlexilType argType) {
		if (args.size() != 1) {
			throw new RuntimeException("Expected 1 arg here for "+fn);
		}
		return new NodeCallExpr(fn, args.get(0).accept(this, argType));
	}
	
	private Expr binary(List<Expression> args, String fn, PlexilType argType) {
		if (args.size() != 2) {
			throw new RuntimeException("Expected 2 args here for "+fn);
		}
		return new NodeCallExpr(fn, args.get(0).accept(this, argType),
				args.get(1).accept(this, argType));
	}
	
	
	private Expr binary(List<Expression> args, BinaryOp op,
			PlexilType argType) {
		if (args.size() != 2) {
			throw new RuntimeException("Expected 2 args here for "+op);
		}
		return new BinaryExpr(args.get(0).accept(this, argType), op, 
				args.get(1).accept(this, argType));
	}
	
	private Expr multi(List<Expression> args, String fn, PlexilType argType) {
		Expr ret = null;
		for (Expression arg : args) {
			if (ret == null) {
				ret = arg.accept(this, argType);
			} else {
				ret = new NodeCallExpr(fn, ret, arg.accept(this, argType));
			}
		}
		return ret;
	}
	
	private Expr multi(List<Expression> args, BinaryOp op, PlexilType argType) {
		Expr ret = null;
		for (Expression arg : args) {
			if (ret == null) { 
				ret = arg.accept(this, argType);
			} else {
				ret = new BinaryExpr(ret, op, arg.accept(this, argType));
			}
		}
		return ret;
	}

	@Override
	public Expr visitBooleanValue(BooleanValue bool, PlexilType expectedType) {
		if (bool.isTrue()) {
			return LustreNamingConventions.P_TRUE;
		}
		if (bool.isFalse()) {
			return LustreNamingConventions.P_FALSE;
		}
		return LustreNamingConventions.P_UNKNOWN;
	}

	@Override
	public Expr visitIntegerValue(IntegerValue integer, PlexilType expectedType) {
		return id(integer.getIntValue()+"");
	}

	@Override
	public Expr visitRealValue(RealValue real, PlexilType expectedType) {
		return id(real.getRealValue()+"");
	}

	@Override
	public Expr visitStringValue(StringValue string, PlexilType expectedType) {
		return id(stringToEnum(string));
	}

	@Override
	public Expr visitUnknownValue(UnknownValue unk, PlexilType expectedType) {
		switch(expectedType) {
		case BOOLEAN:
			return LustreNamingConventions.P_UNKNOWN;
		case INTEGER:
			return id("0");
		case REAL:
			return id("0.0");
		case STRING:
			return id(stringToEnum(unk));
		default: 
			throw new RuntimeException("Unknowns not supported yet except booleans -- "+expectedType);	
		}
	}

	@Override
	public Expr visitPValueList(PValueList<?> list, PlexilType expectedType) {
		List<Expr> values = new ArrayList<Expr>();
		for (PValue v : list) {
			values.add(v.accept(this, list.getType().elementType()));
		}
		return new ArrayExpr(values);
	}

	@Override
	public Expr visitCommandHandleState(CommandHandleState state, PlexilType expectedType) {
		return id(LustreNamingConventions.getEnumId(state));
	}

	@Override
	public Expr visitNodeFailure(NodeFailureType type, PlexilType expectedType) {
		return id(LustreNamingConventions.getEnumId(type));
	}

	@Override
	public Expr visitNodeOutcome(NodeOutcome outcome, PlexilType expectedType) {
		return id(LustreNamingConventions.getEnumId(outcome));
	}

	@Override
	public Expr visitNodeState(NodeState state, PlexilType expectedType) {
		return id(LustreNamingConventions.getEnumId(state));
	}

	@Override
	public Expr visitSimple(SimpleVar var, PlexilType expectedType) {
		return pre(id(LustreNamingConventions.getVariableId(var)));
	}

	@Override
	public Expr visitArray(ArrayVar array, PlexilType expectedType) {
		return pre(id(LustreNamingConventions.getVariableId(array)));
	}

	@Override
	public Expr visitLibrary(LibraryVar lib, PlexilType expectedType) {
		throw new RuntimeException("Libraries not supported yet");
	}

	@Override
	public Expr visitGetNodeState(GetNodeStateExpr state, PlexilType expectedType) {
		return PlanToLustre.getPlexilStateExprForNode(state.getNodeUid());
	}

	@Override
	public Expr visitAlias(AliasExpr alias, PlexilType expectedType) {
		throw new RuntimeException("Aliases not supported yet");
	}

	@Override
	public Expr visitRootParentState(RootParentStateExpr state, PlexilType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

	@Override
	public Expr visitRootParentExit(RootAncestorExitExpr ancExit, PlexilType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

	@Override
	public Expr visitRootParentEnd(RootAncestorEndExpr ancEnd, PlexilType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

	@Override
	public Expr visitRootParentInvariant(RootAncestorInvariantExpr ancInv,
			PlexilType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

}
