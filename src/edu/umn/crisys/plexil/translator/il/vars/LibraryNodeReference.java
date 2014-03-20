package edu.umn.crisys.plexil.translator.il.vars;

import java.util.HashMap;
import java.util.Map;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il2java.ActionToJava;
import edu.umn.crisys.plexil.il2java.ILExprToJava;
import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.LibraryInterface;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;

/**
 * This represents the state of a node that was pulled in from a 
 * library. 
 * 
 * TODO: Man, this class is a mess. Fix it. 
 */
public class LibraryNodeReference extends RHSVariable {

    private ILExpression libNodeState;
    private ILExpression libAndAncestorsInvariants;
    private ILExpression libOrAncestorsEnds;
    private ILExpression libOrAncestorsExits;
    private Map<String,ILExpression> aliases;
    private Map<String,String> idToClassName = new HashMap<String, String>();
    private String libPlexilID;
    private NodeUID nodeUID;
    
    public LibraryNodeReference(NodeUID nodeUID, String libPlexilID, ILExpression libNodeState,
            Map<String,ILExpression> aliases) {
        this.nodeUID = nodeUID;
        this.libPlexilID = libPlexilID;
        this.libNodeState = libNodeState;
        this.aliases = aliases;
    }
    
    /*
     * These have to be settable after the reference is created, because they
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
    
    @Override
    public PlexilType getType() {
        return PlexilType.STATE;
    }
    
    @Override
    public String toString() {
        return "Library node call to ID "+libPlexilID;
    }
    
    public void setIdToClassNameMap(Map<String,String> newMap) {
    	this.idToClassName = newMap;
    }

    @Override
    public void addVarToClass(JDefinedClass clazz) {
        JCodeModel cm = clazz.owner();
        // We're actually adding the entire node, and creating the interface
        // for aliases, if any.
        JDefinedClass anonClass = cm.anonymousClass(cm.ref(LibraryInterface.class));
        
        // getWorld()
        anonClass.method(JMod.PUBLIC, cm.ref(JavaPlan.class), "getParentPlan")
            // http://stackoverflow.com/questions/56974/keyword-for-the-outer-class-from-an-anonymous-inner-class
            .body()._return(clazz.staticRef("this"));

        // evalParentState() needs to return our library node's state
        anonClass.method(JMod.PUBLIC, cm.ref(NodeState.class), "evalParentState")
            .body()._return(ILExprToJava.toJava(libNodeState, cm));
        
        // evalAncestorInvariant() returns our + ancestor's invariants
        JMethod evalInv = anonClass.method(JMod.PUBLIC, cm.ref(PBoolean.class), "evalAncestorInvariant");
        evalInv.body().decl(cm.ref(PBoolean.class), "temp");
        evalInv.body()._return(ILExprToJava.toJava(libAndAncestorsInvariants, cm));
        
        // evalAncestorEnd() returns our + ancestor's ends
        JMethod evalEnd = anonClass.method(JMod.PUBLIC, cm.ref(PBoolean.class), "evalAncestorEnd");
        evalEnd.body().decl(cm.ref(PBoolean.class), "temp");
        evalEnd.body()._return(ILExprToJava.toJava(libOrAncestorsEnds, cm));
        
        // evalAncestorExit() returns our + ancestor's exits
        JMethod evalExit = anonClass.method(JMod.PUBLIC, cm.ref(PBoolean.class), "evalAncestorExit");
        evalExit.body().decl(cm.ref(PBoolean.class), "temp");
        evalExit.body()._return(ILExprToJava.toJava(libOrAncestorsExits, cm));
        
        
        if ( ! aliases.isEmpty()) {
            // More work to handle aliases
            JMethod performAssignment = anonClass.method(JMod.PUBLIC, cm.VOID, "performAssignment");
            JVar varNamePA = performAssignment.param(cm.ref(String.class), "varName");
            JVar value = performAssignment.param(cm.ref(PValue.class), "value");
            JVar priority = performAssignment.param(cm.INT, "priority");

            JMethod getValue = anonClass.method(JMod.PUBLIC, cm.ref(PValue.class), "getValue");
            JVar varNameGV = getValue.param(cm.ref(String.class), "varName");

            JConditional condAssign = null;
            JConditional condGetter = null;

            for (String alias : aliases.keySet()) {

                // Do we need to make this assignable, or just readable?

                ILExpression target = aliases.get(alias);
                if (target instanceof IntermediateVariable) {
                    IntermediateVariable lhs = (IntermediateVariable) target;
                    // Need to add this to both methods
                    if (condAssign == null) {
                        condAssign = performAssignment.body()
                        ._if(varNamePA.invoke("equals").arg(JExpr.lit(alias)));
                    } else {
                        condAssign = condAssign
                        ._elseif(varNamePA.invoke("equals").arg(JExpr.lit(alias)));
                    }
                    ActionToJava.addAssignmentToBlock(condAssign._then(), lhs, value, priority, cm);
                }

                if (condGetter == null) {
                    condGetter = getValue.body()
                    ._if(varNameGV.invoke("equals").arg(JExpr.lit(alias)));
                } else {
                    condGetter = condGetter
                    ._elseif(varNameGV.invoke("equals").arg(JExpr.lit(alias)));
                }
                condGetter._then()._return(
                        ILExprToJava.toJava(aliases.get(alias), cm));

            }
            if (condAssign == null) {
            	// We never set one, so make the body just an exception throw.
            	performAssignment.body()._throw(JExpr._new(cm.ref(RuntimeException.class))
            			.arg(JExpr.lit("No variables were specified as writable")));
            } else {
            	condAssign._else()._throw(JExpr._new(cm.ref(RuntimeException.class))
            			.arg(JExpr.lit("I don't know where to assign for ").plus(varNamePA)));
            }
            // That shouldn't be a problem here, though, since there must have
            // been at least 1 alias.
            condGetter._else()._throw(JExpr._new(cm.ref(RuntimeException.class))
                    .arg(JExpr.lit("I don't know about a var named ").plus(varNameGV)));
        }

        String className = NameUtils.clean(libPlexilID);
        if (idToClassName.containsKey(libPlexilID)) {
        	className = idToClassName.get(libPlexilID);
        }
        
        clazz.field(JMod.PRIVATE, clazz.owner().ref(JavaPlan.class), 
                getFieldName(),
                JExpr._new(cm.directClass(className))
                .arg(JExpr._new(anonClass))); 
    }

    private String getFieldName() {
        return "LIB_INCLUDE___"+nodeUID.toCleanString();
    }
    
    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        throw new RuntimeException("Can't assign to state variables");
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return directReference(cm).invoke("getRootNodeState");
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        // Do nothing, states don't reset.
    }

    @Override
    public String getNameForTesting() {
        return null;
    }

    @Override
    public JExpression directReference(JCodeModel cm) {
        return JExpr.ref(getFieldName());
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
        return visitor.visitVariable(this, param);
    }

}
