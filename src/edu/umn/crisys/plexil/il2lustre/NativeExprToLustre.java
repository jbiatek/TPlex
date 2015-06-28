package edu.umn.crisys.plexil.il2lustre;

import java.util.List;
import java.util.stream.Collectors;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.LustreUtil;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeConstant;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeEqual;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeExprVisitor;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeOperation;
import edu.umn.crisys.plexil.expr.il.nativebool.PlexilExprToNative;

public class NativeExprToLustre implements NativeExprVisitor<Void, Expr> {
	
	private ILExprToLustre exprToLustre;
	
	public NativeExprToLustre(ILExprToLustre exprTranslator) {
		this.exprToLustre = exprTranslator;
	}

	@Override
	public Expr visitNativeOperation(NativeOperation op, Void param) {
		List<Expr> translated = op.getArgs().stream().map(arg -> arg.accept(this, null))
				.collect(Collectors.toList());
		switch(op.getOperation()) {
		case NOT: 
			return LustreUtil.not(translated.get(0));
		case AND:
			return LustreUtil.and(translated);
		case OR:
			return translated.stream().reduce(LustreUtil::or)
					.orElseThrow(() -> new RuntimeException("No arguments!"));
		}
		throw new RuntimeException("Missing case");
	}

	@Override
	public Expr visitPlexilExprToNative(PlexilExprToNative pen, Void param) {
		Expr thisGuardExpr = pen.getPlexilExpr().accept(exprToLustre, ExprType.BOOLEAN);
		BinaryOp op;
		Expr compareTo;
		switch (pen.getCondition()) {
		case TRUE:
			op = BinaryOp.EQUAL; compareTo = LustreNamingConventions.P_TRUE;
			break;
		case FALSE:
			op = BinaryOp.EQUAL; compareTo = LustreNamingConventions.P_FALSE;
			break;
		case UNKNOWN:
			op = BinaryOp.EQUAL; compareTo = LustreNamingConventions.P_UNKNOWN;
			break;
		case NOTTRUE:
			op = BinaryOp.NOTEQUAL; compareTo = LustreNamingConventions.P_TRUE;
			break;
		case NOTFALSE:
			op = BinaryOp.NOTEQUAL; compareTo = LustreNamingConventions.P_FALSE;
			break;
		case KNOWN:
			op = BinaryOp.NOTEQUAL; compareTo = LustreNamingConventions.P_UNKNOWN;
			break;
		default:
			throw new RuntimeException("Unknown case!");	
		}
		// Add in the proper comparison
		return new BinaryExpr(thisGuardExpr, op, compareTo);
	}

	@Override
	public Expr visitNativeConstant(NativeConstant c, Void param) {
		return c.getValue() ? new IdExpr("true") : new IdExpr("false");
	}

	@Override
	public Expr visitNativeEqual(NativeEqual e, Void param) {
		return new BinaryExpr(e.getLeft().accept(exprToLustre, e.getLeft().getType()), 
				BinaryOp.EQUAL, 
				e.getRight().accept(exprToLustre, e.getRight().getType()));
	}


}
