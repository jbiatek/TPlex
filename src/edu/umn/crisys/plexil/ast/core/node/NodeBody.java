package edu.umn.crisys.plexil.ast.core.node;

import edu.umn.crisys.plexil.ast.core.visitor.NodeBodyVisitor;

/**
 * Base for node body. This one is an empty node, so no other information is 
 * needed. Other types will extend this and add their information.
 */
public class NodeBody {

    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        if (this.getClass() != NodeBody.class) {
            throw new RuntimeException("Jason forgot to override accept() for class "+this.getClass());
        }
        return visitor.visitEmpty(this, param);
    }
    
}
