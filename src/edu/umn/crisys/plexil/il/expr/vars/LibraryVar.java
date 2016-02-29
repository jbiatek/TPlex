package edu.umn.crisys.plexil.il.expr.vars;

import java.util.Map;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILType;

public class LibraryVar extends ILVariable {
	
    private ILExpr libNodeState;
    private ILExpr libAndAncestorsInvariants;
    private ILExpr libOrAncestorsEnds;
    private ILExpr libOrAncestorsExits;
    private Map<String,ILExpr> aliases;

	public LibraryVar(String libID, NodeUID uid, ILExpr libNodeState,
            Map<String,ILExpr> aliases) {
		super(libID, uid, ILType.STATE);
		this.libNodeState = libNodeState;
		this.aliases = aliases;
	}

    /*
     * These have to be set after the reference is created, because they
     * very well could require a reference to *us*.  
     */
    public void setLibAndAncestorsInvariants(ILExpr libAndAncestorsInvariants) {
        this.libAndAncestorsInvariants = libAndAncestorsInvariants;
    }
    
    public void setLibOrAncestorsEnds(ILExpr libOrAncestorsEnds) {
        this.libOrAncestorsEnds = libOrAncestorsEnds;
    }
    
    public void setLibOrAncestorsExits(ILExpr libOrAncestorsExits) {
        this.libOrAncestorsExits = libOrAncestorsExits;
    }

    public ILExpr getLibraryNodeState() {
    	return libNodeState;
    }
    
    public ILExpr getLibAndAncestorsInvariants() {
    	return libAndAncestorsInvariants;
    }
    
    public ILExpr getLibOrAncestorsEnds() {
    	return libOrAncestorsEnds;
    }
    
    public ILExpr getLibOrAncestorsExits() {
    	return libOrAncestorsExits;
    }
    
    public Map<String, ILExpr> getAliases() {
    	return aliases;
    }
    
    public String getLibraryPlexilID() {
    	return getName();
    }
	
	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public boolean isAssignable() {
		return false;
	}

    
}
