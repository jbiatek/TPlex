package edu.umn.crisys.plexil.translator.il.action;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;

import edu.umn.crisys.plexil.translator.il.vars.LibraryNodeReference;

public class RunLibraryNodeAction implements PlexilAction {
    
    private LibraryNodeReference node;
    
    public RunLibraryNodeAction(LibraryNodeReference node) {
        this.node = node;
    }

    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        block.add(node.directReference(cm).invoke("doMicroStep"));
    }

    @Override
    public String toString() {
        return "Run library node "+node;
    }
}
