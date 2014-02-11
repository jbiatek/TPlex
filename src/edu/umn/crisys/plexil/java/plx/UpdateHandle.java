package edu.umn.crisys.plexil.java.plx;

import edu.umn.crisys.plexil.java.world.UpdateHandler;


public class UpdateHandle implements UpdateHandler {

    private boolean updateComplete = false;
    private String nodeName;
    
    public UpdateHandle(String nodeName) {
        this.nodeName = nodeName;
    }
    
    @Override
    public void acknowledgeUpdate() {
        updateComplete = true;
    }
    
    public boolean isUpdateComplete() {
        return updateComplete;
    }
    
    public void reset() {
        updateComplete = false;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

}
