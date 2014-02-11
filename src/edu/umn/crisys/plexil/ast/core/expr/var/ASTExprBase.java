package edu.umn.crisys.plexil.ast.core.expr.var;

import edu.umn.crisys.plexil.ast.core.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.core.visitor.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.core.visitor.CommonExprVisitor;

public abstract class ASTExprBase implements ASTExpression {

    @Override
    public final <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        if (visitor instanceof ASTExprVisitor<?,?>) {
            return this.accept((ASTExprVisitor<P, R>)visitor, param);
        }
        throw new RuntimeException(visitor.getClass()+" cannot visit an AST-only node");
    }
}
