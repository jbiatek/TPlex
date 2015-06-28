package edu.umn.crisys.plexil.expr.il.vars;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;

public class SimpleVar extends ILVariable {
	
	private Expression init;

	public SimpleVar(String name, NodeUID uid, PlexilType type) {
		this(name, uid, type, (Expression) null);
	}
	
	public SimpleVar(String name, NodeUID uid, PlexilType type, Expression init) {
		super(name, uid, type);
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
	}
	
	public Expression getInitialValue() {
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
