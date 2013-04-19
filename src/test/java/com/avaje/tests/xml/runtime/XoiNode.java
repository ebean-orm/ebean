package com.avaje.tests.xml.runtime;

import java.io.IOException;

import org.w3c.dom.Node;


public interface XoiNode {

    public String getNodeName();
    
    public Object createBean();

    public void writeNode(XrOutputDocument out, Node node, Object bean) throws IOException;

    public void writeNode(XrOutputWriter o, Object bean) throws IOException;

    public void readNode(Node node, XrReadContext ctx);

}
