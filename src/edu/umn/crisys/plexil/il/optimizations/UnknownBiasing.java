package edu.umn.crisys.plexil.il.optimizations;

import java.util.List;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ast.ASTOperation;
import edu.umn.crisys.plexil.expr.il.ILExprModifier;
import edu.umn.crisys.plexil.expr.il.ILOperation;
import edu.umn.crisys.plexil.expr.il.ILOperator;
import edu.umn.crisys.plexil.il.Plan;

public class UnknownBiasing extends ILExprModifier<Void> {

	/**
	 * Replace some 3-valued operations with 2-valued ones, allowing more
	 * native expressions to be used. 
	 * 
	 * @param ilPlan
	 */
	public static void optimize(Plan ilPlan) {
		ilPlan.modifyAllExpressions(new UnknownBiasing(), null);
	}

	private static Expression collect(ILOperator topOperator, 
			ILOperator insideOperator, List<Expression> args) {
		return topOperator.expr(args.stream().map(arg -> insideOperator.expr(arg))
				.collect(Collectors.toList()));
	}



	@Override
	public Expression visit(ILOperation oper, Void param) {
		// Looking for PLEXIL booleans that are being biased into regular bools.
		if (oper.getType() != ExprType.NATIVE_BOOL || 
				oper.getArguments().size() != 1) {
			// Nope, not applicable. 
			return visitComposite(oper, null);
		}
		
		// We can only push the bias down PLEXIL's boolean operators.
		Expression argSomeType = oper.getUnaryArg();
		if ( ! (argSomeType instanceof ILOperation)) {
			return visitComposite(oper, null);
		}
		ILOperation arg = (ILOperation) argSomeType;
		if (arg.getType() != ExprType.BOOLEAN) {
			return visitComposite(oper, null);
		}
		// Okay, this is at least a PBoolean -> bool situation. 

		Expression ret;
		switch(oper.getOperator()) {
		case IS_TRUE:
			switch (arg.getOperator()) {
			case PAND:
				// (a && b).isTrue() becomes (a.isTrue() && b.isTrue())
				// We want to know if all our args are true. 
				ret = collect(ILOperator.AND, ILOperator.IS_TRUE, arg.getArguments());
				break;
			case POR:
				// (a || b).isTrue() becomes (a.isTrue() || b.isTrue())
				ret = collect(ILOperator.OR, ILOperator.IS_TRUE, arg.getArguments());
				break;
			case PNOT:
				// (!a).isTrue() -> a.isFalse()
				ret = ILOperator.IS_FALSE.expr(arg.getUnaryArg());
				break;
			default:
				// Nothing to change here after all
				return visitComposite(oper, null);
			}
			break;
		case IS_FALSE:
			switch (arg.getOperator()) {
			case PAND:
				// (a && b).isFalse() becomes
				// (a.isFalse() || b.isFalse())
				// (if any of them are false, then the whole thing is false)
				ret = collect(ILOperator.OR, ILOperator.IS_FALSE, arg.getArguments());
				break;

			case POR:
				// (a || b).isFalse() becomes (a.isFalse() && b.isFalse())
				// (both have to be false for the whole thing to be false)
				ret = collect(ILOperator.AND, ILOperator.IS_FALSE, arg.getArguments());
				break;

			case PNOT:
				// (!a).isFalse() -> a.isTrue();
				ret = ILOperator.IS_TRUE.expr(arg.getUnaryArg());
				break;
			default:
				// Nothing to change here after all
				return visitComposite(oper, null);
			}
			break;
		case IS_UNKNOWN:
			Expression oneIsUnknown = collect(ILOperator.OR, ILOperator.IS_UNKNOWN, arg.getArguments());
			switch (arg.getOperator()) {
			case PAND:
				// (a && b).isUnknown() becomes
				// ((a.isUnknown() || b.isUnknown()) && (a.isNotFalse() && b.isNotFalse()))
				Expression noneAreFalse = collect(ILOperator.OR, ILOperator.IS_NOT_FALSE, arg.getArguments());

				ret = ILOperator.AND.expr(oneIsUnknown, noneAreFalse);
				break;
			case POR:
				// (a || b).isUnknown() becomes 
				// ((a.isUnknown() || b.isUnknown()) && (a.isNotTrue() && b.isNotTrue()))
				Expression noneAreTrue = collect(ILOperator.OR, ILOperator.IS_NOT_TRUE, arg.getArguments());
				ret = ILOperator.AND.expr(oneIsUnknown, noneAreTrue);
				break;
			case PNOT:
				// The NOT operator doesn't change whether it's known or not,
				// so we can just strip it out entirely. 
				// (!a).isUnknown() -> a.isUnknown();
				ret = ILOperator.IS_UNKNOWN.expr(arg.getUnaryArg());
				break;
			default:
				// Nothing to change here after all
				return visitComposite(oper, null);
			}
			break;
		case IS_NOT_TRUE:
			switch (arg.getOperator()) {
			case PAND:
				// (a && b).isNotTrue() becomes 
				// (a.isNotTrue() || b.isNotTrue())
				ret = collect(ILOperator.OR, ILOperator.IS_NOT_TRUE, arg.getArguments());
				break;

			case POR:
				// (a || b).isNotTrue() becomes (a.isNotTrue() && b.isNotTrue())
				ret = collect(ILOperator.AND, ILOperator.IS_NOT_TRUE, arg.getArguments());
				break;

			case PNOT:
				// (!a).isNotTrue() -> a.isNotFalse();
				ret = ILOperator.IS_NOT_FALSE.expr(arg.getUnaryArg());
				break;

			default:
				// Nothing to change here after all
				return visitComposite(oper, null);
			}
			break;
		case IS_NOT_FALSE:
			switch (arg.getOperator()) {
			case PAND:
				// (a && b).isNotFalse() becomes
				// (a.isNotFalse() && b.isNotFalse())
				ret = collect(ILOperator.AND, ILOperator.IS_NOT_FALSE, arg.getArguments());
				break;

			case POR:
				// (a || b).isNotFalse() becomes (a.isNotFalse() || b.isNotFalse())
				ret = collect(ILOperator.OR, ILOperator.IS_NOT_FALSE, arg.getArguments());
				break;

			case PNOT:
				ret = ILOperator.IS_NOT_TRUE.expr(arg.getUnaryArg());
				break;

			default:
				// Nothing to change here after all
				return visitComposite(oper, null);
			}
			break;
		case IS_KNOWN:
			Expression allAreKnown = collect(ILOperator.AND, ILOperator.IS_KNOWN, arg.getArguments());
			switch (arg.getOperator()) {
			case PAND:
				// (a && b).isKnown() becomes 
				// (a.isKnown() && b.isKnown()) || (a.isFalse() || b.isFalse())
				Expression oneIsFalse = collect(ILOperator.OR, ILOperator.IS_FALSE, arg.getArguments());
				ret = ILOperator.OR.expr(allAreKnown, oneIsFalse);
				break;

			case POR:
				// (a || b).isKnown() becomes
				Expression oneIsTrue = collect(ILOperator.OR, ILOperator.IS_TRUE, arg.getArguments());
				ret = ILOperator.OR.expr(allAreKnown, oneIsTrue);
				break;

			case PNOT:
				// NOT doesn't change known-ness, so remove it.
				ret =ILOperator.IS_KNOWN.expr(arg.getUnaryArg());
				break;

			default:
				// Nothing to change here after all
				return visitComposite(oper, null);
			}
			break;
		default:
			// Nope, this must be something else.
			return visitComposite(oper, null);
		}
		
		// We've got a new expression now, it should be a native operator on
		// some biased PLEXIL terms. We might be able to optimize those terms
		// further, so we'll run it through the default "composite" method 
		// which will propagate this visitor to the arguments. 
		return visitComposite(ret, null);
	}


}
