package edu.umn.crisys.plexil.il2lustre;

import java.util.List;
import java.util.stream.Collectors;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.LustreUtil;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeConstant;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExprVisitor;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeOperation;
import edu.umn.crisys.plexil.il.expr.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class NativeExprToLustre implements NativeExprVisitor<Void, Expr> {

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
		Expr thisGuardExpr = ILExprToLustre.toLustre(pen.getPlexilExpr(), PlexilType.BOOLEAN);
		BinaryOp op;
		Expr compareTo;
		switch (pen.getCondition()) {
		case TRUE:
			op = BinaryOp.EQUAL; compareTo = ILExprToLustre.P_TRUE;
			break;
		case FALSE:
			op = BinaryOp.EQUAL; compareTo = ILExprToLustre.P_FALSE;
			break;
		case UNKNOWN:
			op = BinaryOp.EQUAL; compareTo = ILExprToLustre.P_UNKNOWN;
			break;
		case NOTTRUE:
			op = BinaryOp.NOTEQUAL; compareTo = ILExprToLustre.P_TRUE;
			break;
		case NOTFALSE:
			op = BinaryOp.NOTEQUAL; compareTo = ILExprToLustre.P_FALSE;
			break;
		case KNOWN:
			op = BinaryOp.NOTEQUAL; compareTo = ILExprToLustre.P_UNKNOWN;
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


}
