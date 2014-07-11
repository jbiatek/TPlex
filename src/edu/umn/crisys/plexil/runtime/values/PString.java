package edu.umn.crisys.plexil.runtime.values;

public interface PString extends PValue {

	public abstract String getString();

	public abstract PString concat(PString o);

}