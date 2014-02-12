package edu.umn.crisys.plexil.translator.il.action;

import java.util.LinkedList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il2java.ILExprToJava;
import edu.umn.crisys.plexil.translator.il.vars.UpdateHandleReference;
import edu.umn.crisys.util.Pair;

public class UpdateAction implements PlexilAction {

    private UpdateHandleReference handle;
    private List<Pair<String, ILExpression>> updates = new LinkedList<Pair<String, ILExpression>>();
    
    public UpdateAction(UpdateHandleReference handle) {
        this.handle = handle;
    }
    
    public List<Pair<String, ILExpression>> getUpdates() {
        return updates;
    }
    
    @Override
    public String toString() {
        return "Update : "+updates.toString();
    }
    
    public void addUpdatePair(String name, ILExpression value) {
        updates.add(new Pair<String, ILExpression>(name, value));
    }

    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        //ExternalWorld w = null;
        //w.update(node, key, value);
        
        for (Pair<String, ILExpression> p : updates) {
            block.invoke(JExpr.invoke("getWorld"), "update")
                .arg(handle.directReference(cm))
                .arg(JExpr.lit(p.first))
                .arg(ILExprToJava.toJava(p.second, cm));
        }
        block.invoke("endMacroStep");
        
    }
    
}
