package edu.umn.crisys.plexil.ast.core.nodebody;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.util.Pair;

public class UpdateBody extends NodeBody {

    private List<Pair<String,ASTExpression>> updates = 
        new ArrayList<Pair<String,ASTExpression>>();
    
    public void addUpdate(String name, ASTExpression value) {
        updates.add(new Pair<String, ASTExpression>(name, value));
    }
    
    public List<Pair<String, ASTExpression>> getUpdates() {
        return updates;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitUpdate(this, param);
    }
}
