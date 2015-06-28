package edu.umn.crisys.plexil.il2java.expr;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import edu.umn.crisys.plexil.expr.il.nativebool.NativeConstant;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeEqual;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeExprVisitor;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeOperation;
import edu.umn.crisys.plexil.expr.il.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeOperation.NativeOp;

public class NativeExprToJava implements
		NativeExprVisitor<JCodeModel, JExpression> {

	@Override
	public JExpression visitNativeOperation(NativeOperation op, JCodeModel cm) {
		switch (op.getOperation()) {
		case NOT :
			return op.getArgs().get(0).accept(this, cm).not();
		case AND:
		case OR:
			return op.getArgs().stream().map((arg) -> arg.accept(this, cm))
					.reduce((a, b) -> 
						{ if (op.getOperation() == NativeOp.AND) 
							return a.cand(b);
						  else return a.cor(b); })
					.orElseThrow(() -> new RuntimeException("No args to operator"));
		}
		throw new RuntimeException("Missing case");
	}

	@Override
	public JExpression visitPlexilExprToNative(PlexilExprToNative pen,
			JCodeModel cm) {
		return pen.getPlexilExpr().accept(new IL2Java(), cm)
				.invoke(pen.getCondition().getJavaMethodName());
	}

	@Override
	public JExpression visitNativeConstant(NativeConstant c, JCodeModel param) {
		return c.getValue() ? JExpr.TRUE : JExpr.FALSE;
	}

	@Override
	public JExpression visitNativeEqual(NativeEqual e, JCodeModel cm) {
		return e.getLeft().accept(new IL2Java(), cm)
				.invoke("equals").arg(e.getRight().accept(new IL2Java(), cm));
	}

}
