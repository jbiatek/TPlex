package edu.umn.crisys.plexil.ast.core;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.globaldecl.CommandDecl;
import edu.umn.crisys.plexil.ast.core.node.Node;

public class PlexilPlan {

    //Command declarations
	private List<CommandDecl> commandDecls = new ArrayList<CommandDecl>();
    
    //State declarations (lookups)
    
    // Library node declarations
    
    // Time scaling units subunits???
	
	private int timeScalingUnitsSubunits;
    
    private String name;
    private Node root;
    public Node getRootNode() { return root; }
    public void setRootNode(Node root) {
        this.root = root;
    }
    
    public PlexilPlan(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getFullPrintout() {
        NodePrinter p = new NodePrinter(root);
        return p.prettyPrint();
    }
    
    public List<CommandDecl> getCommandDeclarations() {
    	return commandDecls;
    }
    
	public void setTimeScalingUnitsSubunits(int value) {
		timeScalingUnitsSubunits = value;
	}
}
