package edu.umn.crisys.plexil.il;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.umn.crisys.util.NameUtils;

public class NodeUID {
    
	private Optional<NodeUID> parent;
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
    	parent = Optional.ofNullable(parentPath);
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
    
    public void shortenUniqueNames() {
    	if (parent.isPresent()) {
    		throw new RuntimeException("Call this on the root UID only!");
    	}
    	Map<String,NodeUID> takenNames = new HashMap<>();
    	NodeUID.shorten(this, takenNames);
    	// Set each key as the unique path.
    	takenNames.entrySet().forEach(e -> e.getValue().uniquePath = e.getKey());
    }
    
    private static void shorten(NodeUID node, Map<String, NodeUID> taken) {
    	String desired = getDesired(node, 0);
    	if (taken.containsKey(desired)) {
    		// Fight! 
    		NodeUID other = taken.remove(desired);
    		String myNewName = desired;
    		String otherNewName = desired;
    		int numParents = 1;
    		while (myNewName.equals(otherNewName)) {
    			myNewName = getDesired(node, numParents);
    			otherNewName = getDesired(other, numParents);
    			numParents++;
    		}
    		// Eventually they must be different. 
    		taken.put(myNewName, node);
    		taken.put(otherNewName, other);
    	} else {
    		// No fight necessary.
    		taken.put(desired, node);
    	}
    	// Repeat for all children. 
    	node.childNames.forEach((uid) -> shorten(uid, taken));
	}
    
    private static String getDesired(NodeUID name, int numParents) {
    	String unclean = name.shortName;
    	Optional<NodeUID> parent = name.parent;
    	
    	for (int i = 0; i < numParents; i++) {
    		if (parent.isPresent()) {
    			unclean = parent.get().shortName + "/" + unclean;
    			parent = parent.get().parent;
    		} else {
    			break;
    		}
    	}
    	return NameUtils.clean(unclean);
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
