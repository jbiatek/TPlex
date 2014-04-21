package edu.umn.crisys.plexil.java.plx;

import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class SimplePValue<T> extends SimpleCurrentNext<T> {

	private PlexilType type;
	
	public SimplePValue(T initial, PlexilType type) {
		super(initial);
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	public void setNext(PValue v) {
		super.setNext((T) (v.castTo(type)));
	}

}