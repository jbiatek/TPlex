package edu.umn.crisys.plexil.ast.expr;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.expr.var.ASTExprVisitor;

public interface ASTExpression extends Expression {

    public <P,R> R accept(ASTExprVisitor<P,R> visitor, P param);

    @Override
    default public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        if (visitor instanceof ASTExprVisitor<?,?>) {
            return this.accept((ASTExprVisitor<P, R>)visitor, param);
        }
        throw new RuntimeException(visitor.getClass()+" cannot visit an AST-only node");
    }

}
