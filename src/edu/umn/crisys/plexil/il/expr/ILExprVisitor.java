package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeRefExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.il.vars.ILVarVisitor;

public interface ILExprVisitor<Param, Return> 
extends CommonExprVisitor<Param, Return>, ILVarVisitor<Param, Return>
{

    public default Return visitVariable(UnresolvedVariableExpr expr, Param param) {
    	throw new RuntimeException("This is an AST expression: "+expr);
    }
    public default Return visitNodeReference(NodeRefExpr ref, Param param) {
    	throw new RuntimeException("This is an AST expression: "+ref);

    }
    public default Return visitDefaultEnd(DefaultEndExpr end, Param param) {
    	throw new RuntimeException("This is an AST expression: "+end);

    }
    public default Return visitNodeTimepoint(NodeTimepointExpr timept, Param param) {
    	throw new RuntimeException("This is an AST expression: "+timept);
    }

    
}
