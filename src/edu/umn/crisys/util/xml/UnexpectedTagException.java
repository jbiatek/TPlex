package edu.umn.crisys.util.xml;

import javax.xml.stream.events.XMLEvent;

public class UnexpectedTagException extends RuntimeException {

    private static final long serialVersionUID = -3672341180630831826L;

    public UnexpectedTagException(XMLEvent unexpected) {
        super("Did not expect a "+unexpected+" tag here at "+unexpected.getLocation());
    }
    
    public UnexpectedTagException(XMLEvent unexpected, String expected) {
        super("Expected a "+expected+" tag here, not "+unexpected+". Location: "+unexpected.getLocation());
    }
    
}
