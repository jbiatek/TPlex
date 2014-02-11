package edu.umn.crisys.plexil.ast.core.visitor;

import edu.umn.crisys.plexil.ast.core.node.AssignmentBody;
import edu.umn.crisys.plexil.ast.core.node.CommandBody;
import edu.umn.crisys.plexil.ast.core.node.LibraryBody;
import edu.umn.crisys.plexil.ast.core.node.NodeBody;
import edu.umn.crisys.plexil.ast.core.node.NodeListBody;
import edu.umn.crisys.plexil.ast.core.node.UpdateBody;

public interface NodeBodyVisitor<Param,Return> {

    public Return visitEmpty(NodeBody empty, Param p);
    public Return visitAssignment(AssignmentBody assign, Param p);
    public Return visitCommand(CommandBody cmd, Param p);
    public Return visitLibrary(LibraryBody lib, Param p);
    public Return visitNodeList(NodeListBody list, Param p);
    public Return visitUpdate(UpdateBody update, Param p);
    
}
