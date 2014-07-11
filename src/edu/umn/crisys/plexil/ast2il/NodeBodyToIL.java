package edu.umn.crisys.plexil.ast2il;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.nodebody.AssignmentBody;
import edu.umn.crisys.plexil.ast.nodebody.CommandBody;
import edu.umn.crisys.plexil.ast.nodebody.LibraryBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBody;
import edu.umn.crisys.plexil.ast.nodebody.NodeBodyVisitor;
import edu.umn.crisys.plexil.ast.nodebody.NodeListBody;
import edu.umn.crisys.plexil.ast.nodebody.UpdateBody;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.AlsoRunNodesAction;
import edu.umn.crisys.plexil.il.action.AssignAction;
import edu.umn.crisys.plexil.il.action.CommandAction;
import edu.umn.crisys.plexil.il.action.EndMacroStep;
import edu.umn.crisys.plexil.il.action.RunLibraryNodeAction;
import edu.umn.crisys.plexil.il.action.UpdateAction;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.util.Pair;

/**
 * Adds actions to the IL States according to the node body. 
 * @author jbiatek
 *
 */
public class NodeBodyToIL implements NodeBodyVisitor<Void, Void> {

		private final NodeToIL nodeToIL;
		private final Map<NodeState, State> map;
		private final Plan ilPlan;

		public NodeBodyToIL(NodeToIL nodeToIL, Map<NodeState, State> map, Plan ilPlan) {
			this.nodeToIL = nodeToIL;
			this.map = map;
			this.ilPlan = ilPlan;
		}

		@Override
		public Void visitEmpty(NodeBody empty, Void p) {
			// Nothing needs to be done.
			return null;
		}

		@Override
		public Void visitAssignment(AssignmentBody assignment, Void p) {
		    Expression lhsUntranslated = assignment.getLeftHandSide();
		    ILExpression lhsExpr = nodeToIL.resolveVariableforWriting(lhsUntranslated);
		    ILExpression rhs = nodeToIL.toIL(assignment.getRightHandSide());
		    AssignAction assignAction = new AssignAction(lhsExpr, rhs, nodeToIL.getPriority());
		    // Add the previous value now that we have the IL left hand side
		    PlexilType type = lhsUntranslated.getType();
		    if (type == PlexilType.UNKNOWN) {
		    	// If it's an array element, the type info isn't stored in the
		    	// original XML (at least, not here.) Let's try the translated
		    	// version:
		    	if (lhsExpr.getType() != PlexilType.UNKNOWN) {
		    		// Here we go. 
		    		type = lhsExpr.getType();
		    	} else if (rhs.getType() != PlexilType.UNKNOWN) {
		    		type = rhs.getType();
		    	} else {
		    		throw new RuntimeException("Find the type of this assignment."); 
		    	}
		    }
		    SimpleVar previousValue;
		    if (type.isArrayType()) {
		    	// We need an initial value for it, but it'll be overwritten anyway.
		    	// An empty array should be just fine.
		    	previousValue = new SimpleVar(".previous_value", nodeToIL.getUID(), type, new PValueList<PValue>(type));
		    } else {
		    	// Just create an UNKNOWN one. 
		    	previousValue = new SimpleVar(".previous_value", nodeToIL.getUID(), type);
		    }
		    ilPlan.addVariable(previousValue);
		    
		    AssignAction capture = new AssignAction(previousValue, lhsExpr, nodeToIL.getPriority());
		    AssignAction revert = new AssignAction(lhsExpr, previousValue, nodeToIL.getPriority());
		    
		    map.get(NodeState.EXECUTING).addEntryAction(assignAction);
		    map.get(NodeState.EXECUTING).addEntryAction(capture);
		    map.get(NodeState.EXECUTING).addEntryAction(EndMacroStep.get());
		    map.get(NodeState.FAILING).addEntryAction(revert);
		    map.get(NodeState.FAILING).addEntryAction(EndMacroStep.get());
			return null;
		}

		@Override
		public Void visitCommand(CommandBody cmd, Void p) {
		    ILExpression name = nodeToIL.toIL(cmd.getCommandName());
		    ILExpression returnTo = null;
		    if (cmd.getVarToAssign() != null) {
		    	returnTo = nodeToIL.resolveVariableforWriting(cmd.getVarToAssign());
		    }
		    List<ILExpression> args = nodeToIL.toIL(cmd.getCommandArguments());
		    CommandAction issueCmd = new CommandAction(nodeToIL.getCommandHandle(), name, args, returnTo);
		    map.get(NodeState.EXECUTING).addEntryAction(issueCmd);
		    map.get(NodeState.EXECUTING).addEntryAction(EndMacroStep.get());
		    
		    // TODO: Implement aborting commands.
//	            AbortCommandAction abort = new AbortCommandAction(getCommandHandle());
//	            map.get(NodeState.FAILING).addEntryAction(abort);
		    
			return null;
		}

		@Override
		public Void visitLibrary(LibraryBody lib, Void p) {
			if (nodeToIL.hasLibraryHandle()) {
			    // Basically the same as a list, we need to run the library node when not INACTIVE.
			    RunLibraryNodeAction runLib = new RunLibraryNodeAction(nodeToIL.getLibraryHandle());
			    for (NodeState state : NodeState.values()) {
			        if (state == NodeState.INACTIVE) continue;
			        map.get(state).addInAction(runLib);
			    }
			} else {
				// The library must have been statically included. We should 
				// treat it like a real list. Since the AST body isn't actually
				// used, passing in null is fine. 
				visitNodeList(null, null);
			}
			return null;
		}

		@Override
		public Void visitNodeList(NodeListBody list, Void p) {
		    // Each child needs to be told to transition in all states but INACTIVE.
		    List<NodeUID> childIds = new ArrayList<NodeUID>();
		    for (NodeToIL child : nodeToIL.getChildren()) {
		        childIds.add(child.getUID());
		    }
		    AlsoRunNodesAction runChildren = new AlsoRunNodesAction(childIds, ilPlan);
		    for (NodeState state : NodeState.values()) {
		        if (state == NodeState.INACTIVE) continue;
		        map.get(state).addInAction(runChildren);
		    }
			return null;
		}

		@Override
		public Void visitUpdate(UpdateBody update, Void p) {
		    UpdateAction doUpdate = new UpdateAction(nodeToIL.getUpdateHandle(), nodeToIL.getPlexilID());
		    for ( Pair<String, ASTExpression> pair : update.getUpdates()) {
		        doUpdate.addUpdatePair(pair.first, nodeToIL.toIL(pair.second));
		    }
		    map.get(NodeState.EXECUTING).addEntryAction(doUpdate);
		    map.get(NodeState.EXECUTING).addEntryAction(EndMacroStep.get());
		    
			return null;
		}
	}