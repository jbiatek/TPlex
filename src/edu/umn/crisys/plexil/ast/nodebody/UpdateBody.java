package edu.umn.crisys.plexil.ast.nodebody;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.expr.PlexilExpr;
import edu.umn.crisys.util.Pair;

public class UpdateBody extends NodeBody {

    private List<Pair<String,PlexilExpr>> updates = 
        new ArrayList<Pair<String,PlexilExpr>>();
    
    public void addUpdate(String name, PlexilExpr value) {
        updates.add(new Pair<String, PlexilExpr>(name, value));
    }
    
    public List<Pair<String, PlexilExpr>> getUpdates() {
        return updates;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitUpdate(this, param);
    }
}
