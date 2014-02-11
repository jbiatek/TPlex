package edu.umn.crisys.plexil.ast.core.expr;

import edu.umn.crisys.plexil.ast.core.visitor.ASTExprVisitor;

public interface ASTExpression extends Expression {

    public <P,R> R accept(ASTExprVisitor<P,R> visitor, P param);

}
