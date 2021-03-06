package edu.umn.crisys.plexil.il.expr.vars;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;

public class ArrayVar extends ILVariable {
	
	private PValueList<?> init;
	private int size;
	
	public ArrayVar(String name, int size, ILType type, NodeUID uid) {
		this(name, size, type, uid, new PValueList<PValue>(type));
	}
	
	public ArrayVar(String name, int size, ILType type, NodeUID uid, PValueList<?> init) {
		super(name, uid, type);
		this.size = size;
		if ( ! getType().isArrayType()) {
			throw new RuntimeException("Array var must have array type, not "+type);
		}
		if (init == null) {
			this.init = new PValueList<PValue>(type, size);
		} else {
			this.init = init;
		}
	}
	
	public PValueList<?> getInitialValue() {
		return init;
	}

	public int getMaxSize() {
		return size;
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
