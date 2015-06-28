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
import edu.umn.crisys.plexil.expr.ExprType;
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

public class TypeAnalyzer extends ASTExprVisitor<ExprType, Void> implements NodeBodyVisitor<Node, Void>{
	
    private Map<String, ExprType> lookups = new HashMap<String, ExprType>();
    private Map<String, List<PValue>> lookupValuesOfInterest = new HashMap<String, List<PValue>>();
    private Map<String, ExprType> commands = new HashMap<String, ExprType>();
    

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
        root.getStartCondition().accept(this, ExprType.BOOLEAN);
        root.getSkipCondition().accept(this, ExprType.BOOLEAN);
        root.getPreCondition().accept(this, ExprType.BOOLEAN);
        root.getInvariantCondition().accept(this, ExprType.BOOLEAN);
        root.getRepeatCondition().accept(this, ExprType.BOOLEAN);
        root.getPostCondition().accept(this, ExprType.BOOLEAN);
        root.getEndCondition().accept(this, ExprType.BOOLEAN);
        root.getExitCondition().accept(this, ExprType.BOOLEAN);
        // And the body.
        root.getNodeBody().accept(this, null);
        
    }
    
    public Map<String, ExprType> getLookupTypes() {
    	return lookups;
    }
    
    public Map<String, List<PValue>> getPossibleLookupConstants() {
    	return lookupValuesOfInterest;
    }
    
    public Map<String, ExprType> getCommandTypes() {
    	return commands;
    }

    private void customAdd(Map<String, ExprType> map, Expression key, ExprType value) {
		customAdd(lookups, "("+key.toString()+") (expression)", value);
    }
    
    private void customAdd(Map<String, ExprType> map, String key, ExprType value) {
        if (map.containsKey(key)) {
        	// Check the existing value and make sure it makes sense.
            if (map.get(key) == value) {
            	// Okay, we already knew that.
                return;
            } else if (map.get(key) == null) {
            	// Ah, we had seen this before, but we didn't know it had a type.
                map.put(key, value);
                return;
            } else if (map.get(key) == ExprType.INTEGER &&
                    (value == ExprType.REAL || value == ExprType.NUMERIC)) {
            	// Hmm, we thought this was an integer, but I guess we can't be
            	// that specific.
                map.put(key, ExprType.NUMERIC);
                return;
            } else if (map.get(key) == ExprType.REAL &&
                    (value == ExprType.INTEGER || value == ExprType.NUMERIC)) {
            	// Hmm, we thought it was a real, but I guess we can't be that specific.
                map.put(key, ExprType.NUMERIC);
                return;
            } else if (map.get(key) == ExprType.NUMERIC &&
                    (value == ExprType.REAL || value == ExprType.INTEGER)) {
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
	public Void visit(LookupNowExpr lookup, ExprType currentType) {
		Expression name = lookup.getLookupName();
		if ( ! (name instanceof StringValue)) {
			customAdd(lookups, name, currentType);
		} else {
			customAdd(lookups, ((StringValue)name).getString(), currentType);
		}
		return null;
	}


	@Override
	public Void visit(LookupOnChangeExpr lookup, ExprType currentType) {
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
		ExprType typeToSet = null;
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
	
	private static ExprType resolveVariableType(Expression e, Node n) {
		if (e instanceof ArrayIndexExpr) {
			// We can get the array type!
			ExprType arrayType = resolveVariableType(((ArrayIndexExpr) e).getArray(), n);
			if (arrayType.isArrayType()) {
				return arrayType.elementType();
			} else {
				// Mmm, didn't work. I guess we're stuck.
				return ExprType.UNKNOWN;
			}
		} else if (e instanceof UnresolvedVariableExpr) {
			return resolveVariableType(((UnresolvedVariableExpr) e).getName(), n);
		} else {
			System.err.println("Add LHS type to resolveVariableType() : "+e+" "+e.getClass());
			return ExprType.UNKNOWN;
		}

	}

	private static ExprType resolveVariableType(String name, Node n) {
		if (n.containsVar(name)) {
			return n.getVariableInfo(name).getType();
		} else if (n.getParent().isPresent()) {
			return resolveVariableType(name, n.getParent().get());
		} else {
			return ExprType.UNKNOWN;
		}
	}

	@Override
	public Void visit(Operation op, ExprType currentType) {
		// Operators contain information about their argument types.
		ExprType argType = op.getExpectedArgumentType();
		
		// If it's a NodeRef, there's no point in continuing.
		if (argType == ExprType.NODEREF) return null;

        // It's possible that we can be more specific than "numeric":
        if (argType == ExprType.NUMERIC) {
            // Do we know if this is gonna be a real or integer already?
            if (currentType == ExprType.INTEGER ||
                    currentType == ExprType.REAL) {
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
        		if (arg.getType() != ExprType.BOOLEAN) {
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
	public Void visit(ArrayIndexExpr array, ExprType currentType) {
		// Maybe they're indexing an array with a lookup? That would be stupid,
		// but we do know this type I guess.
		array.getIndex().accept(this, ExprType.INTEGER);
		return null;
	}

	


	@Override
	public Void visitAssignment(AssignmentBody assign, Node n) {
		ExprType t = assign.getLeftHandSide().getType();
		// It's probably not set, though. 
		if (t == ExprType.UNKNOWN) {
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
	public Void visit(NodeTimepointExpr timept, ExprType currentType) {
		return null;
	}

	@Override
	public Void visit(BooleanValue bool, ExprType currentType) {
		return null;
	}


	@Override
	public Void visit(IntegerValue integer, ExprType currentType) {
		return null;
	}


	@Override
	public Void visit(RealValue real, ExprType currentType) {
		return null;
	}


	@Override
	public Void visit(StringValue string, ExprType currentType) {
		return null;
	}


	@Override
	public Void visit(UnknownValue unk, ExprType currentType) {
		return null;
	}


	@Override
	public Void visit(PValueList<?> list, ExprType currentType) {
		return null;
	}


	@Override
	public Void visit(CommandHandleState state,
			ExprType currentType) {
		return null;
	}


	@Override
	public Void visit(NodeFailureType type, ExprType currentType) {
		return null;
	}


	@Override
	public Void visit(NodeOutcome outcome, ExprType currentType) {
		return null;
	}


	@Override
	public Void visit(NodeState state, ExprType currentType) {
		return null;
	}

	@Override
	public Void visit(UnresolvedVariableExpr expr, ExprType currentType) {
		// Nothing to do here
		return null;
	}


	@Override
	public Void visit(NodeRefExpr ref, ExprType currentType) {
		// Nothing to do here
		return null;
	}


	@Override
	public Void visit(DefaultEndExpr end, ExprType currentType) {
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
