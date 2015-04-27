package edu.umn.crisys.plexil.il.action;

public abstract class ILActionFilter<Param> implements ILActionVisitor<Param, Boolean> {

	@Override
	public Boolean visitAlsoRunNodes(AlsoRunNodesAction run, Param param) {
		return true;
	}

	@Override
	public Boolean visitAssign(AssignAction assign, Param param) {
		return true;
	}

	@Override
	public Boolean visitCommand(CommandAction cmd, Param param) {
		return true;
	}

	@Override
	public Boolean visitEndMacroStep(EndMacroStep end, Param param) {
		return true;
	}

	@Override
	public Boolean visitRunLibraryNode(RunLibraryNodeAction lib, Param param) {
		return true;
	}

	@Override
	public Boolean visitComposite(CompositeAction composite, Param param) {
		composite.getActions().removeIf(a -> ! a.accept(this, param));
		return ! composite.getActions().isEmpty();
	}

	@Override
	public Boolean visitUpdate(UpdateAction update, Param param) {
		return true;
	}

}
