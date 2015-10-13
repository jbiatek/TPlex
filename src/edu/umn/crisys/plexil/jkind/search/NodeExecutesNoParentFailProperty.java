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
import jkind.lustre.visitors.PrettyPrintVisitor;
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
		Expr finalCondition = not(and(allConditions));
		addLocalBool(id, nb, finalCondition);
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
		// These are the goals to try if we don't think that we might be reachable.
		
		// We should try to get to any ancestors that haven't been executed,
		Set<NodeExecutesNoParentFailProperty> fellows = filter(trace.getProperties());
		
		List<OriginalHierarchy> mySequencePath = node.getSequentialExecutionPath();
		return (Set<TraceProperty>) fellows.stream()
				.map(fellow -> {
					if (mySequencePath.contains(fellow.node)) {
						// This one looks like it could move toward us.
						// Ask for the rest of our path if possible.
						Set<TraceProperty> customPath = new HashSet<>();
						int theirIndex = mySequencePath.indexOf(fellow.node);
						for (int i = theirIndex+1; i < mySequencePath.size(); i++) {
							customPath.add(
									new NodeExecutesNoParentFailProperty(mySequencePath.get(i)));
						}
						return customPath;
					} else {
						// This isn't an ancestor, or a predecessor of an 
						// ancestor. It's probably just doing something else.
						return Collections.<TraceProperty>emptySet();
					}
				})
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	
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
