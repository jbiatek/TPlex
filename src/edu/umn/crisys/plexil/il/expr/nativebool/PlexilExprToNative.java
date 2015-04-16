package edu.umn.crisys.plexil.il.expr.nativebool;

import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class PlexilExprToNative implements NativeExpr {

	public static PlexilExprToNative isTrue(ILExpression e) {
		return new PlexilExprToNative(e, Condition.TRUE);
	}
	
	public static PlexilExprToNative isFalse(ILExpression e) {
		return new PlexilExprToNative(e, Condition.FALSE);
	}
	
	public static PlexilExprToNative isUnknown(ILExpression e) {
		return new PlexilExprToNative(e, Condition.UNKNOWN);
	}
	
	public static PlexilExprToNative isNotTrue(ILExpression e) {
		return new PlexilExprToNative(e, Condition.NOTTRUE);
	}
	
	public static PlexilExprToNative isNotFalse(ILExpression e) {
		return new PlexilExprToNative(e, Condition.NOTFALSE);
	}
	
	public static PlexilExprToNative isKnown(ILExpression e) {
		return new PlexilExprToNative(e, Condition.KNOWN);
	}
	
	public static enum Condition {
        TRUE,
        FALSE,
        UNKNOWN,
        NOTTRUE,
        NOTFALSE,
        KNOWN;
        
        public boolean checkValue(PValue v) {
            if ( ! (v instanceof PBoolean)) {
                throw new RuntimeException("Conditions should only be boolean, not "+v.getType());
            }
            PBoolean value = (PBoolean) v;
            switch (this) {
            case TRUE:
                return value.isTrue();
            case FALSE:
                return value.isFalse();
            case UNKNOWN:
                return value.isUnknown();
            case NOTTRUE:
                return value.isNotTrue();
            case NOTFALSE:
                return value.isNotFalse();
            case KNOWN:
                return value.isKnown();
            }
            throw new RuntimeException("Add this case to checkValue(): "+this);
        }
        
        @Override
        public String toString() {
            switch (this) {
            case TRUE:
                return "T";
            case FALSE:
                return "F";
            case UNKNOWN:
                return "U";
            case NOTTRUE:
                return "F, U";
            case NOTFALSE:
                return "T, U";
            case KNOWN:
                return "T, F";
            }
            return "some unknown condition";
        }
        
        public String getJavaMethodName() {
        	switch (this) {
        	case TRUE:
        		return "isTrue";
        	case FALSE:
        		return "isFalse";
        	case UNKNOWN:
        		return "isUnknown";
        	case NOTTRUE:
        		return "isNotTrue";
        	case NOTFALSE:
        		return "isNotFalse";
        	case KNOWN:
        		return "isKnown";
        	}
        	return "some unknown condition";
        }
        
    }
	
	
	private ILExpression plexilExpr;
	private Condition condition;
	
	public PlexilExprToNative(ILExpression plexilExpr, Condition condition) {
		PlexilType.BOOLEAN.typeCheck(plexilExpr.getType());
		this.plexilExpr = plexilExpr;
		this.condition = condition;
	}

	public ILExpression getPlexilExpr() {
		return plexilExpr;
	}
	
	public void setPlexilExpr(ILExpression expr) {
		this.plexilExpr = expr;
	}

	public Condition getCondition() {
		return condition;
	}

	@Override
	public <P, R> R accept(NativeExprVisitor<P, R> visitor, P param) {
		return visitor.visitPlexilExprToNative(this, param);
	}
	
	@Override 
	public String toString() {
		return "("+plexilExpr+")."+condition.getJavaMethodName()+"()";
	}

}
