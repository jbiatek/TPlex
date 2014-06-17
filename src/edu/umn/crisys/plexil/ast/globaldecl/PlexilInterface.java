package edu.umn.crisys.plexil.ast.globaldecl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.java.values.PlexilType;

public class PlexilInterface {

	/**
	 * If false, use the default PLEXIL interface, which is defined as 
	 * having no restrictions. 
	 */
    private boolean isDefined = false;
    private Map<String, PlexilType> varsIn = new HashMap<String, PlexilType>();
    private Map<String, PlexilType> varsInOut = new HashMap<String, PlexilType>();

    public boolean isDefined() {
    	return isDefined;
    }
    
    /**
     * Enable this Node's interface. The default PLEXIL interface allows all
     * variables through, this turns the interface into a whitelist. The only
     * accessible variables will be the ones that are added to it.
     */
    public void restrictInterface() {
        isDefined = true;
    }

    public void addInVariable(String varName, PlexilType t) {
	    isDefined = true;
	    varsIn.put(varName, t);
	}

	public void addInOutVariable(String varName, PlexilType t) {
	    isDefined = true;
	    varsInOut.put(varName, t);
	}

	/**
     * If this interface isDefined(), this is the list of variables that are 
     * allowed through that interface as read only. Trying to assign to any of
     * these should be an error. 
     */
    public Set<String> getInterfaceReadOnlyVars() { 
    	return varsIn.keySet(); 
    }
    
    /**
     * If this interface isDefined(), this is the list of variables that are 
     * allowed through that interface as readable and writeable. 
     */
    public Set<String> getInterfaceWriteableVars() { 
    	return varsInOut.keySet(); 
    }
    
    /**
     * Check this node's Interface to see if the variable is explicitly 
     * removed from the interface. Note that nodes inherit their interfaces
     * from their parents, but this method doesn't check any of that. It's just
     * what this node says about its own interface. 
     * @param varName
     * @return
     */
    public boolean isReadable(String varName) {
        if (isDefined) {
            return getInterfaceReadOnlyVars().contains(varName)
                    || getInterfaceWriteableVars().contains(varName);
        }
        return true;
    }
    /**
     * Check this node's Interface to see if the variable is explicitly 
     * set as read-only (or not visible at all). Note that nodes inherit their 
     * interfaces from their parents, but this method doesn't check any of that.
     * It's just what this node says about its own interface. 
     * @param varName
     * @return
     */

    public boolean isWritable(String varName) {
        if (isDefined) {
            return getInterfaceWriteableVars().contains(varName);
        }
        return true;
    }

    
}
