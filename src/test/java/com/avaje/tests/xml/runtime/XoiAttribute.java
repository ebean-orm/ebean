package com.avaje.tests.xml.runtime;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public interface XoiAttribute {

    public void writeAttribute(XrOutputWriter o, Object bean, Object value) throws IOException;
    
    public void writeAttribute(XrOutputDocument out, Element e, Object bean, Object value) throws IOException;

    public void readNode(Node node, NamedNodeMap attributes, XrReadContext ctx);
    
}
