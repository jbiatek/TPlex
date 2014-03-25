package edu.umn.crisys.plexil.il.vars;

import java.util.Map;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class LibraryVar extends ILVariable {
	
    private ILExpression libNodeState;
    private ILExpression libAndAncestorsInvariants;
    private ILExpression libOrAncestorsEnds;
    private ILExpression libOrAncestorsExits;
    private Map<String,ILExpression> aliases;

	public LibraryVar(String libID, NodeUID uid, ILExpression libNodeState,
            Map<String,ILExpression> aliases) {
		super(libID, uid, PlexilType.STATE);
		this.libNodeState = libNodeState;
		this.aliases = aliases;
	}

    /*
     * These have to be set after the reference is created, because they
     * very well could require a reference to *us*.  
     */
    public void setLibAndAncestorsInvariants(ILExpression libAndAncestorsInvariants) {
        this.libAndAncestorsInvariants = libAndAncestorsInvariants;
    }
    
    public void setLibOrAncestorsEnds(ILExpression libOrAncestorsEnds) {
        this.libOrAncestorsEnds = libOrAncestorsEnds;
    }
    
    public void setLibOrAncestorsExits(ILExpression libOrAncestorsExits) {
        this.libOrAncestorsExits = libOrAncestorsExits;
    }

    public ILExpression getLibraryNodeState() {
    	return libNodeState;
    }
    
    public ILExpression getLibAndAncestorsInvariants() {
    	return libAndAncestorsInvariants;
    }
    
    public ILExpression getLibOrAncestorsEnds() {
    	return libOrAncestorsEnds;
    }
    
    public ILExpression getLibOrAncestorsExits() {
    	return libOrAncestorsExits;
    }
    
    public Map<String, ILExpression> getAliases() {
    	return aliases;
    }
    
    public String getLibraryPlexilID() {
    	return getName();
    }
	
	@Override
	public <P, R> R accept(ILVarVisitor<P, R> visitor, P param) {
		return visitor.visitLibrary(this, param);
	}

	@Override
	public boolean isAssignable() {
		return false;
	}

    
}
