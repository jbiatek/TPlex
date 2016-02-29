package edu.umn.crisys.plexil.runtime.plx;

import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class SimplePValue<T> extends SimpleCurrentNext<T> {

	private ILType type;
	
	public SimplePValue(T initial, ILType type) {
		super(initial);
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	public void setNext(PValue v) {
		super.setNext((T) (v.castTo(type)));
	}

}
