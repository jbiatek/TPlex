package edu.umn.crisys.plexil.runtime.plx;

import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.world.UpdateHandler;


public class UpdateHandle implements UpdateHandler {

    private SimpleCurrentNext<PBoolean> wrappedVar;
    private String nodeName;
    
    public UpdateHandle(SimpleCurrentNext<PBoolean> varToWrap, String nodeName) {
        this.nodeName = nodeName;
        this.wrappedVar = varToWrap;
    }
    
    @Override
    public void acknowledgeUpdate() {
        wrappedVar.setNext(BooleanValue.get(true));
        wrappedVar.commit();
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

}
