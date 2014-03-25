package edu.umn.crisys.plexil.test.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.RealValue;
import edu.umn.crisys.plexil.java.values.StringValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;

public class PlanState {
	
	public static boolean DEBUG = false;
	
	private NodeUID uid;
	
	private List<PlanState> children = new ArrayList<PlanState>();
    private Map<String, PValue> vars = new HashMap<String, PValue>();

    public PlanState(String id) {
        uid = new NodeUID(id);
    }
    
    public PlanState(String id, PlanState parent) {
        uid = new NodeUID(id, parent.uid, parent.getChildUIDs());
    }
    
    private Set<NodeUID> getChildUIDs() {
        Set<NodeUID> childUIDs = new HashSet<NodeUID>();
        
        for (PlanState child : children) {
            childUIDs.add(child.uid);
        }
        return childUIDs;
    }
    
    public void addChild(PlanState child) {
    	children.add(child);
    }
    
    public void addVariable(String name, PValue value) {
    	vars.put(name, value);
    }

    /**
     * Read in one entry from a Plexil Executive log file. The log should
     * consist solely of [PlexilExec:printPlan] entries.
     * 
     * @param in
     * @return the next PlanState from the stream
     * @throws IOException
     */
    public static PlanState parseLogFile(BufferedReader in) throws IOException {
    	// Find the first node name, then pass it off
    	String firstName = "";
    	while (firstName != null && !firstName.endsWith("{")) {
    		firstName = in.readLine();
    		//System.out.println("Trying to start: "+firstName);
    	}
    	
    	if (firstName == null) return null;
    	
    	return parseLogFile(in, firstName, null);
    }
    
    private static PlanState parseLogFile(BufferedReader in, String rawName, PlanState parent) throws IOException {
    	PlanState node;
    	String nodeName = rawName.replace("{", "").replaceAll("^\\s*", "");
        if (parent == null) {
            node = new PlanState(nodeName);
        } else {
            node = new PlanState(nodeName, parent);
        }

    	
        //System.out.println("Entering node "+name.replace("{", ""));
    	
        String line = in.readLine();
		// Delete leading whitespace
		line = line.replaceAll("^\\s*", "");
    	
        while (!line.equals("}")){
    		if (line.endsWith("{")) {
    			// This is the start of a new node, one of our children.
    			PlanState child = parseLogFile(in, line, node);
    			node.addChild(child);
    		} else if (line.startsWith("State:")) {
    			if (DEBUG)
    				System.out.println(nodeName+" in state "+extractValue(line));
    			node.addVariable(".state", NodeState.valueOf(
    					extractValue(line)));
    			
    		} else if (line.startsWith("Outcome:")) {
    			if (DEBUG)
    				System.out.println(nodeName+" outcome is "+extractValue(line));
    			node.addVariable(".outcome", NodeOutcome.valueOf(
    					extractValue(line)));
    		} else if (line.startsWith("Command handle:")) {
    			if (DEBUG)
    				System.out.println(nodeName+" command handle: "+extractValue(line));
    			node.addVariable(".command_handle", 
    					CommandHandleState.valueOf(extractValue(line)));
    		} else if (line.startsWith("Failure type:")) {
    		    if (DEBUG)
                    System.out.println(nodeName+" failure type: "+extractValue(line));
                node.addVariable(".failure", 
                        NodeFailureType.valueOf(extractValue(line)));
    		}
    		
    		else {
    			// This is possibly a variable.
    			PValue value = null;
    			String valStr = extractValue(line);
    			if (line.endsWith("boolean)")) {
    				if (valStr.equals("1")) {
    					value = BooleanValue.get(true);
    				} else if (valStr.equals("0")) {
    					value = BooleanValue.get(false);
    				} else {
    					value = UnknownValue.get();
    				}
    			} else if (line.endsWith("real)")) {
    				if (valStr.equals("UNKNOWN")) {
    					value = UnknownValue.get();
    				} else {
    					value = RealValue.get(Double.parseDouble(valStr));
    				}
    			} else if (line.endsWith("int)")) {
    				if (valStr.equals("UNKNOWN")) {
    					value = UnknownValue.get();
    				} else {
    					value = IntegerValue.get(Integer.parseInt(valStr));
    				}
    			} else if (line.endsWith("string)")) {
    				if (valStr.equals("UNKNOWN")) {
    					value = UnknownValue.get();
    				} else {
    					value = StringValue.get(valStr);
    				}
    			} else if (line.endsWith("array)")) {
    			    value = new DebugOutputPlexilArray(valStr);
    			} else {
    			    System.out.println("Warning: I don't think this is a variable: "+line);
    			}
    			
    			String varName = line.replaceAll(":.*", "");
    			if (DEBUG)
    				System.out.println("Variable "+nodeName+"."+varName+" is "+value);
    			node.addVariable(varName, value);
    			
    		}

	        line = in.readLine();
			// Delete leading whitespace
			line = line.replaceAll("^\\s*", "");
    		
        }

    		
		// This is the end of our node. Bye!
        //System.out.println("Exiting node  "+name.replace("{", ""));
		return node;
    }
    
