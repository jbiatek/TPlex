package edu.umn.crisys.plexil.java.plx;

import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.world.UpdateHandler;


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
