package edu.umn.crisys.plexil.expr.il;

import java.util.List;

import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.NamedCondition;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AlsoRunNodesAction;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.CompositeAction;
import edu.umn.crisys.plexil.il.action.EndMacroStep;
import edu.umn.crisys.plexil.il.action.ILActionVisitor;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

/**
 * Check an expression for type safety, given the expected type. Throws a 
 * runtime exception if anything does not conform to IL rules or to the
 * type that was expected. 
 * 
 * @author jbiatek
 */
public class ILTypeChecker extends ILExprVisitor<ExprType, Void> implements ILActionVisitor<Void, Void>{

	private static final ILTypeChecker SINGLETON = new ILTypeChecker();
	
	public static void typeCheck(Plan p) {
		p.getVariables().forEach(v -> checkTypeIsLegalInIL(v.getType()));
		p.visitAllGuards(SINGLETON, ExprType.NATIVE_BOOL);
		p.visitAllActions(SINGLETON, null);
	}
	
	public static void typeCheck(Expression e, ExprType expected) {
		e.accept(SINGLETON, expected);
	}
	
	private ILTypeChecker() {}
	
	private static void basicCheck(Expression e, ExprType expected) {
		checkTypeIsLegalInIL(expected);
		if (e.getType() != expected) {
			// Nope, wrong.
			throw new RuntimeException("Expected type "+expected+" for expression "+e);
		}
	}
	
	public static void checkTypeIsLegalInIL(ExprType type) {
		if (type == ExprType.NATIVE_BOOL
				|| type == ExprType.BOOLEAN
				|| type == ExprType.INTEGER
				|| type == ExprType.REAL
				|| type == ExprType.STRING
				|| type == ExprType.STATE
				|| type == ExprType.OUTCOME
				|| type == ExprType.FAILURE
				|| type == ExprType.COMMAND_HANDLE
				|| type == ExprType.BOOLEAN_ARRAY
				|| type == ExprType.INTEGER_ARRAY
				|| type == ExprType.REAL_ARRAY
				|| type == ExprType.STRING_ARRAY) {
			return;
		}
		throw new RuntimeException("Type "+type+" is not generally legal in IL!");
	}
	
	@Override
	public Void visit(Expression e, ExprType expected) {
		basicCheck(e, expected);
		if (e.getArguments().isEmpty()) {
			// Looks good, nothing more to check.
			return null;
		} else {
			// We don't know what their arguments are supposed to be, that
			// should be handled in a more specific method!
			return super.visit(e, expected);
		}
	}
	
	@Override
	public Void visit(UnknownValue unk, ExprType expected) {
		// UnknownValue represents a few different types.
		if (expected == ExprType.BOOLEAN 
				|| expected == ExprType.INTEGER
				|| expected == ExprType.REAL
				|| expected == ExprType.STRING) {
			return null;
		}
		throw new RuntimeException("Type "+expected+" is not acceptable for UnknownValue");
	}

	@Override
	public Void visit(PValueList<?> list, ExprType expected) {
		basicCheck(list, expected);
		// No arguments, but let's check its elements too just to be sure.
		for (PValue element : list) {
			element.accept(this, expected.elementType());
		}
		return null;
	}

	@Override
	public Void visit(ILOperation op, ExprType expected) {
		basicCheck(op, expected);
		
		List<ExprType> types = op.getOperator().getArgumentTypeList();
		List<Expression> args = op.getArguments();
		if (types.size() != args.size()) {
			throw new RuntimeException("Type size and arg size mismatch: "
					+types.size()+" versus "+args.size()
					+" for operation "+op);
		}
		
		for (int i=0; i < types.size(); i++) {
			if (types.get(i) == ExprType.UNKNOWN) {
				// This must be a cast. Obviously, this will just have to be
				// checked at runtime. 
				continue;
			}
			args.get(i).accept(this, types.get(i));
		}
		return null;
	}

	@Override
	public Void visit(LookupNowExpr lookup, ExprType expected) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visit(LookupOnChangeExpr lookup, ExprType expected) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visit(NamedCondition named, ExprType expected) {
		// This is just a wrapper, treat it as if it isn't there
		return named.getUnaryArg().accept(this, expected);
	}

	@Override
	public Void visitAlsoRunNodes(AlsoRunNodesAction run, Void param) {
		// No expressions to check
		return null;
	}

	@Override
	public Void visitAssign(AssignAction assign, Void param) {
		// Left hand side of an assignment should have a legal type
		checkTypeIsLegalInIL(assign.getLHS().getType());
		// It also needs to be assignable, of course.
		if ( ! assign.getLHS().isAssignable()) {
			throw new RuntimeException("The expression "+assign.getLHS()
					+ " is not assignable (Tried to assign "+assign.getRHS());
		}
		
		// The right side is expected to also have that type. 
		typeCheck(assign.getRHS(), assign.getLHS().getType());
		
		return null;
	}

	@Override
	public Void visitCommand(CommandAction cmd, Void param) {
		typeCheck(cmd.getHandle(), ExprType.COMMAND_HANDLE);
		cmd.getPossibleLeftHandSide().ifPresent(
				lhs -> checkTypeIsLegalInIL(lhs.getType()));
		return null;
	}

	@Override
	public Void visitEndMacroStep(EndMacroStep end, Void param) {
		// No expressions to check
		return null;
	}

	@Override
	public Void visitRunLibraryNode(RunLibraryNodeAction lib, Void param) {
		// No expressions to check
		return null;
	}

	@Override
	public Void visitComposite(CompositeAction composite, Void param) {
		for (PlexilAction action : composite.getActions()) {
			action.accept(this, null);
		}
		return null;
	}

	@Override
	public Void visitUpdate(UpdateAction update, Void param) {
		// The expressions in an update don't have an expected type
		return null;
	}

}
