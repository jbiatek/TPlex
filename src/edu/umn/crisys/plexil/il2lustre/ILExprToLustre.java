package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.id;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jkind.lustre.ArrayAccessExpr;
import jkind.lustre.ArrayExpr;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.CastExpr;
import jkind.lustre.Expr;
import jkind.lustre.IntExpr;
import jkind.lustre.NamedType;
import jkind.lustre.NodeCallExpr;
import jkind.lustre.RealExpr;
import jkind.lustre.visitors.PrettyPrintVisitor;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.NamedCondition;
import edu.umn.crisys.plexil.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.expr.common.Operation;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILExprVisitor;
import edu.umn.crisys.plexil.expr.il.RootAncestorEndExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorExitExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.expr.il.RootParentStateExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class ILExprToLustre extends ILExprVisitor<ExprType, jkind.lustre.Expr>{
	
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
	public Expr visit(NamedCondition named, ExprType expected) {
		// This should have been inlined for us. 
		return id(LustreNamingConventions.getNamedConditionId(named));
	}
	
	@Override
	public ArrayAccessExpr visit(ArrayIndexExpr array, ExprType expectedType) {
		// Arrays are static right now, otherwise this would need a pre(). 
		return new ArrayAccessExpr(array.getArray().accept(this, null), 
				array.getIndex().accept(this, null));
	}

	@Override
	public Expr visit(LookupNowExpr lookup, ExprType expectedType) {
		return id(LustreNamingConventions.getInputName(lookup));
	}

	@Override
	public Expr visit(LookupOnChangeExpr lookup, ExprType expectedType) {
		return id(LustreNamingConventions.getInputName(lookup));
	}

	private static Expr toPBoolean(Expr e) {
		return new NodeCallExpr("to_pboolean", e);

	}
	
	@Override
	public Expr visit(Operation op, ExprType expectedType) {
		switch (op.getOperator()) {
		// ---------------- Boolean operators
		case AND:
			return multi(op.getArguments(), LustreNamingConventions.AND_OPERATOR, ExprType.BOOLEAN);
		case NOT:
			return unary(op.getArguments(), LustreNamingConventions.NOT_OPERATOR, ExprType.BOOLEAN);
		case OR:
			return multi(op.getArguments(), LustreNamingConventions.OR_OPERATOR, ExprType.BOOLEAN);
		case XOR:
			return binary(op.getArguments(), LustreNamingConventions.XOR_OPERATOR, ExprType.BOOLEAN);
		case EQ:
			switch (op.getActualArgumentType()) {
			case BOOLEAN: 
				return binary(op.getArguments(), LustreNamingConventions.EQ_BOOL_OPERATOR, ExprType.BOOLEAN);
			default:
				return toPBoolean(binary(op.getArguments(), BinaryOp.EQUAL, op.getExpectedArgumentType()));
			}
		case NE:
			switch (op.getActualArgumentType()) {
			case BOOLEAN:
				return new NodeCallExpr(LustreNamingConventions.NOT_OPERATOR, 
						binary(op.getArguments(), LustreNamingConventions.EQ_BOOL_OPERATOR, ExprType.BOOLEAN));
			default:
				return toPBoolean(binary(op.getArguments(), BinaryOp.NOTEQUAL, op.getExpectedArgumentType()));
			}
		case ISKNOWN:
			// TODO: get stricter types so this wrapping and unwrapping goes away!
			switch (op.getActualArgumentType()) {
			case BOOLEAN:
				return toPBoolean(new BinaryExpr(
						op.getArguments().get(0).accept(this, ExprType.BOOLEAN), 
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
			return toPBoolean(binary(op.getArguments(), BinaryOp.GREATEREQUAL, ExprType.NUMERIC));
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
			return op.getArguments().get(0).accept(this, ExprType.BOOLEAN);
		case CAST_NUMERIC:
			return op.getSingleExpectedArgument().accept(this, op.getActualArgumentType());
		case CAST_INT:
			return new CastExpr(NamedType.INT, op.getSingleExpectedArgument().accept(this, ExprType.NUMERIC));
		case CAST_REAL:
			return new CastExpr(NamedType.REAL, op.getSingleExpectedArgument().accept(this, ExprType.NUMERIC));
		case CAST_STRING:
			return op.getArguments().get(0).accept(this, ExprType.STRING);
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
	
	private Expr binaryReduce(List<Expression> args, BinaryOp op, ExprType argType) {
		if (args.size() != 2) {
			throw new RuntimeException("Expected 2 args, found "+args.size());
		}
		return reduce(args, op, argType);
	}
	
	private Expr reduce(List<Expression> args, BinaryOp op, ExprType argType) {
		return args.stream()
				.map((arg) -> arg.ensureType(argType).accept(this, argType))
				.reduce((l, r) -> new BinaryExpr(l, op, r))
				.orElseThrow(() -> new RuntimeException("No arguments passed in!"));
	}


	private Expr unary(List<Expression> args, String fn, ExprType argType) {
		if (args.size() != 1) {
			throw new RuntimeException("Expected 1 arg here for "+fn);
		}
		return new NodeCallExpr(fn, args.get(0).accept(this, argType));
	}
	
	private Expr binary(List<Expression> args, String fn, ExprType argType) {
		if (args.size() != 2) {
			throw new RuntimeException("Expected 2 args here for "+fn);
		}
		return new NodeCallExpr(fn, args.get(0).accept(this, argType),
				args.get(1).accept(this, argType));
	}
	
	
	private Expr binary(List<Expression> args, BinaryOp op,
			ExprType argType) {
		if (args.size() != 2) {
			throw new RuntimeException("Expected 2 args here for "+op);
		}
		return new BinaryExpr(args.get(0).accept(this, argType), op, 
				args.get(1).accept(this, argType));
	}
	
	private Expr multi(List<Expression> args, String fn, ExprType argType) {
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
	
	private Expr multi(List<Expression> args, BinaryOp op, ExprType argType) {
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
	public Expr visit(BooleanValue bool, ExprType expectedType) {
		if (bool.isTrue()) {
			return LustreNamingConventions.P_TRUE;
		}
		if (bool.isFalse()) {
			return LustreNamingConventions.P_FALSE;
		}
		return LustreNamingConventions.P_UNKNOWN;
	}

	@Override
	public Expr visit(IntegerValue integer, ExprType expectedType) {
		return new IntExpr(integer.getIntValue());
	}

	@Override
	public Expr visit(RealValue real, ExprType expectedType) {
		return new RealExpr(new BigDecimal(real.getRealValue()));
	}

	@Override
	public Expr visit(StringValue string, ExprType expectedType) {
		return id(mapper.stringToEnum(string));
	}

	@Override
	public Expr visit(UnknownValue unk, ExprType expectedType) {
		switch(expectedType) {
		case BOOLEAN:
			return LustreNamingConventions.P_UNKNOWN;
		case INTEGER:
			return new IntExpr(0);
		case REAL:
			return new RealExpr(new BigDecimal(0));
		case STRING:
			return id(mapper.stringToEnum(unk));
		default: 
			throw new RuntimeException("Unknowns not supported yet except booleans -- "+expectedType);	
		}
	}

	@Override
	public Expr visit(PValueList<?> list, ExprType expectedType) {
		List<Expr> values = new ArrayList<Expr>();
		for (PValue v : list) {
			values.add(v.accept(this, list.getType().elementType()));
		}
		return new ArrayExpr(values);
	}

	@Override
	public Expr visit(CommandHandleState state, ExprType expectedType) {
		return id(LustreNamingConventions.getEnumId(state));
	}

	@Override
	public Expr visit(NodeFailureType type, ExprType expectedType) {
		return id(LustreNamingConventions.getEnumId(type));
	}

	@Override
	public Expr visit(NodeOutcome outcome, ExprType expectedType) {
		return id(LustreNamingConventions.getEnumId(outcome));
	}

	@Override
	public Expr visit(NodeState state, ExprType expectedType) {
		return id(LustreNamingConventions.getEnumId(state));
	}

	@Override
	public Expr visit(SimpleVar var, ExprType expectedType) {
		return id(LustreNamingConventions.getVariableId(var));
	}

	@Override
	public Expr visit(ArrayVar array, ExprType expectedType) {
		return id(LustreNamingConventions.getVariableId(array));
	}

	@Override
	public Expr visit(LibraryVar lib, ExprType expectedType) {
		throw new RuntimeException("Libraries not supported yet");
	}

	@Override
	public Expr visit(GetNodeStateExpr state, ExprType expectedType) {
		return PlanToLustre.getPlexilStateExprForNode(state.getNodeUid());
	}

	@Override
	public Expr visit(AliasExpr alias, ExprType expectedType) {
		throw new RuntimeException("Aliases not supported yet");
	}

	@Override
	public Expr visit(RootParentStateExpr state, ExprType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

	@Override
	public Expr visit(RootAncestorExitExpr ancExit, ExprType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

	@Override
	public Expr visit(RootAncestorEndExpr ancEnd, ExprType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

	@Override
	public Expr visit(RootAncestorInvariantExpr ancInv,
			ExprType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

}
