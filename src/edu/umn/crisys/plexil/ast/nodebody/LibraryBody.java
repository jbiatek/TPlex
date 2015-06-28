package edu.umn.crisys.plexil.ast.nodebody;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.expr.Expression;

public class LibraryBody extends NodeBody {

    private String nodeId;
    
    // Variable aliases 
    private Map<String, Expression> aliases = 
        new HashMap<String, Expression>();

    
    // private String renameNodeId; // Never seen this before

    public LibraryBody(String nodeId) {
        this.nodeId = nodeId;
    }
    public String getNodeId() {
        return nodeId;
    }

    public boolean containsAlias(String name) {
        return aliases.containsKey(name); 
    }

    public Expression getAlias(String name) {
        return aliases.get(name); 
    }

    public void addAlias(String name, Expression expr) { 
        aliases.put(name, expr); 
    }
    
    public Set<String> getAliases() {
        return aliases.keySet();
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitLibrary(this, param);
    }

}
