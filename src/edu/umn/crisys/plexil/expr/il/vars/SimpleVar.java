package edu.umn.crisys.plexil.expr.il.vars;

import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILType;
import edu.umn.crisys.plexil.expr.il.ILTypeChecker;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;

public class SimpleVar extends ILVariable {
	
	private ILExpr init;

	public SimpleVar(String name, NodeUID uid, ILType type) {
		this(name, uid, type, (ILExpr) null);
	}
	
	public SimpleVar(String name, NodeUID uid, ILType type, ILExpr init) {
		super(name, uid, type);
		ILTypeChecker.checkTypeIsLegalInIL(type);
		if (init == null) {
			if (type.isArrayType()) {
				// Just use an empty array
				init = new PValueList<PValue>(type);
			} else {
				this.init = type.getUnknown();
			}
		} else {
			this.init = init;
		}
		ILTypeChecker.typeCheck(this.init, type);
	}
	
	public ILExpr getInitialValue() {
		return init;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public boolean isAssignable() {
		return true;
	}
	
}
