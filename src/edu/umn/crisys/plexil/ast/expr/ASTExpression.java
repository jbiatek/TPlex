package edu.umn.crisys.plexil.ast.expr;

import edu.umn.crisys.plexil.ast.expr.var.ASTExprVisitor;

public interface ASTExpression extends Expression {

    public <P,R> R accept(ASTExprVisitor<P,R> visitor, P param);

}
