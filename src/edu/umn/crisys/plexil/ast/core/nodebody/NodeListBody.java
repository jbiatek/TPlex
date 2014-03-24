package edu.umn.crisys.plexil.ast.core.nodebody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.Node;

public class NodeListBody extends NodeBody implements Iterable<Node>{

    private List<Node> children = new ArrayList<Node>();
    public List<Node> getChildList() { return children; }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitNodeList(this, param);
    }

    @Override
    public Iterator<Node> iterator() {
        return children.iterator();
    }
}
