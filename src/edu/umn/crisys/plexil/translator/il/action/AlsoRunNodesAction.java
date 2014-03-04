package edu.umn.crisys.plexil.translator.il.action;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;

import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il2java.StateMachineToJava;
import edu.umn.crisys.plexil.translator.il.NodeUID;
import edu.umn.crisys.plexil.translator.il.Plan;

public class AlsoRunNodesAction implements PlexilAction {

    private List<NodeUID> nodesToRun = new LinkedList<NodeUID>();
    private Plan ilPlan;
    
    /**
     * Create an action causing the given nodes to take a step as well. Needs
     * to be given access to the Plan so that it can look up what machine
     * holds what Node.
     * @param nodes
     * @param p
     */
    public AlsoRunNodesAction(List<NodeUID> nodes, Plan p) {
        this.nodesToRun = nodes;
        ilPlan = p;
    }

    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        Set<NodeStateMachine> machinesToRun = new HashSet<NodeStateMachine>();
        for (NodeUID child : nodesToRun) {
            machinesToRun.add(ilPlan.findMachineForNode(child));
        }
        
        for (NodeStateMachine m : machinesToRun) {
            StateMachineToJava.callStepFunction(m, block);
        }
        
    }
    
    @Override
    public String toString() {
        return "Run these state machines as well: "+nodesToRun;
    }
    
}
