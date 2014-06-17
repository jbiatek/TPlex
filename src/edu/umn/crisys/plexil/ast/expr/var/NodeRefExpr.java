package edu.umn.crisys.plexil.ast.expr.var;

import edu.umn.crisys.plexil.java.values.PlexilType;

public class NodeRefExpr extends ASTExprBase {
	
	private static NodeRefExpr PARENT = null;
	private static NodeRefExpr SIBLING = null;
	private static NodeRefExpr CHILD = null;
	private static NodeRefExpr SELF = null;
	
	public static NodeRefExpr get(NodeRef nodeRef) {
		switch (nodeRef) {
		case PARENT:
			if (PARENT == null) {
				PARENT = new NodeRefExpr(NodeRef.PARENT);
			}
			return PARENT;
		case SIBLING: 
			if (SIBLING == null) {
				SIBLING = new NodeRefExpr(NodeRef.SIBLING);
			}
			return SIBLING;
		case CHILD:
			if (CHILD == null) {
				CHILD = new NodeRefExpr(NodeRef.CHILD);
			}
			return CHILD;
		case SELF:
			if (SELF == null) {
				SELF = new NodeRefExpr(NodeRef.SELF);
			}
			return SELF;
		}
		throw new RuntimeException("Missing case: "+nodeRef);
	}
	
	public static enum NodeRef {
		PARENT,
		SIBLING,
		CHILD,
		SELF
	}
	
	private NodeRef myRef;
	
	private NodeRefExpr(NodeRef nodeRef) {
		this.myRef = nodeRef;
	}
	
	public NodeRef getNodeRef() {
		return myRef;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
		return visitor.visitNodeReference(this, param);
	}

	@Override
	public PlexilType getType() {
		return PlexilType.NODEREF;
	}

	@Override
	public String asString() {
		return myRef.toString().toLowerCase();
	}

	@Override
	public boolean isAssignable() {
		return false;
	}

}
