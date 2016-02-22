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
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.ast.globaldecl.VariableDecl;
import edu.umn.crisys.plexil.ast.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBodyVisitor;
import edu.umn.crisys.plexil.ast.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.nodebody.UpdateBody;
import edu.umn.crisys.plexil.expr.ast.ASTExprVisitor;
import edu.umn.crisys.plexil.expr.ast.ASTLookupExpr;
import edu.umn.crisys.plexil.expr.ast.ASTOperation;
import edu.umn.crisys.plexil.expr.ast.ASTOperation.Operator;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILType;
import edu.umn.crisys.plexil.expr.il.NamedCondition;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
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

public class TypeAnalyzer extends ASTExprVisitor<ILType, Void> implements NodeBodyVisitor<Node, Void>{
	
    private Map<String, Optional<ILType>> lookups = new HashMap<>();
    private Map<String, List<PValue>> lookupValuesOfInterest = new HashMap<>();
    private Map<String, Optional<ILType>> commands = new HashMap<>();
    

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
			// Create a declaration based on this info
			LookupDecl inferred = new LookupDecl(lookup);
			ILType type = lookups.get(lookup).get();
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
        root.getStartCondition().accept(this, ILType.BOOLEAN);
        root.getSkipCondition().accept(this, ILType.BOOLEAN);
        root.getPreCondition().accept(this, ILType.BOOLEAN);
        root.getInvariantCondition().accept(this, ILType.BOOLEAN);
        root.getRepeatCondition().accept(this, ILType.BOOLEAN);
        root.getPostCondition().accept(this, ILType.BOOLEAN);
        root.getEndCondition().ifPresent(c -> c.accept(this, ILType.BOOLEAN));
        root.getExitCondition().accept(this, ILType.BOOLEAN);
        // And the body.
        root.getNodeBody().accept(this, root);
        
    }
    
    public Map<String, Optional<ILType>> getLookupTypes() {
    	return lookups;
    }
    
    public Map<String, List<PValue>> getPossibleLookupConstants() {
    	return lookupValuesOfInterest;
    }
    
    public Map<String, Optional<ILType>> getCommandTypes() {
    	return commands;
    }

    private static void customAdd(Map<String, Optional<ILType>> map, ILExpr key, Optional<ILType> value) {
		customAdd(map, "("+key.toString()+") (expression)", value);
    }
    
    private static void customAdd(Map<String, Optional<ILType>> map, String key, Optional<ILType> value) {
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
            } else if (map.get(key).map(t -> t == ILType.INTEGER).orElse(false)
            		&& (value.get() == ILType.REAL)) {
            	// Hmm, we thought this was an integer, but I guess we can't be
            	// that specific.
                map.put(key, Optional.of(ILType.REAL));
                return;
            } else if (map.get(key).map(t -> t == ILType.REAL).orElse(false) 
            		&& (value.get() == ILType.INTEGER)) {
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
	public Void visit(ASTLookupExpr lookup, ILType currentType) {
		ILExpr name = lookup.getLookupName();
		if ( ! (name instanceof StringValue)) {
			customAdd(lookups, name, Optional.of(currentType));
		} else {
			customAdd(lookups, ((StringValue)name).getString(), Optional.of(currentType));
		}
		return null;
	}

	@Override
	public Void visitCommand(CommandBody cmd, Node n) {
		Optional<ILType> typeToSet = Optional.empty();
		cmd.getVarToAssign().ifPresent(
				(varToAssign) -> resolveVariableType(varToAssign, n));
		// Even if it's not there, we want to set it. Empty means that it's a command
		// that doesn't appear to return anything.
		
        // Get command name
		ILExpr name = cmd.getCommandName();
		if ( ! (name instanceof StringValue)) {
			// Don't try to extract string
			customAdd(commands, name, typeToSet);
		}  else {
			String realName = ((StringValue)name).getString();
			customAdd(commands, realName, typeToSet);
		}
		return null;
	}
	
	private static ILType resolveVariableType(ILExpr e, Node n) {
		if (e instanceof ASTOperation) {
			// This must be an array index. We can get the array type!
			ILType arrayType = resolveVariableType(((ASTOperation) e)
					.getBinaryFirst(), n);
			if (arrayType.isArrayType()) {
				return arrayType.elementType();
			} else {
				// Mmm, didn't work. I guess we're stuck.
				return ILType.UNKNOWN;
			}
		} else if (e instanceof UnresolvedVariableExpr) {
			return resolveVariableType(((UnresolvedVariableExpr) e).getName(), n);
		} else {
			System.err.println("Add LHS type to resolveVariableType() : "+e+" "+e.getClass());
			return ILType.UNKNOWN;
		}

	}

	private static ILType resolveVariableType(String name, Node n) {
		if (n.containsVar(name)) {
			return n.getVariableInfo(name).getType();
		} else if (n.getParent().isPresent()) {
			return resolveVariableType(name, n.getParent().get());
		} else {
			return ILType.UNKNOWN;
		}
	}

	@Override
	public Void visit(ASTOperation op, ILType currentType) {
		// Operators contain information about their argument types.
		ILType argType = op.getExpectedArgumentType();
		
		if (argType.isNumeric()) {
			// These are trickier. We need to know actual argument type.
			argType = op.getArguments().stream()
					.map(arg -> arg.getType())
					.reduce(ILType.UNKNOWN, ILType::getMoreSpecific);
		}
		
		// If it's a NodeRef, there's no point in continuing.
		if (argType == ILType.NODEREF) return null;
		if (op.getOperator() == Operator.ARRAY_INDEX) {
			// Maybe they're indexing an array with a lookup? That would be stupid,
			// but we do know this type I guess.
			op.getBinarySecond().accept(this, ILType.INTEGER);
			return null;
		}
		
        // Move down, and pass along the argument type.
        String lookup = null;
        Set<PValue> constants = new HashSet<PValue>();
        for (ILExpr arg : op.getArguments()) {
        	arg.accept(this, argType);
        	
        	if (arg instanceof ASTLookupExpr) {
        		lookup = ((ASTLookupExpr) arg).getLookupNameAsString();
        	} else if (arg instanceof PValue) {
        		// Booleans are boring, we know what those will be already.
        		if (arg.getType() != ILType.BOOLEAN) {
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
	public Void visit(NamedCondition named, ILType currentType) {
		// Just pass through
		named.getExpression().accept(this, currentType);
		return null;
	}


	@Override
	public Void visitAssignment(AssignmentBody assign, Node n) {
		ILType t = assign.getLeftHandSide().getType();
		// It's probably not set, though. 
		if (t == ILType.UNKNOWN) {
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
	public Void visit(NodeTimepointExpr timept, ILType currentType) {
		return null;
	}

	@Override
	public Void visit(BooleanValue bool, ILType currentType) {
		return null;
	}


	@Override
	public Void visit(IntegerValue integer, ILType currentType) {
		return null;
	}


	@Override
	public Void visit(RealValue real, ILType currentType) {
		return null;
	}


	@Override
	public Void visit(StringValue string, ILType currentType) {
		return null;
	}


	@Override
	public Void visit(UnknownValue unk, ILType currentType) {
		return null;
	}


	@Override
	public Void visit(PValueList<?> list, ILType currentType) {
		return null;
	}


	@Override
	public Void visit(CommandHandleState state,
			ILType currentType) {
		return null;
	}


	@Override
	public Void visit(NodeFailureType type, ILType currentType) {
		return null;
	}


	@Override
	public Void visit(NodeOutcome outcome, ILType currentType) {
		return null;
	}


	@Override
	public Void visit(NodeState state, ILType currentType) {
		return null;
	}

	@Override
	public Void visit(UnresolvedVariableExpr expr, ILType currentType) {
		// Nothing to do here
		return null;
	}


	@Override
	public Void visit(NodeRefExpr ref, ILType currentType) {
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
