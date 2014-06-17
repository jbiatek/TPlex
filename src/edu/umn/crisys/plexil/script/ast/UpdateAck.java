package edu.umn.crisys.plexil.script.ast;

public class UpdateAck implements Event {

    private String nodeName;
    
    public UpdateAck(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public String getNodeName() {
    	return nodeName;
    }

    @Override
	public boolean equals(Object other) {
		if ( ! (other instanceof UpdateAck)) {
			return false;
		}
		UpdateAck o = (UpdateAck) other;
		return o.nodeName.equals(this.nodeName);
	}
	
	@Override
	public int hashCode() {
		return nodeName.hashCode();
	}

	@Override
	public <P, R> R accept(ScriptEventVisitor<P, R> v, P param) {
		return v.visitUpdateAck(this, param);
	}


    
}