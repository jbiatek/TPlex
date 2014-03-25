package edu.umn.crisys.plexil.il.action;

import java.util.LinkedList;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.util.Pair;

public class UpdateAction implements PlexilAction {

    private SimpleVar handle;
    private String shortName;
    private List<Pair<String, ILExpression>> updates = new LinkedList<Pair<String, ILExpression>>();
    
    public UpdateAction(SimpleVar handle, String shortName) {
    	PlexilType.BOOLEAN.typeCheck(handle.getType());
        this.handle = handle;
        this.shortName = shortName;
    }
    
    public SimpleVar getHandle() {
    	return handle;
    }
    
    public String getShortName() {
    	return shortName;
    }
    
    public List<Pair<String, ILExpression>> getUpdates() {
        return updates;
    }
    
    @Override
    public String toString() {
        return "Update : "+updates.toString();
    }
    
    public void addUpdatePair(String name, ILExpression value) {
        updates.add(new Pair<String, ILExpression>(name, value));
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitUpdate(this, param);
	}
    
}
