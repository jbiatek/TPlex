package edu.umn.crisys.plexil.il;


import java.util.HashSet;
import java.util.Optional;
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
    public NodeUID(NodeUID parent, Optional<String> localName) {
        if (parent == null) {
            construct(localName, null);
        } else {
            construct(localName, parent);
        }
    }
    
    /**
     * Create a UID for a root node. Pass in empty if it's anonymous.
     * @param rootName
     */
    public NodeUID(Optional<String> rootName) {
        construct(rootName, null);
    }
    
    /**
     * The actual constructor for this object. It can't be a real constructor
     * because this() can only be called once as the first thing in another
     * constructor. 
     * @param localName
     * @param parentPath
     * @param siblings
     */
    private void construct(Optional<String> localName, NodeUID parentPath) {
        if (localName.isPresent()) {
        	this.shortName = localName.get();
        } else {
        	if (parentPath == null) {
                // We are an anonymous root node.
                this.shortName = "Root node";
            } else {
                // We are an anonymous node
                this.shortName = "Anonymous node";
            }
        }
        
        if (parentPath == null) {
            uniquePath = this.shortName;
        } else {
            // Let's find a unique path for ourselves keeping in mind our siblings
            Set<String> sibStrs = new HashSet<String>();
            for (NodeUID uid : parentPath.childNames) {
                sibStrs.add(uid.toString());
            }
            uniquePath = NameUtils.getUniqueName(sibStrs, parentPath+"/"+this.shortName);
            // Put our name into the pool of taken names
            parentPath.childNames.add(this);
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
