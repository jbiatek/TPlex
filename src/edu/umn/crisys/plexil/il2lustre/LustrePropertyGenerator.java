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
import java.util.List;
import java.util.Optional;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.NamedType;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.PlexilExprDescription;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILOperator;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class LustrePropertyGenerator {

	private PlanToLustre translator;
	private NodeBuilder nb;
	
	public LustrePropertyGenerator(PlanToLustre translator, NodeBuilder nb) {
		this.translator = translator;
		this.nb = nb;
	}
	
	public Expr addPlainExecuteProperty(NodeUID uid) {
		IdExpr exec = getPlainExecuteExpr(uid);
		nb.addProperty(exec.id);
		return exec; 

	}
	
	public IdExpr getPlainExecuteExpr(NodeUID uid) {
		String id = LustreNamingConventions.getStateMapperId(uid)+"_executes";
		
		if (checkForId(id)) return id(id);
		
		addLocalBool(id, new BinaryExpr(
				new IdExpr(LustreNamingConventions.getStateMapperId(uid)), 
				BinaryOp.NOTEQUAL, 
				translator.toLustre(
						NodeState.EXECUTING, ILType.STATE))
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
	
	public static String getNoFaiureExecuteId(OriginalHierarchy node) {
		return LustreNamingConventions.getStateMapperId(
				node.getUID())+"_executes_no_failures";
	}
	
	public IdExpr addNoFailureExecuteProperty(OriginalHierarchy node) {
		String id = getNoFaiureExecuteId(node);
		if (checkForId(id)) return id(id);
		
		// Make sure that parents haven't failed, and also that our node executes
		List<Expr> allConditions = new ArrayList<>();
		allConditions.add(getPlainExecuteExpr(node.getUID()));
		
		Optional<OriginalHierarchy> parent = node.getParent();
		while (parent.isPresent()) {
			allConditions.add(getFailureTracker(parent.get()));
			parent = parent.get().getParent();
		}
		
		// Negate all the other stuff, in addition to having the node execute
		addLocalBool(id, and(getPlainExecuteExpr(node.getUID()),
					not(and(allConditions))));
		nb.addProperty(id);
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
						translator.toLustre(NodeState.INACTIVE, ILType.STATE)),
				// then set us back to false
				FALSE,
				// else keep it true, indicating failure
				TRUE
				);
		ILExpr invFailGuard = ILOperator.IS_FALSE.expr(
				node.getConditions().get(
						PlexilExprDescription.INVARIANT_CONDITION));
		ILExpr exitGuard = ILOperator.IS_TRUE.expr(
				node.getConditions().get(
						PlexilExprDescription.EXIT_CONDITION));
		ILExpr endGuard = ILOperator.IS_TRUE.expr(
				node.getConditions().get(
						PlexilExprDescription.END_CONDITION));
		
		
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
