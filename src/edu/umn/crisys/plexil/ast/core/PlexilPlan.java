package edu.umn.crisys.plexil.ast.core;


public class PlexilPlan {

    //Command declarations
    
    //State declarations (lookups)
    
    // Library node declarations
    
    // Time scaling units subunits???
    
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
}
