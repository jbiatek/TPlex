package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.*;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import jkind.lustre.visitors.PrettyPrintVisitor;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.NamedCondition;
import edu.umn.crisys.plexil.expr.ast.ArrayIndexExpr;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILOperation;
import edu.umn.crisys.plexil.expr.il.RootAncestorExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
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
		// This should have been de-inlined for us. 
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
	public Expr visit(NativeBool b, ExprType param) {
		return b.getValue() ? LustreUtil.TRUE : LustreUtil.FALSE;
	}
	
	
	@Override
	public Expr visit(ILOperation op, ExprType expectedType) {
		switch (op.getOperator()) {
		// ---------------- Native boolean operators
		case AND:
			return multi(op.getArguments(), BinaryOp.AND, ExprType.NATIVE_BOOL);
		case OR:
			return multi(op.getArguments(), BinaryOp.OR, ExprType.NATIVE_BOOL);
		case NOT:
			return LustreUtil.not(op.getUnaryArg().accept(this, ExprType.NATIVE_BOOL));
		case IS_TRUE:
			return new BinaryExpr(op.getUnaryArg().accept(this, ExprType.BOOLEAN), 
					BinaryOp.EQUAL, LustreNamingConventions.P_TRUE);
		case IS_FALSE:
			return new BinaryExpr(op.getUnaryArg().accept(this, ExprType.BOOLEAN), 
					BinaryOp.EQUAL, LustreNamingConventions.P_FALSE);
		case IS_NOT_TRUE:
			return new BinaryExpr(op.getUnaryArg().accept(this, ExprType.BOOLEAN), 
					BinaryOp.NOTEQUAL, LustreNamingConventions.P_TRUE);
		case IS_NOT_FALSE:
			return new BinaryExpr(op.getUnaryArg().accept(this, ExprType.BOOLEAN), 
					BinaryOp.NOTEQUAL, LustreNamingConventions.P_FALSE);
		case IS_UNKNOWN:
			return new BinaryExpr(op.getUnaryArg().accept(this, ExprType.BOOLEAN), 
					BinaryOp.EQUAL, LustreNamingConventions.P_UNKNOWN);
		case IS_KNOWN:
			return new BinaryExpr(op.getUnaryArg().accept(this, ExprType.BOOLEAN), 
					BinaryOp.NOTEQUAL, LustreNamingConventions.P_UNKNOWN);
		case DIRECT_COMPARE:
			return binary(op.getArguments(), BinaryOp.EQUAL, ExprType.UNKNOWN);
		case WRAP_BOOLEAN:
			return toPBoolean(op.getUnaryArg().accept(this, ExprType.BOOLEAN));
			
			
		// ---------------- Plexil boolean operators
		case PAND:
			return multi(op.getArguments(), LustreNamingConventions.AND_OPERATOR, ExprType.BOOLEAN);
		case PNOT:
			return unary(op.getArguments(), LustreNamingConventions.NOT_OPERATOR, ExprType.BOOLEAN);
		case POR:
			return multi(op.getArguments(), LustreNamingConventions.OR_OPERATOR, ExprType.BOOLEAN);
		case PXOR:
			return binary(op.getArguments(), LustreNamingConventions.XOR_OPERATOR, ExprType.BOOLEAN);
		case PBOOL_EQ:
			return binary(op.getArguments(), LustreNamingConventions.EQ_BOOL_OPERATOR, ExprType.BOOLEAN);
		case PINT_EQ:
		case PREAL_EQ:
		case PSTRING_EQ: 
		case PSTATE_EQ:
		case POUTCOME_EQ:
		case PFAILURE_EQ:
		case PHANDLE_EQ:
				return toPBoolean(binary(op.getArguments(), BinaryOp.EQUAL, 
						op.getBinaryFirst().getType()));
		case ISKNOWN_OPERATOR:
			// note: this operator returns a *native* bool
			switch (op.getUnaryArg().getType()) {
			case BOOLEAN:
				return new BinaryExpr(
						op.getArguments().get(0).accept(this, ExprType.BOOLEAN), 
						BinaryOp.NOTEQUAL, 
						LustreNamingConventions.P_UNKNOWN);
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

		// ---------------- Numeric operators
		case PINT_ABS:
		case PREAL_ABS:
			return unary(op.getArguments(), "abs", op.getUnaryArg().getType());
		case PINT_ADD:
		case PREAL_ADD:
			return reduce(op.getArguments(), BinaryOp.PLUS, op.getBinaryFirst().getType());
		case PINT_DIV:
		case PREAL_DIV:
			return binaryReduce(op.getArguments(), BinaryOp.DIVIDE, op.getBinaryFirst().getType());
		case PINT_GE:
		case PREAL_GE:
			return toPBoolean(binary(op.getArguments(), BinaryOp.GREATEREQUAL, 
					op.getBinaryFirst().getType()));
		case PINT_GT:
		case PREAL_GT:
			return toPBoolean(binary(op.getArguments(), BinaryOp.GREATER, 
					op.getBinaryFirst().getType()));
		case PINT_LE:
		case PREAL_LE:
			return toPBoolean(binary(op.getArguments(), BinaryOp.LESSEQUAL, 
					op.getBinaryFirst().getType()));
		case PINT_LT:
		case PREAL_LT:
			return toPBoolean(binary(op.getArguments(), BinaryOp.LESS, 
					op.getBinaryFirst().getType()));
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
			return multi(op.getArguments(), BinaryOp.MULTIPLY, 
					op.getBinaryFirst().getType());
		case PREAL_SQRT:
			return unary(op.getArguments(), "sqrt", ExprType.REAL);
		case PINT_SUB:
		case PREAL_SUB:
			return binary(op.getArguments(), BinaryOp.MINUS, 
					op.getBinaryFirst().getType());

		// ---------------- String operators (not supported)
		case PSTR_CONCAT:
			throw new RuntimeException("String concatenation not supported when translating to Lustre");
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
				.map((arg) -> arg.accept(this, argType))
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
	public Expr visit(RootAncestorExpr root, ExprType expectedType) {
		throw new RuntimeException("Libraries aren't supported");
	}

}
