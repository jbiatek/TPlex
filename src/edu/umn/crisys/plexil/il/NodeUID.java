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
    	String desired = getLongerName(node, 0);
    	if (taken.containsKey(desired)) {
    		// Fight! 
    		NodeUID other = taken.remove(desired);
    		findNewUniqueNamesFor(node, other, taken);
    	} else {
    		// No fight necessary.
    		taken.put(desired, node);
    	}
    	// Repeat for all children. 
    	node.childNames.forEach((uid) -> shorten(uid, taken));
	}
    
    private static void findNewUniqueNamesFor(NodeUID one, NodeUID two, Map<String, NodeUID> taken) {
		String oneNewName = one.shortName;
		String twoNewName = two.shortName;
		int numParents = 1;
		
		// First, let's try the first ancestor that differs between them.
		// This is guaranteed to find a difference eventually. 
		while (oneNewName.equals(twoNewName)) {
			oneNewName = getNameWithAncestor(one, numParents);
			twoNewName = getNameWithAncestor(two, numParents);
			numParents++;
		}
		// Before we add them, are there any conflicts?
		if ( ! taken.containsKey(oneNewName) && ! taken.containsKey(twoNewName)) {
			// Nope. We are all done.
			taken.put(oneNewName, one);
			taken.put(twoNewName, two);
			return;
		}
		
		// Well, that didn't work. This time, as we add ancestors, we'll keep
		// the names. That's guaranteed to eventually find a unique name. 
		numParents = 1;
		while (oneNewName.equals(twoNewName) || taken.containsKey(oneNewName)
				|| taken.containsKey(twoNewName)) {
			oneNewName = getLongerName(one, numParents);
			twoNewName = getLongerName(two, numParents);
			numParents++;
		}
		// Found unique names, all done.
		taken.put(oneNewName, one);
		taken.put(twoNewName, two);
    }
    
    private Optional<NodeUID> findAncestor(int numLevels) {
    	if (numLevels == 0) return Optional.of(this);
    	return parent.map(p -> p.findAncestor(numLevels-1))
				.orElse(Optional.empty());
    }
    
    private static String getNameWithAncestor(NodeUID name, int ancestor) {
    	return NameUtils.clean(
    			name.findAncestor(ancestor).get().shortName+"___"+name.shortName
    			);
    }
    
    private static String getLongerName(NodeUID name, int numParents) {
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
