package edu.umn.crisys.plexil.il.expr.nativebool;

public interface NativeExprVisitor<Param, Return> {
	
	public Return visitNativeOperation(NativeOperation op, Param param);
	public Return visitPlexilExprToNative(PlexilExprToNative pen, Param param);
	public Return visitNativeConstant(NativeConstant c, Param param);
}
