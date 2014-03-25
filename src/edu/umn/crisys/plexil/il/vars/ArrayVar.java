package edu.umn.crisys.plexil.il.vars;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class ArrayVar extends ILVariable {
	
	private PValueList<?> init;
	private int size;
	
	public ArrayVar(String name, int size, PlexilType type, NodeUID uid) {
		this(name, size, type, uid, new PValueList<PValue>(type));
	}
	
	public ArrayVar(String name, int size, PlexilType type, NodeUID uid, PValueList<?> init) {
		super(name, uid, type);
		this.size = size;
		if ( ! getType().isArrayType()) {
			throw new RuntimeException("Array var must have array type, not "+type);
		}
		this.init = init;
	}
	
	public PValueList<?> getInitialValue() {
		return init;
	}

	public int getMaxSize() {
		return size;
	}

	@Override
	public <P, R> R accept(ILVarVisitor<P, R> visitor, P param) {
		return visitor.visitArray(this, param);
	}

	@Override
	public boolean isAssignable() {
		return true;
	}
	
	
}
