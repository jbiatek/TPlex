package edu.umn.crisys.plexil.il.action;

import java.util.LinkedList;
import java.util.List;

import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
import edu.umn.crisys.util.Pair;

public class UpdateAction implements PlexilAction {

    private SimpleVar handle;
    private String shortName;
    private List<Pair<String, ILExpr>> updates = new LinkedList<Pair<String, ILExpr>>();
    
    public UpdateAction(SimpleVar handle, String shortName) {
    	ILType.BOOLEAN.typeCheck(handle.getType());
        this.handle = handle;
        this.shortName = shortName;
    }
    
    public SimpleVar getHandle() {
    	return handle;
    }
    
    public String getShortName() {
    	return shortName;
    }
    
    public List<Pair<String, ILExpr>> getUpdates() {
        return updates;
    }
    
    @Override
    public String toString() {
        return "Update : "+updates.toString();
    }
    
    public void addUpdatePair(String name, ILExpr value) {
        updates.add(new Pair<String, ILExpr>(name, value));
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitUpdate(this, param);
	}
    
}
