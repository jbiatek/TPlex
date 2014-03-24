package edu.umn.crisys.plexil.il.vars;

public interface ILVarVisitor<Param, Return> {
	
	public Return visitSimple(SimpleVar var, Param param);
	public Return visitArray(ArrayVar array, Param param);
	public Return visitLibrary(LibraryVar lib, Param param);

}
