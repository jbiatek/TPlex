package edu.umn.crisys.plexil.runtime.plx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownBool;
import edu.umn.crisys.plexil.runtime.values.UnknownInt;
import edu.umn.crisys.plexil.runtime.values.UnknownReal;
import edu.umn.crisys.plexil.runtime.values.UnknownString;
import edu.umn.crisys.plexil.test.java.DebugOutputPlexilArray;

public class PlanState {
	
	public static boolean DEBUG = false;
	
	private NodeUID uid;
	
	private List<PlanState> children = new ArrayList<PlanState>();
    private Map<String, Optional<PValue>> vars = new HashMap<>();

    public PlanState(String id) {
        uid = new NodeUID(Optional.of(id));
    }
    
    public PlanState(String id, PlanState parent) {
        uid = new NodeUID(parent.uid, Optional.of(id));
    }
    
    public NodeUID getUID() {
    	return uid;
    }
    
    public Optional<PValue> getVarValue(String varName) {
    	return vars.get(varName);
    }
    
    public List<PlanState> getChildren() {
    	return children;
    }
    
    public void addChild(PlanState child) {
    	children.add(child);
    }
    
    public void addVariable(String name, Optional<PValue> value) {
    	vars.put(name, value);
    }

    
    public void addVariable(String name, PValue value) {
    	vars.put(name, Optional.of(value));
    }
    
    public static List<PlanState> parseLogFile(BufferedReader in) throws IOException {
    	List<PlanState> stateSnapshots = new ArrayList<>();
		PlanState state = parseSingleStep(in);
		while (state != null) {
			if (PlanState.DEBUG) 
				System.out.println("*******************************");
			stateSnapshots.add(state);
			state = parseSingleStep(in);
		}
		return stateSnapshots;

    }
    
    /**
     * Read in one entry from a Plexil Executive log file. The log should
     * consist solely of [PlexilExec:printPlan] entries.
     * 
     * @param in
     * @return the next PlanState from the stream
     * @throws IOException
     */
    public static PlanState parseSingleStep(BufferedReader in) throws IOException {
    	// Try to find the first node name, then pass it off
    	String line = "";
    	while (line != null) {
    		if (line.startsWith("ERROR:") 
    				|| line.startsWith("Assertion failed")) {
    			// Read the rest of this too, let's give them everything
    			List<String> remainingLines = new ArrayList<>();
    			remainingLines.add(line);
    			while ((line = in.readLine()) != null) {
    				remainingLines.add(line);
    			}
    			throw new RuntimeException("This script caused an error in PLEXIL interpreter: \n"+
    					remainingLines.stream().collect(Collectors.joining("\n")));
    		}
    		
    		if (line.endsWith("{")) break;
    		
    		// Anything that isn't an error or a "{" we skip.
    		line = in.readLine();
    		//System.out.println("Trying to start: "+firstName);
    	}
    	
    	// We didn't find one? Nothing to do then.
    	if (line == null) return null;
    	
    	return parseSingleNode(in, line, null);
    }
    
