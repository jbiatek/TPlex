package edu.umn.crisys.plexil;

import java.util.Set;

/**
 * @author Whalen
 *
 * static functions for naming.
 */


public class NameUtils {
    
    /**
     * @param nameSet Names that are already taken.
     * @param desiredName A good name for this thing.
     * @return a unique name that has been added to nameSet.
     */
	public static String getUniqueName(Set<String> nameSet, String desiredName) {
		String desiredClean = clean(desiredName);
		
		if (nameSet.contains(desiredClean)) {
			int num = 1;
			while (nameSet.contains(desiredClean+num)) {
				num++;
			}
			nameSet.add(desiredClean+num);
			return desiredClean+num;
		} else {
			nameSet.add(desiredClean);
			return desiredClean;
		}
	}
	
	/**
	 * Sanitize a string such that it can be used for Java class, variable,
	 * etc. names. This includes:
	 * <li> Sticking the word "Plan" in front of leading digits
	 * <li> Turning / and . into __
	 * <li> Turning - into _
	 * <li> Removing anything that isn't a letter, number, or underscore 
	 * @param str
	 * @return the cleaned string
	 */
	public static String clean(String str) {
	    if (str.matches("^[0-9].*")) {
	        str = "Plan"+str;
	    }
		return str.replaceAll("[/.]", "__")
		          .replaceAll("-", "_")
		          .replaceAll("[^A-Za-z0-9_]", "");
	}


}
