package edu.umn.crisys.plexil.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.umn.crisys.plexil.ast.Node;
import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.ASTLookupExpr;
import edu.umn.crisys.plexil.ast.expr.ASTOperation;
import edu.umn.crisys.plexil.ast.expr.NodeRefExpr;
import edu.umn.crisys.plexil.ast.expr.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.expr.PlexilExpr;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.ast.expr.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.expr.ASTOperation.Operator;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.ast.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBodyVisitor;
import edu.umn.crisys.plexil.ast.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.nodebody.UpdateBody;
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
import edu.umn.crisys.plexil.runtime.values.UnknownBool;
import edu.umn.crisys.plexil.runtime.values.UnknownInt;
import edu.umn.crisys.plexil.runtime.values.UnknownReal;
import edu.umn.crisys.plexil.runtime.values.UnknownString;

public class TypeAnalyzer extends ASTExprVisitor<PlexilType, Void> implements NodeBodyVisitor<Node, Void>{
	
    private Map<String, Optional<PlexilType>> lookups = new HashMap<>();
    private Map<String, List<PValue>> lookupValuesOfInterest = new HashMap<>();
    private Map<String, Optional<PlexilType>> commands = new HashMap<>();
    

	public void printAnalysis() {
		System.out.println("    Lookups:");
		for (String key : lookups.keySet()) {
			System.out.println("      "+key+" returns "+lookups.get(key).get());
			if (lookupValuesOfInterest.containsKey(key) && ! lookupValuesOfInterest.get(key).isEmpty()) {
				System.out.println("        Possible values: ");
				for (PValue v : lookupValuesOfInterest.get(key)) {
					System.out.println("          "+v);
				}
			}
			
		}
		System.out.println("    Commands:");
		for (String key : commands.keySet()) {
			String type = commands.get(key).map(Object::toString).orElse("nothing");
			System.out.println("      "+key+" returns "+type);
		}
		
		
		System.out.println();

	}
	
	public void inferTypeDeclarations(PlexilPlan plan) {
		for (String lookup : lookups.keySet()) {
			if (plan.getStateDeclarations().stream()
					.anyMatch(d -> d.getName().equals(lookup))) {
				continue;
			}
			if (lookup.equals("time")) {
				// Don't try to infer this, there's already a mechanism for
				// deciding how to handle time.
				continue;
			}
			// Create a declaration based on this info
			LookupDecl inferred = new LookupDecl(lookup);
			PlexilType type = lookups.get(lookup).get();
			if (type.isArrayType()) {
				// Need one with an index
				inferred.setReturnValue(new VariableDecl("<inferred-array>", 0, type));
			} else {
				inferred.setReturnValue(new VariableDecl("<inferred>", lookups.get(lookup).get()));
			}
			
			plan.getStateDeclarations().add(inferred);
		}
	}

    public void checkNode(Node root) {
        // Check all the conditions
        root.getStartCondition().accept(this, PlexilType.BOOLEAN);
        root.getSkipCondition().accept(this, PlexilType.BOOLEAN);
        root.getPreCondition().accept(this, PlexilType.BOOLEAN);
        root.getInvariantCondition().accept(this, PlexilType.BOOLEAN);
        root.getRepeatCondition().accept(this, PlexilType.BOOLEAN);
        root.getPostCondition().accept(this, PlexilType.BOOLEAN);
        root.getEndCondition().ifPresent(c -> c.accept(this, PlexilType.BOOLEAN));
        root.getExitCondition().accept(this, PlexilType.BOOLEAN);
        // And the body.
        root.getNodeBody().accept(this, root);
        
    }
    
    public Map<String, Optional<PlexilType>> getLookupTypes() {
    	return lookups;
    }
    
    public Map<String, List<PValue>> getPossibleLookupConstants() {
    	return lookupValuesOfInterest;
    }
    
    public Map<String, Optional<PlexilType>> getCommandTypes() {
    	return commands;
    }

    private static void customAdd(Map<String, Optional<PlexilType>> map, PlexilExpr key, Optional<PlexilType> value) {
		customAdd(map, "("+key.toString()+") (expression)", value);
    }
    
