package edu.umn.crisys.plexil.ast.nodebody;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.util.Pair;

public class UpdateBody extends NodeBody {

    private List<Pair<String,Expression>> updates = 
        new ArrayList<Pair<String,Expression>>();
    
    public void addUpdate(String name, Expression value) {
        updates.add(new Pair<String, Expression>(name, value));
    }
    
    public List<Pair<String, Expression>> getUpdates() {
        return updates;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitUpdate(this, param);
    }
}
