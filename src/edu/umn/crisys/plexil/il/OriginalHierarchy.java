package edu.umn.crisys.plexil.il;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;

/**
 * Stores information from the original PLEXIL hierarchy. This can be used to
 * generate a "snapshot" method for testing against the original PLEXIL, since
 * the IL flattens everything out and potentially changes the entire plan. 
 * Note that some variables here may have been optimized out in the actual IL
 * plan. 
 * @author jbiatek
 *
 */
public class OriginalHierarchy {

	private NodeUID uid;
	private Optional<OriginalHierarchy> parent;
	private Map<String, ILExpr> variables = new HashMap<>();
	private Map<PlexilExprDescription, ILExpr> conditions = new HashMap<>();
	private List<OriginalHierarchy> children = new ArrayList<>();
	private Optional<LibraryVar> libraryChild = Optional.empty();
	
	public OriginalHierarchy(NodeToIL root) {
		this(root, Optional.empty());
	}
	
	@Override
	public String toString() {
		return uid.toString();
	}
	
	public OriginalHierarchy(NodeToIL translator, Optional<OriginalHierarchy> parent) {
		if (parent.isPresent() != translator.getParent().isPresent()) {
			throw new RuntimeException("Node and translator parents don't match: presence is "
					+ parent.isPresent() +" versus "+translator.getParent().isPresent());
		}
		
		this.parent = parent;
		this.uid = translator.getUID();
		// Add state, since that isn't actually a variable
		variables.put(".state", translator.getState());
		
		for (String varName : translator.getAllVariables()) {
			ILVariable v = translator.getVariable(varName);
			variables.put(v.getName(), v);
		}
		if (translator.hasLibraryHandle()) {
			libraryChild = Optional.of(translator.getLibraryHandle());
		}
		translator.translateConditions(conditions);
		for (NodeToIL child : translator.getChildren()) {
			children.add(new OriginalHierarchy(child, Optional.of(this)));
		}
		
	}

	public NodeUID getUID() {
		return uid;
	}
	
	public Optional<OriginalHierarchy> getParent() {
		return parent;
	}

	public void removeDeletedVariables(Plan p) {
		Set<String> deleteThese = new HashSet<>();
		for (String varName : variables.keySet()) {
			ILExpr storedExpr = variables.get(varName);
			if (storedExpr instanceof ILVariable) {
				// Is it still there? 
				if ( ! p.getVariables().contains(storedExpr)) {
					// Nope, it's gone.
					deleteThese.add(varName);
				}
			}
		}
		deleteThese.forEach(name -> variables.remove(name));
		
		children.forEach(child -> child.removeDeletedVariables(p));
	}
	
	/**
	 * Get the variables for this node. Note that some of them may no longer
	 * exist in the IL if you've run optimizations, run removeDeletedVariables 
	 * to get rid of them.  
	 * 
	 * @return
	 */
	public Map<String, ILExpr> getVariables() {
		return variables;
	}
	
	public Map<PlexilExprDescription, ILExpr> getConditions() {
		return conditions;
	}
	
	public boolean isSibling(OriginalHierarchy otherNode) {
		return parent.isPresent() && ! this.equals(otherNode)
				&& parent.get().getChildren().contains(otherNode);
	}
	
	public boolean isAncestorOf(OriginalHierarchy otherNode) {
		return otherNode.getAllParents().contains(this);
	}
	
	public boolean isDescendantOf(OriginalHierarchy otherNode) {
		return getAllParents().contains(otherNode);
	}
	
	public boolean isDirectParentOf(OriginalHierarchy otherNode) {
		return otherNode.parent.isPresent() 
				&& otherNode.parent.get().equals(this);
	}

	public List<OriginalHierarchy> getChildren() {
		return children;
	}
	
	public List<OriginalHierarchy> getSequentialExecutionPath() {
		List<OriginalHierarchy> ret = new ArrayList<OriginalHierarchy>();
		parent.ifPresent(p -> {
			ret.addAll(p.getSequentialExecutionPath());
			// Any siblings in our way?
			// TODO: This should look and see if we are part of a sequence
			if (conditions.containsKey(PlexilExprDescription.START_CONDITION)) {
				for (OriginalHierarchy sibling : p.children) {
					if (sibling == this) break;
					ret.add(sibling);
				}
			}
		});
		ret.add(this);
		return ret;
	}
	
	public List<OriginalHierarchy> getSiblings() {
		if (parent.isPresent()) {
			List<OriginalHierarchy> sibs = new ArrayList<>(parent.get().children);
			sibs.remove(this);
			return sibs;
		} else {
			return Collections.emptyList();
		}
	}
	
	public List<OriginalHierarchy> getAllParents() {
		List<OriginalHierarchy> ret = new ArrayList<>();
		Optional<OriginalHierarchy> pointer = parent;
		while (pointer.isPresent()) {
			ret.add(pointer.get());
			pointer = pointer.get().getParent();
		}
		return ret;
	}
	
	public OriginalHierarchy getNearestCommonAncestor(OriginalHierarchy otherNode) {
		List<OriginalHierarchy> myParents = getAllParents();
		List<OriginalHierarchy> theirParents = otherNode.getAllParents();
		
		myParents.retainAll(theirParents);
		if (myParents.isEmpty()) {
			throw new RuntimeException("Nodes "+this+" and "+otherNode
					+" share no ancestors");
		}
		return myParents.get(0);
	}
	
	public Stream<OriginalHierarchy> streamEntireHierarchy() {
		return getAllChildren().stream();
	}
	
	private List<OriginalHierarchy> getAllChildren() {
		List<OriginalHierarchy> all = new ArrayList<>();
		all.add(this);
		children.forEach(child -> all.addAll(child.getAllChildren()));
		return all;
	}

	public Optional<LibraryVar> getLibraryChild() {
		return libraryChild;
	}
	
}