    private static void customAdd(Map<String, Optional<PlexilType>> map, String key, Optional<PlexilType> value) {
        if (map.containsKey(key)) {
        	if ( ! value.isPresent()) {
        		// We already have something for this, if they're telling us
        		// they don't know, this isn't going to help.
        		return;
        	}
        	// Check the existing value and make sure it makes sense.
            if (map.get(key).map(t -> t == value.get()).orElse(false)) {
            	// Okay, we already knew that.
                return;
            } else if ( ! map.get(key).isPresent()) {
            	// Ah, we had seen this before, but we didn't know it had a type.
                map.put(key, value);
                return;
            } else if (map.get(key).map(t -> t == PlexilType.INTEGER).orElse(false)
            		&& (value.get() == PlexilType.REAL)) {
            	// Hmm, we thought this was an integer, but I guess we can't be
            	// that specific.
                map.put(key, Optional.of(PlexilType.REAL));
                return;
            } else if (map.get(key).map(t -> t == PlexilType.REAL).orElse(false) 
            		&& (value.get() == PlexilType.INTEGER)) {
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
	public Void visit(ASTLookupExpr lookup, PlexilType currentType) {
		PlexilExpr name = lookup.getLookupName();
		if ( ! (name instanceof StringValue)) {
			customAdd(lookups, name, Optional.of(currentType));
		} else {
			customAdd(lookups, ((StringValue)name).getString(), Optional.of(currentType));
		}
		return null;
	}

	@Override
	public Void visitCommand(CommandBody cmd, Node n) {
		Optional<PlexilType> typeToSet = Optional.empty();
		cmd.getVarToAssign().ifPresent(
				(varToAssign) -> resolveVariableType(varToAssign, n));
		// Even if it's not there, we want to set it. Empty means that it's a command
		// that doesn't appear to return anything.
		
        // Get command name
		PlexilExpr name = cmd.getCommandName();
		if ( ! (name instanceof StringValue)) {
			// Don't try to extract string
			customAdd(commands, name, typeToSet);
		}  else {
			String realName = ((StringValue)name).getString();
			customAdd(commands, realName, typeToSet);
		}
		return null;
	}
	
	private static PlexilType resolveVariableType(PlexilExpr e, Node n) {
		if (e instanceof ASTOperation) {
			// This must be an array index. We can get the array type!
			PlexilType arrayType = resolveVariableType(((ASTOperation) e)
					.getPlexilArguments().get(0), n);
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
	public Void visit(ASTOperation op, PlexilType currentType) {
		// Operators contain information about their argument types.
		PlexilType argType = op.getExpectedArgumentType();
		
		if (argType.isNumeric()) {
			// These are trickier. We need to know actual argument type.
			argType = op.getPlexilArguments().stream()
					.map(arg -> arg.getPlexilType())
					.reduce(PlexilType.UNKNOWN, PlexilType::getMoreSpecific);
		}
		
		// If it's a NodeRef, there's no point in continuing.
		if (argType == PlexilType.NODEREF) return null;
		if (op.getOperator() == Operator.ARRAY_INDEX) {
			// Maybe they're indexing an array with a lookup? That would be stupid,
			// but we do know this type I guess.
			op.getPlexilArguments().get(1).accept(this, PlexilType.INTEGER);
			return null;
		}
		
        // Move down, and pass along the argument type.
        String lookup = null;
        Set<PValue> constants = new HashSet<PValue>();
        for (PlexilExpr arg : op.getPlexilArguments()) {
        	arg.accept(this, argType);
        	
        	if (arg instanceof ASTLookupExpr) {
        		lookup = ((ASTLookupExpr) arg).getLookupNameAsString();
        	} else if (arg instanceof PValue) {
        		// Booleans are boring, we know what those will be already.
        		if (arg.getPlexilType() != PlexilType.BOOLEAN) {
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
	public Void visitAssignment(AssignmentBody assign, Node n) {
		PlexilType t = assign.getLeftHandSide().getPlexilType();
		// Odds are, the left hand side is an "UnresolvedVariable", so let's
		// dig deeper. 
		if (! t.isSpecificType()) {
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
	public Void visit(NodeTimepointExpr timept, PlexilType currentType) {
		return null;
	}

	@Override
	public Void visit(BooleanValue bool, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visit(IntegerValue integer, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visit(RealValue real, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visit(StringValue string, PlexilType currentType) {
		return null;
	}

	@Override
	public Void visit(UnknownBool unk, PlexilType param) {
		return null;
	}

	@Override
	public Void visit(UnknownInt unk, PlexilType param) {
		return null;
	}

	@Override
	public Void visit(UnknownReal unk, PlexilType param) {
		return null;
	}

	@Override
	public Void visit(UnknownString unk, PlexilType param) {
		return null;
	}


	@Override
	public Void visit(PValueList<?> list, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visit(CommandHandleState state,
			PlexilType currentType) {
		return null;
	}


	@Override
	public Void visit(NodeFailureType type, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visit(NodeOutcome outcome, PlexilType currentType) {
		return null;
	}


	@Override
	public Void visit(NodeState state, PlexilType currentType) {
		return null;
	}

	@Override
	public Void visit(UnresolvedVariableExpr expr, PlexilType currentType) {
		// Nothing to do here
		return null;
	}


	@Override
	public Void visit(NodeRefExpr ref, PlexilType currentType) {
		// Nothing to do here
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
