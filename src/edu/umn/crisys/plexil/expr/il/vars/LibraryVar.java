package edu.umn.crisys.plexil.expr.il.vars;

import java.util.Map;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.il.NodeUID;

public class LibraryVar extends ILVariable {
	
    private Expression libNodeState;
    private Expression libAndAncestorsInvariants;
    private Expression libOrAncestorsEnds;
    private Expression libOrAncestorsExits;
    private Map<String,Expression> aliases;

	public LibraryVar(String libID, NodeUID uid, Expression libNodeState,
            Map<String,Expression> aliases) {
		super(libID, uid, PlexilType.STATE);
		this.libNodeState = libNodeState;
		this.aliases = aliases;
	}

    /*
     * These have to be set after the reference is created, because they
     * very well could require a reference to *us*.  
     */
    public void setLibAndAncestorsInvariants(Expression libAndAncestorsInvariants) {
        this.libAndAncestorsInvariants = libAndAncestorsInvariants;
    }
    
    public void setLibOrAncestorsEnds(Expression libOrAncestorsEnds) {
        this.libOrAncestorsEnds = libOrAncestorsEnds;
    }
    
    public void setLibOrAncestorsExits(Expression libOrAncestorsExits) {
        this.libOrAncestorsExits = libOrAncestorsExits;
    }

    public Expression getLibraryNodeState() {
    	return libNodeState;
    }
    
    public Expression getLibAndAncestorsInvariants() {
    	return libAndAncestorsInvariants;
    }
    
    public Expression getLibOrAncestorsEnds() {
    	return libOrAncestorsEnds;
    }
    
    public Expression getLibOrAncestorsExits() {
    	return libOrAncestorsExits;
    }
    
    public Map<String, Expression> getAliases() {
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
