package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ast.ASTOperation;
import edu.umn.crisys.plexil.expr.ast.ASTOperation.Operator;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILOperator;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.PlexilExprDescription;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.NamedType;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;

public class IncrementalConstraintGenerator {

	public static void go(Plan ilPlan, NodeBuilder nb, PlanToLustre p2l) {
		System.out.println("Generating incremental constraints...");
		List<OriginalHierarchy> parentChain = 
				findIf55(ilPlan.getOriginalHierarchy());
		
		if (parentChain.isEmpty()) {
			throw new RuntimeException("Couldn't find If 55");
		}
		
		// The root node is going to execute no matter what. No reason to
		// search for that and concretize things prematurely.
		parentChain.remove(0);
		
		// Add nodes in sequences too
		System.out.println("Going through target node's ancestry");
		List<OriginalHierarchy> actualChain = new ArrayList<>();
		for (int i = 0; i < parentChain.size(); i++) {
			OriginalHierarchy currentNode = parentChain.get(i);
			// Add this one in as a goal
			actualChain.add(currentNode);
			System.out.println(currentNode.getUID()+" added as a goal");
			if (i == parentChain.size() - 1) {
				// This is the last one, no need to check for sequences
				continue;
			}
			
			
			// Does the next node look like it's actually in a sequence?
			OriginalHierarchy nextNode = parentChain.get(i+1);
			Set<NodeUID> nextNodeWaitingFor = nodesThisOneIsWaitingFor(nextNode);
			System.out.println("Next node "+nextNode.getUID()
					+" appears to be waiting for "+nextNodeWaitingFor);
			if ( ! nodesThisOneIsWaitingFor(nextNode).isEmpty()) {
				// It's waiting for others. Let's go through the current
				// node's children until we get to the next goal.
				for (OriginalHierarchy child : currentNode.getChildren()) {
					if (child == nextNode) break;
					System.out.println("Adding "+child.getUID()+" as an additional goal.");
					actualChain.add(child);
				}
			}
		}
		
		List<Expr> failureTrackers = new ArrayList<>();
		
		for (OriginalHierarchy node : actualChain) {
			NodeUID uid = node.getUID();
			String id = LustreNamingConventions.getStateMapperId(uid)+"_executes";
			// If this was in the parent chain, from now on make sure it 
			// doesn't fail
			if (parentChain.contains(node)) {
				addFailureTracker(node, p2l, nb);
				failureTrackers.add(not(getFailureTrackerId(node)));
			}
			
			List<Expr> allConditions = new ArrayList<>();
			// This state could never be executing, right? Prove me wrong!
			allConditions.add(nodeIsExecuting(uid, p2l));
			// Also, make sure that nothing fails along the way
			allConditions.addAll(failureTrackers);
			// And that we're on a macro step boundary.
			allConditions.add(LustreNamingConventions.MACRO_STEP_ENDED);
			
			nb.addLocal(new VarDecl(id, NamedType.BOOL));
			nb.addEquation(new Equation(new IdExpr(id), 
					not(and(allConditions))));
			nb.addProperty(id);
			
		}
		
	}
	
	private static Set<NodeUID> nodesThisOneIsWaitingFor(OriginalHierarchy node) {
		Expression start = node.getConditions().get(PlexilExprDescription.START_CONDITION);
		Set<NodeUID> found = new HashSet<>();
		findNodeFinishedChecks(start, found);
		return found;
	}
	
