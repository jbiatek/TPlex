package edu.umn.crisys.util.xml;


import java.util.Iterator;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


/**
 * <p>Utilities for using Java's XML Stream (StAX) intended for data 
 * marshalling. 
 * You should follow some conventions to keep things straight:
 * 
 * <p>Pass around both the reader and the latest StartElement to come out of it.
 * That way, you can see what the next tag is and delegate it to another 
 * method, and the other method can see what the tag is too (since the Reader
 * has already moved on to the next one). 
 * 
 * <p>When you delegate to another method, that method should read everything
 * up to and including the end tag that corresponds with the passed-in 
 * start tag. When you return, the Reader should be poised to return the 
 * event just after the end tag. It's easy to forget to pull the end tag out
 * too, so just think of it as "closing" each tag that you "start". 
 * 
 * <p>Two things in here "close" the tag for you. Both getStringContent and
 * allChildTagsOf take a start tag, and when they see an end tag they "close"
 * it for you and additionally make sure that it matches the start tag. The
 * allChildTagsOf method uses some slightly tricky side effects, so make sure
 * to read its documentation when using. 
 * 
 * <p>Common idioms:
 * <pre><p>
 * public Foo parseFooTag(StartElement fooStart, XMLEventReader xml) {
 *     assertStart("Foo", fooStart); // just to make sure 
 *      
 *     // We want each child node of the Foo tag: 
 *     for (StartElement child : allChildTagsOf(fooStart, xml)) { 
 *         if (isTag(child, "Bar")) { 
 *         
 *             // Do something to contents of "Bar" tag 
 *         
 *             // Now we think that the next tag out of the reader is &lt;/Bar&gt;
 *             assertClosedTag(child, xml); 
 *             // By doing this, we "consume" the end tag and we can move on.
 *             
 *         } else if (isTag(child, "Baz")) { 
 *             bazContent = getStringContent(child, xml); 
 *             // tag is already closed thanks to getStringContent()
 *         } 
 *     } 
 *     // Tag is already closed thanks to allChildTagsOf, otherwise we'd 
 *     // probably do assertClosedTag(fooStart, xml) to "consume" that end tag 
 *     return foo; 
 * }
 * </p></pre>
 * 
 * <p>Lots of asserts will help you. The assert methods work well wrapped around
 * <code>xml.nextTag()</code>, since they also return the tag after checking it. 
 * 
 * <p>Lastly, some of these methods assume that whitespace and/or XML comments
 * are irrelevent. I think that they all state as such, but if you care about
 * those things, watch out for methods that call nextTag() or obvious ones like
 * skipWhitespaceAndComments(). For our purposes, whitespace and comments just
 * get in the way. 
 * 
 */
public class XMLUtils {

