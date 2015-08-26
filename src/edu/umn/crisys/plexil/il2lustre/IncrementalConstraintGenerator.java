package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.Plan;
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
		
		List<OriginalHierarchy> parentChain = 
				findIf55(ilPlan.getOriginalHierarchy());
		
		if (parentChain.isEmpty()) {
			throw new RuntimeException("Couldn't find If 55");
		}
		
		for (OriginalHierarchy node : parentChain) {
			NodeUID uid = node.getUID();
			String id = LustreNamingConventions.getStateMapperId(uid)+"_executes";
			nb.addLocal(new VarDecl(id, NamedType.BOOL));
			// This state could never be executing, right? Prove me wrong!
			nb.addEquation(new Equation(new IdExpr(id), 
					nodeIsntExecuting(uid, p2l)));
			nb.addProperty(id);
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
	
	private static Expr nodeIsntExecuting(NodeUID uid, PlanToLustre p2l) {
		return new BinaryExpr(
				new IdExpr(LustreNamingConventions.getStateMapperId(uid)), 
				BinaryOp.NOTEQUAL, 
				p2l.toLustre(NodeState.EXECUTING, ExprType.STATE));
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