	private static void findNodeFinishedChecks(Expression e, Set<NodeUID> found) {
		if (e instanceof ASTOperation) {
			ASTOperation oper = (ASTOperation) e;
			if (oper.getOperator() == Operator.EQ) {
				if (oper.getArguments().contains(NodeState.FINISHED)) {
					Expression other = oper.getArguments().get(0);
					if (other.equals(NodeState.FINISHED)) {
						// Oops
						other = oper.getArguments().get(1);
					}
					
					if (other instanceof GetNodeStateExpr) {
						found.add(((GetNodeStateExpr) other).getNodeUid());
					} else {
						throw new RuntimeException("Found comparison of FINISHED to "+other);
					}
				}
			}
		}
		for (Expression child : e.getArguments()) {
			findNodeFinishedChecks(child, found);
		}
	}
	
	
	private static void addFailureTracker(OriginalHierarchy node, PlanToLustre p2l,
			NodeBuilder nb) {
		// if (tracker failed) then 
		//    if (node state is inactive) then reset else stay failed
		// else if (failure) then false else pre(tracker)
		
		Expr checkForReset = ite(
				// If node state is inactive
				equal(getPlexilState(node),
						p2l.toLustre(NodeState.INACTIVE, ExprType.STATE)),
				// then set us back to false
				FALSE,
				// else keep it true, indicating failure
				TRUE
				);
		Expression invFailGuard = ILOperator.IS_FALSE.expr(
				node.getConditions().get(
						PlexilExprDescription.INVARIANT_CONDITION));
		Expression exitGuard = ILOperator.IS_TRUE.expr(
				node.getConditions().get(
						PlexilExprDescription.EXIT_CONDITION));
		Expression endGuard = ILOperator.IS_TRUE.expr(
				node.getConditions().get(
						PlexilExprDescription.END_CONDITION));
		
		
		Expr failure = or(or(p2l.toLustre(invFailGuard),
				p2l.toLustre(exitGuard)), 
				p2l.toLustre(endGuard));
		
		Expr checkFailure = ite(
				// Check for invariant or exit fail
				failure,
				// Set true if it happens
				TRUE,
				// Else keep it the same as before
				pre(getFailureTrackerId(node))
				);
		
		Expr finalStatement = arrow(FALSE, 
				ite(
				// If the tracker was true (has failed),
				pre(getFailureTrackerId(node)),
				// Check to see if the node has reset 
				checkForReset,
				// Else, check for failure and set accordingly.
				checkFailure));
		nb.addLocal(new VarDecl(getFailureTrackerName(node), NamedType.BOOL));
		nb.addEquation(eq(getFailureTrackerId(node), finalStatement));
	}
	
	private static IdExpr getFailureTrackerId(OriginalHierarchy node) {
		return getFailureTrackerId(node.getUID());
	}
	
	private static IdExpr getFailureTrackerId(NodeUID uid) {
		return id(getFailureTrackerName(uid));
	}
	
	private static String getFailureTrackerName(OriginalHierarchy node) {
		return getFailureTrackerName(node.getUID());
	}

	private static String getFailureTrackerName(NodeUID uid) {
		return uid.toCleanString()+"__had_failure";
	}

	private static ILVariable failureOf(NodeUID uid, Plan ilPlan) {
		for (ILVariable var : ilPlan.getVariables()) {
			if (var.getName().equals(".failure")
					&& var.getNodeUID().equals(uid)) {
				return var;
			}
		}
		throw new RuntimeException("Didn't find .failure for "+uid);
	}
	
	private static Expr nodeIsExecuting(NodeUID uid, PlanToLustre p2l) {
		return new BinaryExpr(
				getPlexilState(uid), 
				BinaryOp.EQUAL, 
				p2l.toLustre(NodeState.EXECUTING, ExprType.STATE));
	}
	
	private static Expr equal(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.EQUAL, right);
	}
	
	private static IdExpr getPlexilState(NodeUID uid) {
		return new IdExpr(LustreNamingConventions.getStateMapperId(uid));
	}
	
	private static IdExpr getPlexilState(OriginalHierarchy node) {
		return getPlexilState(node.getUID());
	}
	
	private static List<OriginalHierarchy> findIf55(OriginalHierarchy root) {
		if (root.getUID().getShortName().equals("if__55")) {
			List<OriginalHierarchy> ret = new ArrayList<>();
			ret.add(root);
			return ret;
		}
		
		List<OriginalHierarchy> correctList = null;
		for (OriginalHierarchy child : root.getChildren()) {
			List<OriginalHierarchy> childList = findIf55(child);
			if ( ! childList.isEmpty()) {
				correctList = childList;
				break;
			}
		}
		if (correctList == null) {
			return Collections.emptyList();
		}
		correctList.add(0, root);
		return correctList;
	}
	
}
