package edu.umn.crisys.plexil.translator.il;


import java.util.HashSet;
import java.util.Set;

import edu.umn.crisys.plexil.NameUtils;

public class NodeUID {
    
    private String shortName;
    private String uniquePath;
    private Set<NodeUID> childNames = new HashSet<NodeUID>();
    
    /**
     * Create a UID for this Node. Guaranteed unique within this plan.
     * @param node
     */
    public NodeUID(NodeUID parent, String localName) {
        if (parent == null) {
            construct(localName, null, null);
        } else {
            construct(localName, parent, parent.childNames);
        }
    }
    
    /**
     * Create a UID for a Node with the given parent and siblings. 
     * @param shortName The "Plexil ID" of this node, or null if it is 
     * anonymous.
     * @param parentId The parent's already created NodeUID.
     * @param siblings All the NodeUIDs that have already been made for siblings
     * of this node. This UID will be added to it.
     */
    public NodeUID(String shortName, NodeUID parentId, Set<NodeUID> siblings) {
        construct(shortName, parentId, siblings);
    }
    
    /**
     * Create a UID for a root node. Pass in null if it's anonymous.
     * @param rootName
     */
    public NodeUID(String rootName) {
        construct(rootName, null, null);
    }
    
    /**
     * The actual constructor for this object. It can't be a real constructor
     * because this() can only be called once as the first thing in another
     * constructor. 
     * @param localName
     * @param parentPath
     * @param siblings
     */
    private void construct(String localName, NodeUID parentPath, Set<NodeUID> siblings) {
        if (localName == null) {
            if (parentPath == null) {
                // We are an anonymous root node.
                localName = "Root node";
            } else {
                // We are an anonymous node
                localName = "Anonymous node";
            }
        } else {
            this.shortName = localName;
        }
        
        if (parentPath == null) {
            uniquePath = this.shortName;
        } else {
            // Let's find a unique path for ourselves keeping in mind our siblings
            Set<String> sibStrs = new HashSet<String>();
            for (NodeUID uid : siblings) {
                sibStrs.add(uid.toString());
            }
            uniquePath = NameUtils.getUniqueName(sibStrs, parentPath+"/"+this.shortName);
            // Put our name into the pool of taken names
            siblings.add(this);
        }
        
    }
    
    @Override
    public String toString() {
        return uniquePath;
    }
    
    @Override 
    public boolean equals(Object o) {
        if (o instanceof NodeUID) {
            NodeUID other = (NodeUID) o;
            return uniquePath.equals(other.uniquePath);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return uniquePath.hashCode();
    }
    
    public String toCleanString() {
        return NameUtils.clean(toString());
    }
    
    public String getShortName() {
        return shortName;
    }
}
