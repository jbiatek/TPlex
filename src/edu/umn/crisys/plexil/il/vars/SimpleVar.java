package edu.umn.crisys.plexil.il.vars;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class SimpleVar extends ILVariable {
	
	private ILExpression init;
	private boolean ignoreMe = false;

	public SimpleVar(String name, NodeUID uid, PlexilType type) {
		this(name, uid, type, (ILExpression) null);
	}
	
	public SimpleVar(String name, NodeUID uid, PlexilType type, ILExpression init) {
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
	
	public ILExpression getInitialValue() {
		return init;
	}

	@Override
	public <P, R> R accept(ILVarVisitor<P, R> visitor, P param) {
		return visitor.visitSimple(this, param);
	}

	@Override
	public boolean isAssignable() {
		return true;
	}
	
	public boolean shouldBeIgnored() {
		return ignoreMe;
	}

	public void markAsUnused() {
		ignoreMe = true;
	}
	
}
