package edu.umn.crisys.plexil.il.expr;

import java.util.ArrayList;
import java.util.List;

/**
 * A superclass for visitors that modify ILExpressions. Each method implemented
 * here either returns the expression unmodified, or if it is a composite, 
 * recurses down the tree and returns the same type of expression with the new
 * arguments, if any. All you have to do is override the methods for the
 * type of expression that you want to change, and everything else should be
 * left untouched. 
 * 
 * @author jbiatek
 *
 * @param <Param>
 */
public abstract class ILExprModifier<Param> extends ILExprVisitor<Param, ILExpr> {

	public ILExpr visitComposite(ILExpr composite, Param param) {
		if (composite.getArguments().isEmpty()) {
			return composite;
		}
		List<ILExpr> modified = new ArrayList<ILExpr>();
		for (ILExpr child : composite.getArguments()) {
			modified.add(child.accept(this, param));
		}
		return composite.getCloneWithArgs(modified);
	}
	
	@Override
	public ILExpr visit(ILExpr e, Param param) {
		return visitComposite(e, param);
	}
}
