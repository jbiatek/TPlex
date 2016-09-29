package edu.umn.crisys.plexil.il.expr;

import java.util.List;

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

/**
 * Check an expression for type safety, given the expected type. Throws a 
 * runtime exception if anything does not conform to IL rules or to the
 * type that was expected. 
 * 
 * @author jbiatek
 */
public class ILTypeChecker extends ILExprVisitor<ILType, Void> implements ILActionVisitor<Void, Void>{

	private static final ILTypeChecker SINGLETON = new ILTypeChecker();
	
	public static void typeCheck(Plan p) {
		p.getVariables().forEach(v -> checkTypeIsLegalInIL(v.getType()));
		p.visitAllGuards(SINGLETON, ILType.NATIVE_BOOL);
		p.visitAllActions(SINGLETON, null);
	}
	
	public static void ensureExpressionContainsLegalTypes(ILExpr e) {
		// Recursively check that types are legal. 
		try {
			checkTypeIsLegalInIL(e.getType());
		} catch (Exception ex) {
			throw new RuntimeException("Expression "+e+" has illegal type "+e.getType(), ex);
		}
		e.getArguments().stream()
			.forEach(ILTypeChecker::ensureExpressionContainsLegalTypes);
	}
	
	public static void typeCheck(ILExpr e, ILType expected) {
		e.accept(SINGLETON, expected);
	}
	
	private ILTypeChecker() {}
	
	private static void basicCheck(ILExpr e, ILType expected) {
		checkTypeIsLegalInIL(expected);
		if (e instanceof PValue && ((PValue) e).isUnknown()) {
			return;
		}
		if (e.getType() != expected) {
			// Nope, wrong.
			throw new RuntimeException("Expected type "+expected+" for expression "+e);
		}
	}
	
	public static void checkTypeIsLegalInIL(ILType type) {
		if (type == ILType.NATIVE_BOOL
				|| type == ILType.BOOLEAN
				|| type == ILType.INTEGER
				|| type == ILType.REAL
				|| type == ILType.STRING
				|| type == ILType.STATE
				|| type == ILType.OUTCOME
				|| type == ILType.FAILURE
				|| type == ILType.COMMAND_HANDLE
				|| type == ILType.BOOLEAN_ARRAY
				|| type == ILType.INTEGER_ARRAY
				|| type == ILType.REAL_ARRAY
				|| type == ILType.STRING_ARRAY) {
			return;
		}
		throw new RuntimeException("Type "+type+" is not generally legal in IL!");
	}
	
	@Override
	public Void visit(ILExpr e, ILType expected) {
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
	public Void visit(PValueList<?> list, ILType expected) {
		basicCheck(list, expected);
		// No arguments, but let's check its elements too just to be sure.
		for (PValue element : list) {
			element.accept(this, expected.elementType());
		}
		return null;
	}

	@Override
	public Void visit(ILOperation op, ILType expected) {
		basicCheck(op, expected);
		
		List<ILType> types = op.getOperator().getArgumentTypeList();
		List<ILExpr> args = op.getArguments();
		if (types.size() != args.size()) {
			throw new RuntimeException("Type size and arg size mismatch: "
					+types.size()+" versus "+args.size()
					+" for operation "+op);
		}
		
		for (int i=0; i < types.size(); i++) {
			if (types.get(i) == ILType.UNKNOWN) {
				// This must be a cast. Obviously, this will just have to be
				// checked at runtime. 
				continue;
			}
			args.get(i).accept(this, types.get(i));
		}
		return null;
	}

	@Override
	public Void visit(LookupExpr lookup, ILType expected) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visit(NamedCondition named, ILType expected) {
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
		typeCheck(cmd.getHandle(), ILType.COMMAND_HANDLE);
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
