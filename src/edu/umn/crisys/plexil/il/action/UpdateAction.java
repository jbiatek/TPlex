package edu.umn.crisys.plexil.il.action;

import java.util.LinkedList;
import java.util.List;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.util.Pair;

public class UpdateAction implements PlexilAction {

    private SimpleVar handle;
    private String shortName;
    private List<Pair<String, Expression>> updates = new LinkedList<Pair<String, Expression>>();
    
    public UpdateAction(SimpleVar handle, String shortName) {
    	ExprType.BOOLEAN.typeCheck(handle.getType());
        this.handle = handle;
        this.shortName = shortName;
    }
    
    public SimpleVar getHandle() {
    	return handle;
    }
    
    public String getShortName() {
    	return shortName;
    }
    
    public List<Pair<String, Expression>> getUpdates() {
        return updates;
    }
    
    @Override
    public String toString() {
        return "Update : "+updates.toString();
    }
    
    public void addUpdatePair(String name, Expression value) {
        updates.add(new Pair<String, Expression>(name, value));
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitUpdate(this, param);
	}
    
}