    private static PlanState parseSingleNode(BufferedReader in, String rawName, PlanState parent) throws IOException {
    	PlanState node;
    	String nodeName = rawName.replace("{", "").replaceAll("^\\s*", "");
        if (parent == null) {
            node = new PlanState(nodeName);
        } else {
            node = new PlanState(nodeName, parent);
        }

    	
        String line;
        // Read in a line, strip out all whitespace, and then make sure it 
        // isn't the end of this node. 
        while (! (line = in.readLine().replaceAll("^\\s*", "")).equals("}") ){
    		if (line.endsWith("{")) {
    			// This is the start of a new node, one of our children.
    			PlanState child = parseSingleNode(in, line, node);
    			node.addChild(child);
    		} else if (line.contains("[i]")) {
    			// This means that the variable or condition is "inactive"
    			// inside the PLEXIL executive. It shouldn't matter what this
    			// value is because no one should be reading it. Also, more
    			// importantly, the executive doesn't always give the correct
    			// value for inactive variables, so we need to ignore it.
    			continue;
    		} else if (line.startsWith("State:")) {
    			// State also now has the start time in parentheses after it.
    			String mangledState = extractSimple(line);
    			String correctState = mangledState.replaceFirst(" \\(.*\\)$", "");
    			String startTime = mangledState.replaceFirst(".*\\(", "")
    			                               .replaceFirst("\\)$", "");
    			
    			if (DEBUG)
    				System.out.println(nodeName+" in state "+correctState);
    			node.addVariable(".state", Optional.of(NodeState.valueOf(
    					correctState)));
    			node.addVariable("."+correctState+".START", 
    					Optional.of(RealValue.get(Double.parseDouble(startTime))));
    			
    		} else if (line.startsWith("Outcome:")) {
    			if (DEBUG)
    				System.out.println(nodeName+" outcome is "+extractSimple(line));
    			node.addVariable(".outcome", Optional.of(NodeOutcome.valueOf(
    					extractSimple(line))));
    		} else if (line.startsWith("Command handle:")) {
    			if (line.contains("[i]")) {
    				// This is an inactive variable. The reported value may 
    				// be complete garbage, so ignore it for compliance.
        			node.addVariable(".command_handle", Optional.empty());
    			} else {
    				if (DEBUG)
    					System.out.println(nodeName+" command handle: "+extractValue(line));
    				node.addVariable(".command_handle", 
    						CommandHandleState.valueOf(extractValue(line)));
    			}
    		} else if (line.startsWith("Failure type:")) {
    		    if (DEBUG)
                    System.out.println(nodeName+" failure type: "+extractSimple(line));
                node.addVariable(".failure", 
                		NodeFailureType.valueOf(extractSimple(line)));
    		} else if (line.startsWith("AbortCompleteCondition: ")) {
    			if (DEBUG) {
    				System.out.println(nodeName+" abort ack: "+extractValue(line));
    			}
    			node.addVariable(".command_abort_ack", NativeBool.wrap(
    					extractValue(line).equalsIgnoreCase("1")));
    		}
    		
    		else if (line.matches(".*?: \\(Variable .*")) {
    			// This is possibly a variable.
    			Optional<PValue> value = Optional.empty();
    			String valStr = extractValue(line);
    			
    			if (line.contains("[i]")) {
    				// This value is ignored by the interpreter, and might be garbage.
    				// Leave it as Optional.empty().
    			} else if (line.contains("Boolean")) {
    				if (valStr.equals("1")) {
    					value = Optional.of(BooleanValue.get(true));
    				} else if (valStr.equals("0")) {
    					value = Optional.of(BooleanValue.get(false));
    				} else {
    					value = Optional.of(UnknownBool.get());
    				}
    			} else if (line.contains("Real")) {
    				if (valStr.equals("UNKNOWN")) {
    					value = Optional.of(UnknownReal.get());
    				} else {
    					value = Optional.of(RealValue.get(Double.parseDouble(valStr)));
    				}
    			} else if (line.contains("Integer")) {
    				if (valStr.equals("UNKNOWN")) {
    					value = Optional.of(UnknownInt.get());
    				} else {
    					value = Optional.of(IntegerValue.get(Integer.parseInt(valStr)));
    				}
    			} else if (line.contains("String")) {
    				if (valStr.equals("UNKNOWN")) {
    					value = Optional.of(UnknownString.get());
    				} else {
    					value = Optional.of(StringValue.get(valStr));
    				}
    			} else if (line.endsWith("String")) {
    			    value = Optional.of(new DebugOutputPlexilArray(valStr));
    			} 

    			String varName = line.replaceAll(":.*", "");
    			if (DEBUG)
    				System.out.println("Variable "+nodeName+"."+varName+" is "+value);
    			node.addVariable(varName, value);
    			
    		} else if ( ! line.matches(".*?Condition: .*")){
    			// Conditions get output too, but they're nigh unreadable in the
    			// exec's format. Besides, if a condition is wrong, we'll find
    			// out when a variable goes wrong. 
    			System.out.println("Warning: I don't think this is a variable: "+line);
    		}

        }

    		
		// This is the end of our node. Bye!
        //System.out.println("Exiting node  "+name.replace("{", ""));
		return node;
    }
    
    private static String extractSimple(String line) {
    	return line.replaceFirst(".*: ", "");
    }
    
    private static String extractValue(String line) {
    	return line.replaceAll(".*\\(.*\\]\\(", "")
    				.replaceAll("\\):.*\\).*", "")
    				.replaceAll("\\)\\)$", "");
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
        		// This variable was entirely missing in the oracle's logs.
                if (vars.get(var).get().isKnown() && !var.endsWith(".START") && !var.endsWith(".END")
                		&& !var.equals(".previous_value")) {
                	if (DEBUG) {
                		System.err.println("Warning: No expected value found in oracle for "
                				+uid+"/"+var+". Current value is "+vars.get(var));                		
                	}
                }
            } else if ( ! expected.vars.get(var).isPresent()) {
        		// This variable was inactive in the oracle's logs.
        		// We don't have anything to compare to. 
        	} else if (expected.vars.get(var).get() instanceof DebugOutputPlexilArray) {
            	// Ah, this is a little bit of a special case. This array doesn't
            	// have all the type info we'd like, so we have to use a method
            	// that compares a little more loosely.
            	DebugOutputPlexilArray expArray = (DebugOutputPlexilArray) expected.vars.get(var).get();
            	if ( ! expArray.nonStrictEquals(vars.get(var).get())) {
                    failures.add("Array didn't match in "+uid+": "
                            +var+" expected value was "+expected.vars.get(var)
                            + " but actual value was "+vars.get(var)
                            + " (this array was checked using DebugOutputPlexilArray)");
            	}
            } else if ( ! expected.vars.get(var).equals(vars.get(var))) {
                failures.add("Variable didn't match in "+uid+": "
                        +var+" expected value was "+expected.vars.get(var)
                        + " but actual value was "+vars.get(var));
            }
        }
        
        if ( children.size() != expected.children.size()) {
            failures.add("Number of children for "+uid+"didn't match. Expected had "+
            		expected.children.size()+", but actual had "+
                    children.size() + ". As such, no children were compared.");
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
            } else if ( this.vars.get(var).get().equalTo(expected.vars.get(var).get()).isFalse()) {
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