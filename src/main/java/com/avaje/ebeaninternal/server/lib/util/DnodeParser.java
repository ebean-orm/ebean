package com.avaje.ebeaninternal.server.lib.util;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse an xml document into a Dnode tree.
 */
public class DnodeParser extends DefaultHandler {

    /**
     * The root of the DContent tree.
     */
    Dnode root;

    /**
     * The current node being parsed.
     */
    Dnode currentNode;

    /**
     * The nodeContent buffer.
     */
    StringBuilder buffer;

    /**
     * Used to stack the nodes.
     */
    Stack<Dnode> stack = new Stack<Dnode>();

    /**
     * The class used to construct new nodes. Should be Dnode or a subtype of
     * Dnode.
     */
    Class<?> nodeClass = Dnode.class;

    int depth = 0;
    
    /**
     * Trim whitespace from the content.
     */
    boolean trimWhitespace = true;

    /**
     * The name of the tag that contains html content
     */
    String contentName;
    
    /**
     * The depth of the tag that contains the html content
     */
    int contentDepth;
    
    
    /**
     * If true then trim the whitespace from the content.
     */
    public boolean isTrimWhitespace() {
        return trimWhitespace;
    }

    /**
     * Set whether to trim whitespace from the content.
     */
    public void setTrimWhitespace(boolean trimWhitespace) {
        this.trimWhitespace = trimWhitespace;
    }

    /**
     * Return the root node of the DContent tree.
     */
    public Dnode getRoot() {
        return root;
    }

    /**
     * Set the type class of node to be created.
     */
    public void setNodeClass(Class<?> nodeClass) {
        this.nodeClass = nodeClass;
    }

    /**
     * Create a new Dnode using the nodeClass.
     */
    private Dnode createNewNode() {
        try {
            return (Dnode) nodeClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * process a startElement.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        super.startElement(uri, localName, qName, attributes);
        depth++;
        
        boolean isContent = (contentName != null);
        
        if (isContent){
            // must be html content... add the begin tag as content
            buffer.append("<").append(localName);
            for (int i = 0; i < attributes.getLength(); i++) {
                String key = attributes.getLocalName(i);
                String val = attributes.getValue(i);
                buffer.append(" ").append(key).append("='").append(val).append("'");
            }
            buffer.append(">");
            return;
              
        } 
        
        buffer = new StringBuilder();
        Dnode node = createNewNode();
        node.setNodeName(localName);     
        for (int i = 0; i < attributes.getLength(); i++) {
            String key = attributes.getLocalName(i);
            String val = attributes.getValue(i);
            node.setAttribute(key, val);
            if ("type".equalsIgnoreCase(key) && "content".equalsIgnoreCase(val)) {
                // this tag contains html content
                // no more nodes until end tag is found
                contentName = localName;
                contentDepth = depth-1;
            }
            
        }
        if (root == null) {
            root = node;
        }
        if (currentNode != null) {
            currentNode.addChild(node);
        }
        stack.push(node);
        currentNode = node;
        
    }

    /**
     * append the node content.
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        String s = new String(ch, start, length);
        int p = s.indexOf('\r');
        int p2 = s.indexOf('\n');
        if (p == -1 && p2 > -1) {
            // This is probably not an issue but tidys up content
            // in my text editor
            s = StringHelper.replaceString(s, "\n", "\r\n");
        }
        buffer.append(s);
    }

    /**
     * process the endElement.
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        depth--;
        
        if (contentName != null){
            // is this the end of the content?
            if (contentName.equals(localName) && contentDepth == depth){
                contentName = null;
                
            } else {
                // the html content end tag
                buffer.append("</").append(localName).append(">");
            }
            return;
        }
        String content = buffer.toString();
        buffer.setLength(0);
        if (content.length() > 0) {
            if (trimWhitespace) {
            	content = content.trim();
            }
            if (content.length() > 0) {
                currentNode.setNodeContent(content);
            }
        }
        stack.pop();
        if (!stack.isEmpty()) {
            // get the new currentNode
            currentNode = (Dnode) stack.pop();
            stack.push(currentNode);
        }
    }

}
