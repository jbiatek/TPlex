package edu.umn.crisys.plexil.il.optimizations;

import java.util.List;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.Operation;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeConstant;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExpr;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExprVisitor;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeOperation;
import edu.umn.crisys.plexil.il.expr.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeOperation.NativeOp;
import edu.umn.crisys.plexil.il.expr.nativebool.PlexilExprToNative.Condition;

public class UnknownBiasing implements NativeExprVisitor<Void, NativeExpr> {
	
	public static void optimize(Plan ilPlan) {
		ilPlan.getMachines().stream()
		.forEach((nsm) -> nsm.getTransitions().stream()
				.forEach((t) -> t.guard = t.guard.accept(new UnknownBiasing(), null)));
	}

	@Override
	public NativeExpr visitNativeOperation(NativeOperation op, Void param) {
		// We're already native, but child expressions might have things to do
		return new NativeOperation(op.getOperation(),
				op.getArgs().stream()
				.map((arg) -> arg.accept(this, null))
				.collect(Collectors.toList()));
	}
	
	private static NativeExpr collect(NativeOp op, Condition c, List<Expression> args) {
		return new NativeOperation(op, args.stream()
				.map((arg) -> new PlexilExprToNative((ILExpression)arg, c))
				.collect(Collectors.toList()));
	}

	@Override
	public NativeExpr visitPlexilExprToNative(PlexilExprToNative original, Void param) {
		// Is this something that we can push down on?
		if (original.getPlexilExpr() instanceof Operation) {
			NativeExpr ret;
			Operation oper = (Operation) original.getPlexilExpr();
			switch(oper.getOperator()) {
			case AND:
				switch(original.getCondition()) {
				case TRUE:
					// (a && b).isTrue() becomes (a.isTrue() && b.isTrue())
					// We want to know if all our args are true. 
					ret = collect(NativeOp.AND, Condition.TRUE, oper.getArguments());
					break;
				case FALSE:
					// (a && b).isFalse() becomes
					// (a.isFalse() || b.isFalse())
					// (if any of them are false, then the whole thing is false)
					ret = collect(NativeOp.OR, Condition.FALSE, oper.getArguments());
					break;
				case UNKNOWN:
					// (a && b).isUnknown() becomes
					// ((a.isUnknown() || b.isUnknown()) && (a.isNotFalse() && b.isNotFalse()))
					NativeExpr oneIsUnknown = collect(NativeOp.OR, Condition.UNKNOWN, oper.getArguments());
					NativeExpr noneAreFalse = collect(NativeOp.OR, Condition.NOTFALSE, oper.getArguments());

					ret = new NativeOperation(NativeOp.AND, oneIsUnknown, noneAreFalse);
					break;
				case NOTTRUE:
					// (a && b).isNotTrue() becomes 
					// (a.isNotTrue() || b.isNotTrue())
					ret = collect(NativeOp.OR, Condition.NOTTRUE, oper.getArguments());
					break;
				case NOTFALSE:
					// (a && b).isNotFalse() becomes
					// (a.isNotFalse() && b.isNotFalse())
					ret = collect(NativeOp.AND, Condition.NOTFALSE, oper.getArguments());
					break;
				case KNOWN:
					// (a && b).isKnown() becomes 
					// (a.isKnown() && b.isKnown()) || (a.isFalse() || b.isFalse())
					NativeExpr allAreKnown = collect(NativeOp.AND, Condition.KNOWN, oper.getArguments());
					NativeExpr oneIsFalse = collect(NativeOp.OR, Condition.FALSE, oper.getArguments());
					ret = new NativeOperation(NativeOp.OR, allAreKnown, oneIsFalse);
					break;
				default:
					throw new RuntimeException("Missing case");
				}
				break;
			case OR:
				switch(original.getCondition()) {
				case TRUE:
					// (a || b).isTrue() becomes (a.isTrue() || b.isTrue())
					ret = collect(NativeOp.OR, Condition.TRUE, oper.getArguments());
					break;
				case FALSE:
					// (a || b).isFalse() becomes (a.isFalse() && b.isFalse())
					// (both have to be false for the whole thing to be false)
					ret = collect(NativeOp.AND, Condition.FALSE, oper.getArguments());
					break;
				case UNKNOWN:
					// (a || b).isUnknown() becomes 
					NativeExpr oneIsUnknown = collect(NativeOp.OR, Condition.UNKNOWN, oper.getArguments());
					NativeExpr noneAreTrue = collect(NativeOp.OR, Condition.NOTTRUE, oper.getArguments());
					ret = new NativeOperation(NativeOp.AND, oneIsUnknown, noneAreTrue);
					break;
				case NOTTRUE:
					// (a || b).isNotTrue() becomes (a.isNotTrue() && b.isNotTrue())
					ret = collect(NativeOp.AND, Condition.NOTTRUE, oper.getArguments());
					break;
				case NOTFALSE:
					// (a || b).isNotFalse() becomes (a.isNotFalse() || b.isNotFalse())
					ret = collect(NativeOp.OR, Condition.NOTFALSE, oper.getArguments());
					break;
				case KNOWN:
					// (a || b).isKnown() becomes
					NativeExpr allAreKnown = collect(NativeOp.AND, Condition.KNOWN, oper.getArguments());
					NativeExpr oneIsTrue = collect(NativeOp.OR, Condition.TRUE, oper.getArguments());
					ret = new NativeOperation(NativeOp.OR, allAreKnown, oneIsTrue);
					break;
				default:
					throw new RuntimeException("Missing case");
				}
				break;
			case NOT:
				// Remove the NOT operator and just flip the bias around.
				switch(original.getCondition()) {
				case TRUE:
					// (!a).isTrue() -> a.isFalse()
					ret = PlexilExprToNative.isFalse(oper.getArguments().get(0));
					break;
				case FALSE:
					// (!a).isFalse() -> a.isTrue();
					ret = PlexilExprToNative.isTrue(oper.getArguments().get(0));
					break;
				case UNKNOWN:
					// (!a).isUnknown() -> a.isUnknown();
					ret = PlexilExprToNative.isUnknown(oper.getArguments().get(0));
					break;
				case NOTTRUE:
					// (!a).isNotTrue() -> a.isNotFalse();
					ret = PlexilExprToNative.isNotFalse(oper.getArguments().get(0));
					break;
				case NOTFALSE: 
					ret = PlexilExprToNative.isNotTrue(oper.getArguments().get(0));
					break;
				case KNOWN:
					ret = PlexilExprToNative.isKnown(oper.getArguments().get(0));
					break;
				default:
					throw new RuntimeException("Missing case");
				}
				break;
			default:
				// We don't care about other operators
				return original;
			}
			// Okay, if we're here, we've got a new expression. Give it a chance
			// to bias more, and that's it.
			return ret.accept(this, null);	
		} else {
			// Nothing to do, leave it as is
			return original;
		}
	}

	@Override
	public NativeExpr visitNativeConstant(NativeConstant c, Void param) {
		// These should probably be removed by something somewhere...
		return c;
	}


}
