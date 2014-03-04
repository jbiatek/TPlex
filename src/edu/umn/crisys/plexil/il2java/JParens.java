package edu.umn.crisys.plexil.il2java;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFormatter;

public class JParens extends JExpressionImpl {

    private JExpression contents;
    
    public JParens(JExpression contents) {
        this.contents = contents;
    }
    
    @Override
    public void generate( JFormatter f ) {
        f.p('(').g(contents).p(')');
    }

}
