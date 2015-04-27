package edu.umn.crisys.plexil.il.action;

public class ActionVisitorAdapter<Param> implements
		ILActionVisitor<Param, Void> {

	@Override
	public Void visitAlsoRunNodes(AlsoRunNodesAction run, Param param) {
		return null;
	}

	@Override
	public Void visitAssign(AssignAction assign, Param param) {
		return null;
	}

	@Override
	public Void visitCommand(CommandAction cmd, Param param) {
		return null;
	}

	@Override
	public Void visitEndMacroStep(EndMacroStep end, Param param) {
		return null;
	}

	@Override
	public Void visitRunLibraryNode(RunLibraryNodeAction lib, Param param) {
		return null;
	}

	@Override
	public Void visitComposite(CompositeAction composite, Param param) {
		return null;
	}

	@Override
	public Void visitUpdate(UpdateAction update, Param param) {
		return null;
	}

}
