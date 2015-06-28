package edu.umn.crisys.plexil.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.ast.Node;
import edu.umn.crisys.plexil.ast.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBodyVisitor;
import edu.umn.crisys.plexil.ast.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.nodebody.UpdateBody;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.expr.ast.ASTExprVisitor;
import edu.umn.crisys.plexil.expr.ast.DefaultEndExpr;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.expr.common.Operation;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class TypeAnalyzer extends ASTExprVisitor<PlexilType, Void> implements NodeBodyVisitor<Node, Void>{
	
    private Map<String, PlexilType> lookups = new HashMap<String, PlexilType>();
    private Map<String, List<PValue>> lookupValuesOfInterest = new HashMap<String, List<PValue>>();
    private Map<String, PlexilType> commands = new HashMap<String, PlexilType>();
    

	public void printAnalysis() {
		System.out.println("    Lookups:");
		for (String key : lookups.keySet()) {
			System.out.println("      "+key+" returns "+lookups.get(key));
			if (lookupValuesOfInterest.containsKey(key) && ! lookupValuesOfInterest.get(key).isEmpty()) {
				System.out.println("        Possible values: ");
				for (PValue v : lookupValuesOfInterest.get(key)) {
					System.out.println("          "+v);
				}
			}
			
		}
		System.out.println("    Commands:");
		for (String key : commands.keySet()) {
			String type = commands.get(key) == null ? "nothing" : commands.get(key).toString();
			System.out.println("      "+key+" returns "+type);
		}
		
		
		System.out.println();

	}

    
    public void checkNode(Node root) {
        // Check all the conditions
        root.getStartCondition().accept(this, PlexilType.BOOLEAN);
        root.getSkipCondition().accept(this, PlexilType.BOOLEAN);
        root.getPreCondition().accept(this, PlexilType.BOOLEAN);
        root.getInvariantCondition().accept(this, PlexilType.BOOLEAN);
        root.getRepeatCondition().accept(this, PlexilType.BOOLEAN);
        root.getPostCondition().accept(this, PlexilType.BOOLEAN);
        root.getEndCondition().accept(this, PlexilType.BOOLEAN);
        root.getExitCondition().accept(this, PlexilType.BOOLEAN);
        // And the body.
        root.getNodeBody().accept(this, null);
        
    }
    
    public Map<String, PlexilType> getLookupTypes() {
    	return lookups;
    }
    
    public Map<String, List<PValue>> getPossibleLookupConstants() {
    	return lookupValuesOfInterest;
    }
    
    public Map<String, PlexilType> getCommandTypes() {
    	return commands;
    }

    private void customAdd(Map<String, PlexilType> map, Expression key, PlexilType value) {
		customAdd(lookups, "("+key.toString()+") (expression)", value);
    }
    
    private void customAdd(Map<String, PlexilType> map, String key, PlexilType value) {
        if (map.containsKey(key)) {
        	// Check the existing value and make sure it makes sense.
            if (map.get(key) == value) {
            	// Okay, we already knew that.
                return;
            } else if (map.get(key) == null) {
            	// Ah, we had seen this before, but we didn't know it had a type.
                map.put(key, value);
                return;
            } else if (map.get(key) == PlexilType.INTEGER &&
                    (value == PlexilType.REAL || value == PlexilType.NUMERIC)) {
            	// Hmm, we thought this was an integer, but I guess we can't be
            	// that specific.
                map.put(key, PlexilType.NUMERIC);
                return;
            } else if (map.get(key) == PlexilType.REAL &&
                    (value == PlexilType.INTEGER || value == PlexilType.NUMERIC)) {
            	// Hmm, we thought it was a real, but I guess we can't be that specific.
                map.put(key, PlexilType.NUMERIC);
                return;
            } else if (map.get(key) == PlexilType.NUMERIC &&
                    (value == PlexilType.REAL || value == PlexilType.INTEGER)) {
            	// Yeah, we already knew it was numeric.
                return;
            } else {
            	// They didn't match, and it wasn't numeric? Bad times.
            	System.err.println("***Found incompatible types for "+key+":");
            	System.err.println("***Before, I had "+map.get(key)+", but now I'm seeing "+value);
            	return;
            }
        } else {
        	// This is a new value.
            map.put(key, value);
        }
    }
    


	@Override
	public Void visitLookupNow(LookupNowExpr lookup, PlexilType currentType) {
		Expression name = lookup.getLookupName();
		if ( ! (name instanceof StringValue)) {
			customAdd(lookups, name, currentType);
		} else {
			customAdd(lookups, ((StringValue)name).getString(), currentType);
		}
		return null;
	}


	@Override
	public Void visitLookupOnChange(LookupOnChangeExpr lookup, PlexilType currentType) {
		Expression name = lookup.getLookupName();
		if ( ! (name instanceof StringValue)) {
			customAdd(lookups, name, currentType);
		} else {
			customAdd(lookups, ((StringValue)name).getString(), currentType);
		}
		return null;
	}
	
	@Override
	public Void visitCommand(CommandBody cmd, Node n) {
		PlexilType typeToSet = null;
		cmd.getVarToAssign().ifPresent(
				(varToAssign) -> resolveVariableType(varToAssign, n));
		// Even if it's not there, we want to set it. Null means that it's a command
		// that doesn't appear to return anything.
		
        // Get command name
		Expression name = cmd.getCommandName();
		if ( ! (name instanceof StringValue)) {
			// Don't try to extract string
			customAdd(commands, name, typeToSet);
		}  else {
			String realName = ((StringValue)name).getString();
			customAdd(commands, realName, typeToSet);
		}
		return null;
	}
	
	private static PlexilType resolveVariableType(Expression e, Node n) {
		if (e instanceof ArrayIndexExpr) {
			// We can get the array type!
			PlexilType arrayType = resolveVariableType(((ArrayIndexExpr) e).getArray(), n);
			if (arrayType.isArrayType()) {
				return arrayType.elementType();
			} else {
				// Mmm, didn't work. I guess we're stuck.
				return PlexilType.UNKNOWN;
			}
		} else if (e instanceof UnresolvedVariableExpr) {
			return resolveVariableType(((UnresolvedVariableExpr) e).getName(), n);
		} else {
			System.err.println("Add LHS type to resolveVariableType() : "+e+" "+e.getClass());
			return PlexilType.UNKNOWN;
		}

	}

	private static PlexilType resolveVariableType(String name, Node n) {
		if (n.containsVar(name)) {
			return n.getVariableInfo(name).getType();
		} else if (n.getParent().isPresent()) {
			return resolveVariableType(name, n.getParent().get());
		} else {
			return PlexilType.UNKNOWN;
		}
	}

	@Override
	public Void visitOperation(Operation op, PlexilType currentType) {
		// Operators contain information about their argument types.
		PlexilType argType = op.getExpectedArgumentType();
		
		// If it's a NodeRef, there's no point in continuing.
		if (argType == PlexilType.NODEREF) return null;

        // It's possible that we can be more specific than "numeric":
        if (argType == PlexilType.NUMERIC) {
            // Do we know if this is gonna be a real or integer already?
            if (currentType == PlexilType.INTEGER ||
                    currentType == PlexilType.REAL) {
                argType = currentType;
            } 
        }
        // Move down, and pass along the argument type.
        String lookup = null;
        Set<PValue> constants = new HashSet<PValue>();
        for (Expression arg : op.getArguments()) {
        	arg.accept(this, argType);
        	
        	if (arg instanceof LookupNowExpr) {
        		lookup = ((LookupNowExpr) arg).getLookupName().asString();
        	} else if (arg instanceof LookupOnChangeExpr) {
        		lookup = ((LookupOnChangeExpr) arg).getLookupName().asString();
        	} else if (arg instanceof PValue) {
        		// Booleans are boring, we know what those will be already.
        		if (arg.getType() != PlexilType.BOOLEAN) {
        			constants.add((PValue) arg);
        		}
        	}
        }
		
        // If there is a lookup here, any constants inside this operation
        // may be of interest. This is simpler than symbolic analysis.
        if (lookup != null && ! constants.isEmpty()) {
        	if ( ! lookupValuesOfInterest.containsKey(lookup)) {
        		lookupValuesOfInterest.put(lookup, new ArrayList<PValue>());
        	}
        	lookupValuesOfInterest.get(lookup).addAll(constants);
        }
        
		return null;
	}

	
	@Override
	public Void visitArrayIndex(ArrayIndexExpr array, PlexilType currentType) {
		// Maybe they're indexing an array with a lookup? That would be stupid,
		// but we do know this type I guess.
		array.getIndex().accept(this, PlexilType.INTEGER);
		return null;
	}

	


	@Override
	public Void visitAssignment(AssignmentBody assign, Node n) {
		PlexilType t = assign.getLeftHandSide().getType();
		// It's probably not set, though. 
		if (t == PlexilType.UNKNOWN) {
			t = resolveVariableType(assign.getLeftHandSide(), n);
		}
		
		// The right hand side should match the left. 
		assign.getRightHandSide().accept(this, t);
		return null;
	}

	@Override
	public Void visitNodeList(NodeListBody list, Node n) {
		for (Node child : list) {
			checkNode(child);
		}
		return null;
	}
	
	
	
	/*
	 * All the rest of this is boring. It's either variables, or things that 
	 * don't contain expressions. 
	 */
	
	@Override
	public Void visitNodeTimepoint(NodeTimepointExpr timept, PlexilType currentType) {
		return null;
	}

	@Override
	public Void visitBooleanValue(BooleanValue bool, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visitIntegerValue(IntegerValue integer, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visitRealValue(RealValue real, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visitStringValue(StringValue string, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visitUnknownValue(UnknownValue unk, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visitPValueList(PValueList<?> list, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visitCommandHandleState(CommandHandleState state,
			PlexilType currentType) {
		return null;
	}


	@Override
	public Void visitNodeFailure(NodeFailureType type, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visitNodeOutcome(NodeOutcome outcome, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visitNodeState(NodeState state, PlexilType currentType) {
		return null;
	}

	@Override
	public Void visitVariable(UnresolvedVariableExpr expr, PlexilType currentType) {
		// Nothing to do here
		return null;
	}


	@Override
	public Void visitNodeReference(NodeRefExpr ref, PlexilType currentType) {
		// Nothing to do here
		return null;
	}


	@Override
	public Void visitDefaultEnd(DefaultEndExpr end, PlexilType currentType) {
		// Also nothing
		return null;
	}
    
	
	/*
	 * These node bodies are boring too:
	 */

	@Override
	public Void visitEmpty(NodeBody empty, Node n) {
		return null;
	}


	@Override
	public Void visitLibrary(LibraryBody lib, Node n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitUpdate(UpdateBody update, Node n) {
		// Nothing to do
		return null;
	}

}
