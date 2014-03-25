package edu.umn.crisys.plexil.il.action;


public interface ILActionVisitor<Param,Return> {
	
	//public Return visitAbortCommand(AbortCommandAction abort, Param param);
	public Return visitAlsoRunNodes(AlsoRunNodesAction run, Param param);
	public Return visitAssign(AssignAction assign, Param param);
	public Return visitCommand(CommandAction cmd, Param param);
	public Return visitEndMacroStep(EndMacroStep end, Param param);
	public Return visitResetNode(ResetNodeAction reset, Param param);
	public Return visitRunLibraryNode(RunLibraryNodeAction lib, Param param);
	public Return visitComposite(CompositeAction composite, Param param);
	public Return visitUpdate(UpdateAction update, Param param);

}
