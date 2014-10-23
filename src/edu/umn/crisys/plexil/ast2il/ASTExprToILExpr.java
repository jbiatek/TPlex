package edu.umn.crisys.plexil.ast2il;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.Operation;
import edu.umn.crisys.plexil.ast.expr.var.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeRefExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBodyVisitor;
import edu.umn.crisys.plexil.ast.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.nodebody.UpdateBody;
import edu.umn.crisys.plexil.il.expr.ILExprModifier;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class ASTExprToILExpr extends ILExprModifier<Void> implements ASTExprVisitor<Void, ILExpression> {
    
    private NodeToIL context;
    
    public ASTExprToILExpr(NodeToIL context) {
        this.context = context;
    }
    
    @Override
    public ILExpression visitVariable(UnresolvedVariableExpr expr, Void param) {
        if (expr.getType() == PlexilType.NODEREF) {
            throw new RuntimeException("Node references should be resolved by "
                    + "the operation that needs them, they can't be used directly");
        }
        return context.resolveVariable(expr.getName(), expr.getType());
    }

    @Override
	public ILExpression visitNodeReference(NodeRefExpr ref, Void param) {
		throw new RuntimeException("This reference should have been resolved by the operation that used it");
	}

	@Override
    public ILExpression visitDefaultEnd(DefaultEndExpr end, Void param) {
        return context.getASTNodeBody().accept(new DefaultEndMaker(), context);
    }
    

    private static class DefaultEndMaker implements NodeBodyVisitor<NodeToIL, ILExpression> {

        @Override
        public ILExpression visitEmpty(NodeBody empty, NodeToIL node) {
            // This one is simple: True.
            return BooleanValue.get(true);
        }

        @Override
        public ILExpression visitAssignment(AssignmentBody assign, NodeToIL node) {
            // TODO Make sure this is right by doing some experiments.
            // The detailed semantics say "assignment completed". And then 
            // there's a footnote saying that assignments always complete unless
            // there's an error evaluating the right hand side. Somehow. I'm
            // guessing an error like that is a showstopper, so it's safe to
            // just say that they always complete. 
            return BooleanValue.get(true);
        }

        @Override
        public ILExpression visitCommand(CommandBody cmd, NodeToIL node) {
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
        public ILExpression visitLibrary(LibraryBody lib, NodeToIL node) {
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
        public ILExpression visitNodeList(NodeListBody list, NodeToIL node) {
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
        public ILExpression visitUpdate(UpdateBody update, NodeToIL node) {
            // Invocation success. Pretty sure this just means ACK. 
            return node.getUpdateHandle();
        }
        
    }

    private NodeToIL resolveNode(Expression e) {
    	if (e instanceof UnresolvedVariableExpr) {
    		UnresolvedVariableExpr nodeName = (UnresolvedVariableExpr) e;
    		return context.resolveNode(nodeName.getName());
    	} else if (e instanceof NodeRefExpr) {
    		NodeRefExpr ref = (NodeRefExpr) e;
    		switch (ref.getNodeRef()) {
    		case PARENT: 
    			if (context.getParent() == null) throw new NullPointerException();
    			return context.getParent();
    		case CHILD: 
    			if (context.getChildren().size() != 1) {
    				throw new RuntimeException("Which child?");
    			}
    			return context.getChildren().get(0);
    		case SIBLING:
    			if (context.getParent().getChildren().size() != 2) {
    				throw new RuntimeException("Which sibling?");
    			}
    			NodeToIL sibling0 = context.getParent().getChildren().get(0);
    			NodeToIL sibling1 = context.getParent().getChildren().get(1);
    			if (sibling0 == context) {
    				return sibling1;
    			} else {
    				return sibling0;
    			}
    		case SELF:
    			return context;
    		}
    		throw new RuntimeException("Missing case: "+ref.getNodeRef());
    	}
    	else {
    		throw new RuntimeException("How do you resolve a node given a "+e.getClass()+"?");
    	}
    }
    
    @Override
    public ILExpression visitNodeTimepoint(NodeTimepointExpr timept, Void param) {
        // This one we actually have to take apart, since it ends up as a 
        // variable in the IL.
        return resolveNode(timept.getNodeId()).getNodeTimepoint(timept.getState(), timept.getTimepoint());
    }

    @Override
    public ILExpression visitOperation(Operation op, Void param) {
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
            return visitComposite(op, param);
        }
    }

}
