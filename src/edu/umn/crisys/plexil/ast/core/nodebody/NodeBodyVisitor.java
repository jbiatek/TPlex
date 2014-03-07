package edu.umn.crisys.plexil.ast.core.nodebody;


public interface NodeBodyVisitor<Param,Return> {

    public Return visitEmpty(NodeBody empty, Param p);
    public Return visitAssignment(AssignmentBody assign, Param p);
    public Return visitCommand(CommandBody cmd, Param p);
    public Return visitLibrary(LibraryBody lib, Param p);
    public Return visitNodeList(NodeListBody list, Param p);
    public Return visitUpdate(UpdateBody update, Param p);
    
}
