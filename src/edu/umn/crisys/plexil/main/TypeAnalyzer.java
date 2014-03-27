package edu.umn.crisys.plexil.main;

import java.util.HashMap;
import java.util.Map;

import edu.umn.crisys.plexil.ast.core.Node;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.ast.core.expr.var.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.core.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.core.expr.var.NodeRefExpr;
import edu.umn.crisys.plexil.ast.core.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.core.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.core.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.core.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeBodyVisitor;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.core.nodebody.UpdateBody;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.RealValue;
import edu.umn.crisys.plexil.java.values.StringValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;

public class TypeAnalyzer implements ASTExprVisitor<PlexilType, Void>, NodeBodyVisitor<Node, Void>{
	
    public Map<String, PlexilType> lookups = new HashMap<String, PlexilType>();
    public Map<String, PlexilType> commands = new HashMap<String, PlexilType>();

    
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
    
    
    public Map<String, PlexilType> getCommandTypes() {
    	return commands;
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
            	System.err.println("Found incompatible types for "+key+":");
            	System.err.println("Before, I had "+map.get(key)+", but now I'm seeing "+value);
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
			System.err.println("Non-constant lookup: "+name);
		} else {
			customAdd(lookups, ((StringValue)name).getString(), currentType);
		}
		return null;
	}


	@Override
	public Void visitLookupOnChange(LookupOnChangeExpr lookup, PlexilType currentType) {
		Expression name = lookup.getLookupName();
		if ( ! (name instanceof StringValue)) {
			System.err.println("Non-constant lookup: "+name);
		} else {
			customAdd(lookups, ((StringValue)name).getString(), currentType);
		}
		return null;
	}
	
	@Override
	public Void visitCommand(CommandBody cmd, Node n) {
        // Get command name first
		Expression name = cmd.getCommandName();
		if ( ! (name instanceof StringValue)) {
			System.err.println("Non-constant command: "+name);
			return null;
		} 
		String realName = ((StringValue)name).getString();
		if (cmd.getVarToAssign() != null) {
			PlexilType varType = resolveVariableType(cmd.getVarToAssign(), n);
			customAdd(commands, realName, varType);
		} else {
			// Shouldn't assign anything, so add that information too.
            customAdd(commands, realName, null);
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
		if (n == null) {
			return PlexilType.UNKNOWN;
		}
		if (n.containsVar(name)) {
			return n.getVariableInfo(name).getType();
		} else {
			return resolveVariableType(name, n.getParent());
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
        for (Expression arg : op.getArguments()) {
        	arg.accept(this, argType);
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
