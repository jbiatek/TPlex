package edu.umn.crisys.plexil.jkind.search;

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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.NamedType;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.il.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.expr.il.nativebool.PlexilExprToNative.Condition;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.OriginalHierarchy;
import edu.umn.crisys.plexil.il.PlexilExprDescription;
import edu.umn.crisys.plexil.il2lustre.LustreNamingConventions;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.util.Util;

public class NodeExecutesNoParentFailProperty extends TraceProperty {

	private OriginalHierarchy node;
	
	public NodeExecutesNoParentFailProperty(OriginalHierarchy node) {
		this.node = node;
	}

	private static Expr equal(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.EQUAL, right);
	}
	
	private static boolean checkForId(String varName, NodeBuilder nb) {
		return nb.build().locals.stream()
			.filter(decl -> decl.id.equals(varName))
			.findAny().isPresent();
	}
	
	private static IdExpr getPlexilState(OriginalHierarchy node) {
		return getPlexilState(node.getUID());
	}
	
	private static IdExpr getPlexilState(NodeUID uid) {
		return new IdExpr(LustreNamingConventions.getStateMapperId(uid));
	}
	
	private void addLocalBool(String id, NodeBuilder nb, Expr step) {
		nb.addLocal(new VarDecl(id, NamedType.BOOL));
		nb.addEquation(new Equation(id(id), step));
	}


	private Expr getThisNodeExecuting(NodeUID uid, PlanToLustre translator, NodeBuilder nb) {
		return new BinaryExpr(
				new IdExpr(LustreNamingConventions.getStateMapperId(uid)), 
				BinaryOp.EQUAL, 
				translator.toLustre(
						NodeState.EXECUTING, ExprType.STATE));

	}

	private IdExpr getThisNodeHadAFailure(OriginalHierarchy node, NodeBuilder nb, PlanToLustre translator) {
		String idName = node.getUID().toCleanString()+"__had_failure";
		if (checkForId(idName, nb)) return id(idName);
		
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

	
	
	@Override
	public void addProperty(NodeBuilder nb, PlanToLustre translator) {
		// Make sure that parents haven't failed, and also that our node executes
		List<Expr> allConditions = new ArrayList<>();
		allConditions.add(getThisNodeExecuting(node.getUID(), translator, nb));
		
		Optional<OriginalHierarchy> parent = node.getParent();
		while (parent.isPresent()) {
			allConditions.add(
					not(getThisNodeHadAFailure(parent.get(), nb, translator)));
			parent = parent.get().getParent();
		}
		
		String id = getPropertyId();
		// This condition can never happen, right! It's impossible!
		addLocalBool(id, nb, not(and(allConditions)));
		nb.addProperty(id);
	}
	
	private Set<NodeExecutesNoParentFailProperty> filter(Set<TraceProperty> all) {
		return all.stream()
				.filter(prop -> prop instanceof NodeExecutesNoParentFailProperty)
				.map(prop -> (NodeExecutesNoParentFailProperty) prop)
				.collect(Collectors.toSet());
	}

	@Override
	public String getPropertyId() {
		return LustreNamingConventions.getStateMapperId(
				node.getUID())+"_executes_no_failures";
	}

	@Override
	public Set<TraceProperty> createInitialGoals() {
		// Try to just go for ourselves the first time around, maybe we're easy
		return Util.asHashSet(this);
	}

	@Override
	public Set<TraceProperty> createIntermediateGoalsFrom(
			IncrementalTrace trace, PlanToLustre translator) {
		Set<NodeExecutesNoParentFailProperty> fellows = filter(trace.getProperties());
		
		return fellows.stream()
				.map(fellow -> {
					if (fellow.node.isAncestorOf(node)) {
						// Try to execute any of their children
						return fellow.node.streamEntireHierarchy()
								.collect(Collectors.toSet());
					} else if (fellow.node.isSibling(node)) {
						return node.getParent().map(
									parent -> new HashSet<>(parent.getChildren()))
									.orElse(new HashSet<OriginalHierarchy>());
					} else {
						return new HashSet<OriginalHierarchy>();
					}
				})
				.flatMap(Collection::stream)
				.map(node -> new NodeExecutesNoParentFailProperty(node))
				.collect(Collectors.toSet());
		/*
		List<OriginalHierarchy> myParents = node.getAllParents();
		return fellows.stream()
				.map(fellow -> {
					// Is there a clear path for getting closer to us?
					if (fellow.node.isAncestorOf(node)) {
						// One of our ancestors just got executed, so try to
						// get one closer.
						int fellowIndex = myParents.indexOf(fellow.node);
						if (fellowIndex == 0) {
							// Oh, this is our direct parent. We'll report ourselves
							// as reachable, but we can also try
							// for each of our siblings in case of sequences.
							return node.getSiblings().stream()
									.map(sibling -> new NodeExecutesNoParentFailProperty(sibling))
									.collect(Collectors.toSet());
						} else if (fellowIndex == -1) throw new RuntimeException("Should be in there somewhere");
						
						// Try to reach all of our parents
						return myParents.stream()
								.map(parent -> new NodeExecutesNoParentFailProperty(parent))
								.collect(Collectors.toSet());
					} else {
						// It's not an ancestor of us, not much else to do.
						return Collections.<TraceProperty>emptySet();
					}
				})
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
				*/
		
	}

	@Override
	public boolean traceLooksReachable(IncrementalTrace trace) {
		// Find other "node executes no parent fails":
		Set<NodeExecutesNoParentFailProperty> fellows = filter(trace.getProperties());
		// Does this trace execute either our parent or a sibling?
		return fellows.stream()
			.filter(fellow -> 
					fellow.node.isDirectParentOf(node) 
					|| fellow.node.isSibling(node))
			.findAny().isPresent();
	}

	@Override
	public int hashCode() {
		return node.hashCode() + 1;
	}

	@Override
	public boolean equals(TraceProperty p) {
		return p instanceof NodeExecutesNoParentFailProperty &&
			((NodeExecutesNoParentFailProperty) p).node.equals(node);
	}

	@Override
	public String toString() {
		return "<execute node "+node+" with no failures>";
	}
	
}
