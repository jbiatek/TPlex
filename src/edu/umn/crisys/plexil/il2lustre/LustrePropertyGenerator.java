package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.FALSE;
import static jkind.lustre.LustreUtil.TRUE;
import static jkind.lustre.LustreUtil.and;
import static jkind.lustre.LustreUtil.arrow;
import static jkind.lustre.LustreUtil.eq;
import static jkind.lustre.LustreUtil.id;
import static jkind.lustre.LustreUtil.ite;
import static jkind.lustre.LustreUtil.not;
import static jkind.lustre.LustreUtil.or;
import static jkind.lustre.LustreUtil.pre;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.NamedType;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.common.Operation;
import edu.umn.crisys.plexil.expr.common.Operation.Operator;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.expr.il.nativebool.PlexilExprToNative.Condition;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.PlexilExprDescription;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class LustrePropertyGenerator {

	private PlanToLustre translator;
	private NodeBuilder nb;
	
	
	
	public Expr addPlainExecuteProperty(OriginalHierarchy node) {
		IdExpr exec = getPlainExecuteExpr(node);
		nb.addProperty(exec.id);
		return exec; 

	}
	
	public IdExpr getPlainExecuteExpr(OriginalHierarchy node) {
		NodeUID uid = node.getUID();
		String id = LustreNamingConventions.getStateMapperId(uid)+"_executes";
		
		if (checkForId(id)) return id(id);
		
		addLocalBool(id, new BinaryExpr(
				new IdExpr(LustreNamingConventions.getStateMapperId(uid)), 
				BinaryOp.NOTEQUAL, 
				translator.toLustre(
						NodeState.EXECUTING, ExprType.STATE))
				);

		return id(id);
	}
	
	private boolean checkForId(String varName) {
		return nb.build().locals.stream()
			.filter(decl -> decl.id.equals(varName))
			.findAny().isPresent();
	}
	
	private void addLocalBool(String id, Expr step) {
		nb.addLocal(new VarDecl(id, NamedType.BOOL));
		nb.addEquation(new Equation(id(id), step));
	}
	
	
	public IdExpr addNoFailureExecuteProperty(OriginalHierarchy node) {
		String id = LustreNamingConventions.getStateMapperId(
				node.getUID())+"_executes_no_failures";
		if (checkForId(id)) return id(id);
		
		// Make sure that parents haven't failed, and also that our node executes
		List<Expr> allConditions = new ArrayList<>();
		allConditions.add(getPlainExecuteExpr(node));
		
		Optional<OriginalHierarchy> parent = node.getParent();
		while (parent.isPresent()) {
			allConditions.add(getFailureTracker(parent.get()));
			parent = parent.get().getParent();
		}
		
		// Negate all the other stuff, in addition to having the node execute
		addLocalBool(id, and(getPlainExecuteExpr(node),
					not(and(allConditions))));
		return id(id);
	}

	private IdExpr getFailureTracker(OriginalHierarchy node) {
		String idName = node.getUID().toCleanString()+"__had_failure";
		if (checkForId(idName)) return id(idName);
		
		IdExpr id = id(idName);
		// if (tracker failed) then 
		//    if (node state is inactive) then reset else stay failed
		// else if (failure) then false else pre(tracker)
		
		Expr checkForReset = ite(
				// If node state is inactive
				equal(getPlexilState(node),
						translator.toLustre(NodeState.INACTIVE, ExprType.STATE)),
				// then set us back to false
				FALSE,
				// else keep it true, indicating failure
				TRUE
				);
		PlexilExprToNative invFailGuard = new PlexilExprToNative(
				node.getConditions().get(
						PlexilExprDescription.INVARIANT_CONDITION), 
				Condition.FALSE);
		PlexilExprToNative exitGuard = new PlexilExprToNative(
				node.getConditions().get(
						PlexilExprDescription.EXIT_CONDITION), 
				Condition.TRUE);
		PlexilExprToNative endGuard = new PlexilExprToNative(
				node.getConditions().get(
						PlexilExprDescription.END_CONDITION),
				Condition.TRUE);
		
		
		Expr failure = or(or(translator.toLustre(invFailGuard),
				translator.toLustre(exitGuard)), 
				translator.toLustre(endGuard));
		
		Expr checkFailure = ite(
				// Check for invariant or exit fail
				failure,
				// Set true if it happens
				TRUE,
				// Else keep it the same as before
				pre(id)
				);
		
		Expr finalStatement = arrow(FALSE, 
				ite(
				// If the tracker was true (has failed),
				pre(id),
				// Check to see if the node has reset 
				checkForReset,
				// Else, check for failure and set accordingly.
				checkFailure));
		nb.addLocal(new VarDecl(idName, NamedType.BOOL));
		nb.addEquation(eq(id, finalStatement));
		return id;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static Set<NodeUID> nodesThisOneIsWaitingFor(OriginalHierarchy node) {
		Expression start = node.getConditions().get(PlexilExprDescription.START_CONDITION);
		Set<NodeUID> found = new HashSet<>();
		findNodeFinishedChecks(start, found);
		return found;
	}
	
	private static void findNodeFinishedChecks(Expression e, Set<NodeUID> found) {
		if (e instanceof Operation) {
			Operation oper = (Operation) e;
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
	

}
