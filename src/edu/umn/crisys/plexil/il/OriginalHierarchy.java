package edu.umn.crisys.plexil.il;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.expr.Expression;
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
	private Map<String, Expression> variables = new HashMap<>();
	private Map<PlexilExprDescription, Expression> conditions = new HashMap<>();
	private List<OriginalHierarchy> children = new ArrayList<>();
	private Optional<LibraryVar> libraryChild = Optional.empty();
	
	public OriginalHierarchy(NodeToIL root) {
		this(root, Optional.empty());
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
			Expression storedExpr = variables.get(varName);
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
	public Map<String, Expression> getVariables() {
		return variables;
	}
	
	public Map<PlexilExprDescription, Expression> getConditions() {
		return conditions;
	}

	public List<OriginalHierarchy> getChildren() {
		return children;
	}

	public Optional<LibraryVar> getLibraryChild() {
		return libraryChild;
	}
	
}