    /** 
     * Calls xml.nextTag() and unchecks the exception.
     * 
     * @param xml
     * @return xml.nextTag(), or throws a RuntimeException.
     */
    public static XMLEvent nextTag(XMLEventReader xml) {
        try {
            return xml.nextTag();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
    
    /** 
     * Calls xml.nextEvent() and unchecks the exception.
     * 
     * @param xml
     * @return xml.nextEvent(), or throws a RuntimeException.
     */
    public static XMLEvent nextEvent(XMLEventReader xml) {
        try {
            return xml.nextEvent();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Calls xml.peek() and unchecks the exception. 
     * @param xml
     * @return
     */
    public static XMLEvent peek(XMLEventReader xml) {
        try {
            return xml.peek();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @param e
     * @param localName
     * @return true if the given XML event is a start tag named localName.
     */
    public static boolean isTag(XMLEvent e, String localName) {
        return e.isStartElement() && localNameOf(e).equals(localName);
    }
    
    public static boolean isTagStartingWith(XMLEvent e, String prefix) {
        return e.isStartElement() && localNameOf(e).startsWith(prefix);
    }
    
    public static boolean isTagEndingWith(XMLEvent e, String prefix) {
        return e.isStartElement() && localNameOf(e).endsWith(prefix);
    }

    public static boolean isEndTag(XMLEvent e, String localName) {
        return e.isEndElement() && localNameOf(e).equals(localName);
    }
    
    public static Optional<String> getPossibleAttribute(XMLEvent e, String name) {
    	return Optional.ofNullable(e.asStartElement().getAttributeByName(
    			new QName(name)))
    				.map(attr -> attr.getValue());
    }
    
    public static String attribute(XMLEvent e, String name) {
        return getPossibleAttribute(e, name).get();
    }
    
    public static String localNameOf(XMLEvent e) {
        if (e.isStartElement()) {
            return e.asStartElement().getName().getLocalPart();
        } else if (e.isEndElement()) {
            return e.asEndElement().getName().getLocalPart();
        }
        throw new RuntimeException(e+" isn't a tag to get the name of.");
    }
    
    /**
     * Get the content of this tag, assuming it's just characters with no other
     * tags beneath it. Something like &lt;tag&gt;This is my content&lt;/tag&gt;
     * will return "This is my content". If there's anything but regular 
     * characters, an exception will be thrown. It also checks the end tag
     * for you. 
     * @param start The start tag
     * @param xml
     * @return The string content of this tag.
     * @throws XMLStreamException at runtime
     */
    public static String getStringContent(XMLEvent start, XMLEventReader xml) {
        String ret = "";
        XMLEvent e = nextEvent(xml);
        while (e.isCharacters()) {
            ret += e.asCharacters().getData();
            e = nextEvent(xml);
        }
        // Make sure we actually got to the end tag, not something else
        assertEnd(localNameOf(start), e);
        return ret;
    }

    /**
     * Peek ahead to the next start tag and see if it has the given name. After
     * this call, xml.nextEvent() will be the next event, and you'll know if
     * it's the one you're looking for. Note that this will eat any whitespace
     * between here and the next event. Comments too.
     * @param name
     * @param xml
     * @return
     */
    public static boolean nextTagIsStartOf(String name, XMLEventReader xml) {
                // Let's eat up whitespace if there is any.
                skipWhitespaceAndComments(xml);
                // Now we're at something interesting.
                return isTag(peek(xml), name);
            }

    public static boolean nextTagIsEndOf(String name, XMLEventReader xml) 
            throws XMLStreamException {
        skipWhitespaceAndComments(xml);
        return isEndTag(xml.peek(), name);
    }
    
    public static void skipWhitespaceAndComments(XMLEventReader xml) {
        XMLEvent peek = peek(xml);
        while (isWhitespace(peek) || isComment(peek)) {
            nextEvent(xml);
            peek = peek(xml);
        }
    }
    
    public static boolean isWhitespace(XMLEvent e) {
        if (e.isCharacters() && e.asCharacters().isWhiteSpace()) {
            return true;
        }
        return false;
    }
    
    public static boolean isComment(XMLEvent e) {
        if (e.getEventType() == XMLEvent.COMMENT) {
            return true;
        }
        return false;
    }
    
    public static StartElement assertStart(String name, XMLEvent event) {
        if ( ! isTag(event, name)) {
            throw new RuntimeException("Was expecting a "+name+" tag, not this:" + event);
        }
        return event.asStartElement();
    }

    /**
     * Reads the next tag and throws an exception if the next tag out of the 
     * reader is not an end tag corresponding to the given start tag. 
     * 
     * @param start
     * @param xml
     */
    public static void assertClosedTag(StartElement start, XMLEventReader xml) {
        try {
            assertEnd(localNameOf(start), xml.nextTag());
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static EndElement assertEnd(String name, XMLEvent event) {
        if ( ! isEndTag(event, name)) {
            throw new RuntimeException("Was expecting an end tag "+name+", not this: "+event);
        }
        return event.asEndElement();
    }
    
    /**
     * <p>This iterator will continue to return StartElement events as long as 
     * they come up. It also checks for end tags. If the wrong one comes up,
     * an exception is thrown. If it sees the right one, it will consume it
     * and stop the loop. 
     * 
     * <p>You should use this in a scenario where you've just gotten a Start tag,
     * and you want to iterate over each child. You also shouldn't care about
     * the whitespace between children, because this will silently skip it. 
     * It also skips XML comments. 
     * 
     * <p>This iterator makes that scenario dead simple, <em>as long as you leave the Reader
     * at the next child at the end of the loop</em>. After the loop, you should
     * be at the next tag after the close of the start. More simply, just make
     * sure that the last thing you do to the XML in each loop is "close" the current tag. 
     * 
     * <p>Note that this kind of breaks the contract for iterators. It's really just a 
     * wrapper around the XMLEventReader (which itself is an iterator) that
     * deals with start and end tags differently. It can't be reused or
     * re-obtained to go again. 
     * 
     * @author jbiatek
     *
     */
    public static Iterable<StartElement> allChildTagsOf(StartElement start, XMLEventReader xml) {
    	return new TagIterator(xml, start);
    }
    
    private static class TagIterator implements Iterator<StartElement>, Iterable<StartElement> {
        private XMLEventReader xml;
        private String expectedEnd;
        
        public TagIterator(XMLEventReader xml, StartElement tagToClose) {
            this.xml = xml;
            expectedEnd = localNameOf(tagToClose);
        }

        @Override
        public boolean hasNext() {
            try {
                skipWhitespaceAndComments(xml);
                if (xml.peek().isEndElement()) {
                    if ( ! isEndTag(xml.peek(), expectedEnd)) {
                        throw new UnexpectedTagException(xml.peek(), expectedEnd);
                    } else {
                        // Consume the end tag, since it was okay
                        xml.nextEvent(); // om nom nom
                        return false;
                    }
                }
                return xml.peek().isStartElement();
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public StartElement next() {
            try {
                return xml.nextTag().asStartElement();
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Deletion not supported");
        }

        /**
         * This bit is not technically how it's supposed to be.
         */
        @Override
        public Iterator<StartElement> iterator() {
            return this;
        }
    }
    
    /**
     * Take all events up to the end tag of this element out of the reader. 
     * 
     * @param start 
     * @param xml
     */
    public static void consumeAllOf(StartElement start, XMLEventReader xml) {
    	while (xml.hasNext()) {
    		XMLEvent next = peek(xml);
    		if (next.isStartElement()) {
    			consumeAllOf(nextEvent(xml).asStartElement(), xml);
    		} else if (next.isEndElement()) {
    			// That should be it!
    			assertClosedTag(start, xml);
    			return;
    		} else if (next.isEndDocument()) {
    			throw new RuntimeException(
    					"Document ended in the middle of this tag: "+start);
    		} else {
    			// It's something harmless. Eat it and move on.
    			nextEvent(xml);
    		}
    	}
    	throw new RuntimeException("Stream ran out of events in this tag: "+start);
    }
    
    /**
     * Print this tag and its contents. This will consume everything up to the
     * matching end tag.
     * 
     * @param start
     * @param xml
     * @param indent
     * @throws XMLStreamException
     */
    public static String printEvents(XMLEvent current, XMLEventReader xml) {
                return printEvents(current, xml, 0);
            }

    private static String printEvents(XMLEvent start, XMLEventReader xml, int indent) {
                String ret = "\n";
                for (int i=0; i<indent; i++) {
                    ret += " ";
                }
                ret += start + "\n";
                
                XMLEvent next = null;
                // Keep processing events...
                while (xml.hasNext()) {
                    next = nextEvent(xml);
                    // Delegate start tags to a new indentation level
                    if (next.isStartElement()) {
                        printEvents(next, xml, indent+4);
                    } else if (next.isEndElement()) {
                        // We've found our end element. Add it and be done.
                        for (int i=0; i<indent; i++) {
                            ret += " ";
                        }
                        ret += next + "\n";
                        return ret;
                    } else if (next.isCharacters() && next.asCharacters().isWhiteSpace()) {
                        // Skip whitespace
                        continue;
                    } else {
                        // Print the contents of this tag
                        for (int i=0; i<indent+4; i++) {
                            ret += " ";
                        }
                        ret += next + "\n";
                    }
                }
                // Oh wow, we ran out of XML. Uh, return then?
                return ret;
            }

}