package edu.umn.crisys.plexil.ast.nodebody;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.util.Pair;

public class UpdateBody extends NodeBody {

    private List<Pair<String,ILExpr>> updates = 
        new ArrayList<Pair<String,ILExpr>>();
    
    public void addUpdate(String name, ILExpr value) {
        updates.add(new Pair<String, ILExpr>(name, value));
    }
    
    public List<Pair<String, ILExpr>> getUpdates() {
        return updates;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitUpdate(this, param);
    }
}
