package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.java.values.PlexilType;

public interface IntermediateVariable extends ILExpression {

    public abstract PlexilType getType();
    
    public abstract void addVarToClass(JDefinedClass clazz);
    
    public abstract void addAssignment(JExpression rhs, JExpression priority, JBlock block, JCodeModel cm);
    
    public abstract JExpression rhs(JCodeModel cm);
    
    public abstract void reset(JBlock block, JCodeModel cm);
    
    /**
     * Get the local name of this Variable. If you are just a wrapper for a 
     * "real" variable, or just don't want to be included for regression 
     * testing, return null. Since it's being embedded in a tree representing
     * the state of things, you don't need to worry about NodeUID or anything.
     * @return
     */
    public abstract String getNameForTesting();
    
    /**
     * Get a direct reference to the Java object implementing this variable.
     * This may not be allowed depending on the type of variable. 
     * @param cm
     * @return
     */
    public abstract JExpression directReference(JCodeModel cm);
    
    
}
