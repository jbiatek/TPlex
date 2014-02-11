package edu.umn.crisys.plexil.translator.il.action;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;

public interface PlexilAction {
    public void addActionToBlock(JBlock block, JCodeModel cm);
}
