package edu.umn.crisys.plexil.ast2il;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.CompositeExpr;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.ast.core.expr.var.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.core.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.core.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.core.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.core.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.core.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeBodyVisitor;
import edu.umn.crisys.plexil.ast.core.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.core.nodebody.UpdateBody;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class ASTExprToILExpr implements ASTExprVisitor<Void, ILExpression> {
    
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
    public ILExpression visitDefaultEnd(DefaultEndExpr end, Void param) {
        return context.getNodeBody().accept(new DefaultEndMaker(), context);
    }
    

    private static class DefaultEndMaker implements NodeBodyVisitor<NodeToIL, ILExpression> {

        @Override
        public ILExpression visitEmpty(NodeBody empty, NodeToIL node) {
            // This one is simple: False.
            return PValueExpression.FALSE;
        }

        @Override
        public ILExpression visitAssignment(AssignmentBody assign, NodeToIL node) {
            // TODO Make sure this is right by doing some experiments.
            // The detailed semantics say "assignment completed". And then 
            // there's a footnote saying that assignments always complete unless
            // there's an error evaluating the right hand side. Somehow. I'm
            // guessing an error like that is a showstopper, so it's safe to
            // just say that they always complete. 
            return PValueExpression.TRUE;
        }

        @Override
        public ILExpression visitCommand(CommandBody cmd, NodeToIL node) {
            // TODO: Check on this too. 
            // The documentation says "Command handle received". I think that
            // means that it has been set to something, so not UNKNOWN.
            return Operation.isKnown(node.getCommandHandle());
        }

        @Override
        public ILExpression visitLibrary(LibraryBody lib, NodeToIL node) {
            // Same as a list: "All children FINISHED". But there's just
            // one child, so it's easy.
            return Operation.eq(new PValueExpression(NodeState.FINISHED), 
                                node.getLibraryHandle());
        }

        @Override
        public ILExpression visitNodeList(NodeListBody list, NodeToIL node) {
            // All children FINISHED.
            List<Expression> childStates = new ArrayList<Expression>();
            for (NodeToIL child : node.getChildren()) {
                childStates.add(
                        Operation.eq(new PValueExpression(NodeState.FINISHED),
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
        // Right now, we only support direct node references.
        UnresolvedVariableExpr nodeName = (UnresolvedVariableExpr) e;
        return context.resolveNode(nodeName.getName());
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
            return translateComposite(op);
        }
    }
    /*
     * Everything else is really boring, because it's already an ILExpression!
     * We just either recurse down the tree or return it as-is. 
     * 
     */
    private ILExpression translateComposite(CompositeExpr expr) {
        List<Expression> newArgs = new ArrayList<Expression>();
        for (Expression arg : expr.getArguments()) {
            newArgs.add(arg.accept(this, null));
        }
        return expr.getCloneWithArgs(newArgs);
    }

    @Override
    public ILExpression visitArrayIndex(ArrayIndexExpr array, Void param) {
        return translateComposite(array);
    }

    @Override
    public ILExpression visitArrayLiteral(ArrayLiteralExpr array, Void param) {
        return translateComposite(array);
    }

    @Override
    public ILExpression visitLookupNow(LookupNowExpr lookup, Void param) {
        return translateComposite(lookup);
    }

    @Override
    public ILExpression visitLookupOnChange(LookupOnChangeExpr lookup,
            Void param) {
        return translateComposite(lookup);
    }

    @Override
    public ILExpression visitPValue(PValueExpression value, Void param) {
        return value;
    }
}
