package edu.umn.crisys.plexil.ast2il;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.ast.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBodyVisitor;
import edu.umn.crisys.plexil.ast.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.nodebody.UpdateBody;
import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.expr.ast.DefaultEndExpr;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.expr.common.Operation;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class ASTExprToILExpr implements ExprVisitor<Void, Expression> {
    
    private NodeToIL context;
    
    public ASTExprToILExpr(NodeToIL context) {
        this.context = context;
    }
    
    @Override
    public Expression visit(Expression e, Void param) {
    	// Just apply ourselves to arguments by default
    	return e.getCloneWithArgs(e.getArguments().stream()
    			.map(arg -> arg.accept(this))
    			.collect(Collectors.toList()));
    }
    
    @Override
	public Expression visit(LookupNowExpr lookup, Void param) {
    	// Try to add some type information
    	if (lookup.hasConstantLookupName()) {
    		PlexilType type = context.getTypeOfLookup(lookup.getLookupNameAsString());
    		return new LookupNowExpr(type, lookup.getLookupName(), lookup.getLookupArgs());
    	}
		return visit((Expression)lookup, null);
	}

	@Override
	public Expression visit(LookupOnChangeExpr lookup,
			Void param) {
		// Try to add type information
    	if (lookup.hasConstantLookupName()) {
    		PlexilType type = context.getTypeOfLookup(lookup.getLookupNameAsString());
    		return new LookupOnChangeExpr(type, lookup.getLookupName(), 
    				lookup.getTolerance(), lookup.getLookupArgs());
    	}
		return visit((Expression)lookup, null);
	}

	@Override
    public Expression visit(UnresolvedVariableExpr expr, Void param) {
        if (expr.getType() == PlexilType.NODEREF) {
            throw new RuntimeException("Node references should be resolved by "
                    + "the operation that needs them, they can't be used directly");
        }
        return context.resolveVariable(expr.getName(), expr.getType());
    }

    @Override
	public Expression visit(NodeRefExpr ref, Void param) {
		throw new RuntimeException("This reference should have been resolved by the operation that used it");
	}

	@Override
    public Expression visit(DefaultEndExpr end, Void param) {
		// The default end condition depends on the body type:
        return context.getASTNodeBody().accept(new NodeBodyVisitor<NodeToIL, Expression>() {

            @Override
            public Expression visitEmpty(NodeBody empty, NodeToIL node) {
                // This one is simple: True.
                return BooleanValue.get(true);
            }

            @Override
            public Expression visitAssignment(AssignmentBody assign, NodeToIL node) {
                // TODO Make sure this is right by doing some experiments.
                // The detailed semantics say "assignment completed". And then 
                // there's a footnote saying that assignments always complete unless
                // there's an error evaluating the right hand side. Somehow. I'm
                // guessing an error like that is a showstopper, so it's safe to
                // just say that they always complete. 
                return BooleanValue.get(true);
            }

            @Override
            public Expression visitCommand(CommandBody cmd, NodeToIL node) {
                // The wiki says "Command handle received". But what does that mean?
            	
            	// The PLEXIL source code seems to imply that the default
            	// end condition for Command nodes is just "true". Not
            	// "command handle received" as the wiki states. (CommandNode.cc)
            	// What it does is OR whatever end condition it has with 
            	// handle == denied || handle == failed. 

                //return Operation.isKnown(node.getCommandHandle());
            	return BooleanValue.get(true);
            }

            @Override
            public Expression visitLibrary(LibraryBody lib, NodeToIL node) {
            	// If the library got included statically, this NodeToIL might 
            	// actually have a real child instead of a handle.
            	if (node.hasLibraryHandle()) {
    	            // Treat it the same as a list: "All children FINISHED". 
    	    		// But there's just one child, so it's easy.
    	            return Operation.eq(NodeState.FINISHED, 
    	                                node.getLibraryHandle());
            	} else {
            		// Since we don't actually care about the AST body, passing in
            		// null is fine. 
            		return visitNodeList(null, node);
            	}
            }

            @Override
            public Expression visitNodeList(NodeListBody list, NodeToIL node) {
                // All children FINISHED.
                List<Expression> childStates = new ArrayList<Expression>();
                for (NodeToIL child : node.getChildren()) {
                    childStates.add(
                            Operation.eq(NodeState.FINISHED,
                                         child.getState()));
                }
                return Operation.and(childStates);
            }

            @Override
            public Expression visitUpdate(UpdateBody update, NodeToIL node) {
                // Invocation success. Pretty sure this just means ACK. 
                return node.getUpdateHandle();
            }
            
        }, context);
    }
    

    private NodeToIL resolveNode(Expression e) {
    	if (e instanceof UnresolvedVariableExpr) {
    		UnresolvedVariableExpr nodeName = (UnresolvedVariableExpr) e;
    		return context.resolveNode(nodeName.getName());
    	} else if (e instanceof NodeRefExpr) {
    		NodeRefExpr ref = (NodeRefExpr) e;
    		switch (ref) {
    		case PARENT: 
    			return context.getParent().orElseThrow(
    					() -> new RuntimeException(context+" has no parent!"));
    		case CHILD: 
    			if (context.getChildren().size() != 1) {
    				throw new RuntimeException("Which child?");
    			}
    			return context.getChildren().get(0);
    		case SIBLING:
    			NodeToIL parent = context.getParent().orElseThrow(
    					() -> new RuntimeException(context+" has no parent!"));
    			if (parent.getChildren().size() != 2) {
    				throw new RuntimeException("Which sibling?");
    			}
    			NodeToIL sibling0 = parent.getChildren().get(0);
    			NodeToIL sibling1 = parent.getChildren().get(1);
    			if (sibling0 == context) {
    				return sibling1;
    			} else {
    				return sibling0;
    			}
    		case SELF:
    			return context;
    		default: 
    			throw new RuntimeException("Missing case: "+ref);
    		}
    	}
    	else {
    		throw new RuntimeException("How do you resolve a node given a "+e.getClass()+"?");
    	}
    }
    
    @Override
    public Expression visit(NodeTimepointExpr timept, Void param) {
        // This one we actually have to take apart, since it ends up as a 
        // variable in the IL.
        return resolveNode(timept.getNodeId()).getNodeTimepoint(timept.getState(), timept.getTimepoint());
    }

    @Override
    public Expression visit(Operation op, Void param) {
        // Some of these we need to handle specially. Specifically, the node
        // operations like .state and .command_handle.
        switch (op.getOperator()) {
        case GET_STATE:
            return resolveNode(op.getArguments().get(0)).getState();
        case GET_OUTCOME:
            return resolveNode(op.getArguments().get(0)).getOutcome();
        case GET_FAILURE:
            return resolveNode(op.getArguments().get(0)).getFailure();
        case GET_COMMAND_HANDLE:
            return resolveNode(op.getArguments().get(0)).getCommandHandle();
        default:
            // Everything else is just a composite node that we can deal with
            // the same way. 
            return visit((Expression)op, null);
        }
    }

    private Expression visitILExpr(Expression e) {
    	throw new RuntimeException(e+" is already an IL-only expr, something is "
    			+ "probably very wrong here");
    }
    
	@Override
	public Expression visit(GetNodeStateExpr state, Void param) {
		return visitILExpr(state);
	}

	@Override
	public Expression visit(SimpleVar var, Void param) {
		return visitILExpr(var);
	}

	@Override
	public Expression visit(ArrayVar array, Void param) {
		return visitILExpr(array);
	}

	@Override
	public Expression visit(LibraryVar lib, Void param) {
		return visitILExpr(lib);
	}

}
