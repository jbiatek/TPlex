package edu.umn.crisys.plexil.runtime.values;

public interface PInteger extends PValue {
	
	public abstract PReal castToReal();
	public abstract int getIntValue();

	public abstract PBoolean gt(PInteger o);
	public abstract PBoolean ge(PInteger o);
	public abstract PBoolean lt(PInteger o);
	public abstract PBoolean le(PInteger o);
	public abstract PInteger add(PInteger o);
	public abstract PInteger sub(PInteger o);
	public abstract PInteger mul(PInteger o);
	public abstract PInteger div(PInteger o);
	public abstract PInteger mod(PInteger o);
	public abstract PInteger max(PInteger o);
	public abstract PInteger min(PInteger o);
	public abstract PInteger abs();
	
}