    private static String extractValue(String line) {
    	return line.replaceAll(".*\\(.*\\]\\(", "")
    				.replaceAll("\\):.*\\).*", "");
    }

    /**
     * Compile a list of differences between this PlanState and the given one.
     * Since Plexil's debug logs are not great, we have to make a few 
     * compromises:
     * <ul>
     *      <li>They have a bunch of variables that aren't really variables. 
     *      Conditions, aliases for variables that are actually elsewhere, etc.
     *      So we're only concerned with comparing values we have with values
     *      they have, and not vice versa.</li>
     *      <li>They also don't always print everything. So we're just going to
     *      have to assume that we have it right and hope that they print it
     *      later so that we can check ourselves.</li>
     * </ul>
     * @param expected
     * @return a list of differences between the two PlanStates.
     */
    public List<String> testAgainst(PlanState expected) {
        List<String> failures = new ArrayList<String>();
        
        // First, check on the UID. They should match up.
        if ( ! uid.getShortName().equals(expected.uid.getShortName())) {
            failures.add("UIDs didn't match. I have "+uid.getShortName()
                    +", they have "+uid.getShortName());
        }
        
        for (String var : vars.keySet()) {
            if (expected.vars.get(var) == null) {
                if (vars.get(var).isKnown() && !var.endsWith(".START") && !var.endsWith(".END")) {
                    System.err.println("Warning: Oracle doesn't say what "
                            +uid+"/"+var+" is. I think it's "+vars.get(var));
                }
            } else if ( ! expected.vars.get(var).equals(vars.get(var))) {
                // The expected one needs to be the one checking for equals()
                // because the oracle arrays are actually TypelessPlexilArrays 
                // that know things like 0 == false and 1 == true.
                failures.add("Variable didn't match in "+uid+": "
                        +var+" should be "+expected.vars.get(var)
                        + " but I have "+vars.get(var));
            }
        }
        
        if ( children.size() != expected.children.size()) {
            failures.add("Number of children didn't match. I have "+
                    children.size() + ", but they have "+
                    expected.children.size()+", so I stopped checking here.");
            return failures;
        }
        
        // Let the children go, and then report back what we all found
        for (int i = 0; i < children.size(); i++) {
            failures.addAll(children.get(i).testAgainst(expected.children.get(i)));
        }
        
        return failures;
    }
    
    public List<String> testStrictlyAgainst(PlanState expected) {
        List<String> failures = new ArrayList<String>();
        
        if (! this.uid.equals(expected.uid)) {
            failures.add("UIDs do not match: "+this.uid+", expected "+expected.uid);
        }
        boolean mentionExpectedVars = false;
        for (String var : vars.keySet()) {
            if ( ! expected.vars.containsKey(var)) {
                mentionExpectedVars = true;
                failures.add("No expected value for "+var);
            } else if ( this.vars.get(var).equalTo(expected.vars.get(var)).isFalse()) {
                failures.add("Variable "+var+" does not match: "+this.vars.get(var)+", expected "+expected.vars.get(var));
            }
        }
        if (mentionExpectedVars) {
            String msg = "Expected variables include: ";
            for (String var : expected.vars.keySet()) {
                msg += var+", ";
            }
            failures.add(msg);
        }
        if ( children.size() != expected.children.size()) {
            String msg = "Number of children didn't match. I have "+
            children.size() + ", but they have "+
            expected.children.size()+", so I stopped checking here. I am "+uid
            +", my children are ";
            for (PlanState child : children) {
                msg += child.uid + ", ";
            }
            msg += " and the expected children were ";
            for (PlanState otherChild : expected.children) {
                msg += otherChild.uid + ", ";
            }
            
            failures.add(msg);
            
            return failures;
        }
        
        if (failures.size() > 0) {
            failures.add("Errors were found, not descending into children.");
            return failures;
        }
        
        // Let the children go, and then report back what we all found
        for (int i = 0; i < children.size(); i++) {
            failures.addAll(children.get(i).testStrictlyAgainst(expected.children.get(i)));
        }
        
        return failures;
    }

    
}