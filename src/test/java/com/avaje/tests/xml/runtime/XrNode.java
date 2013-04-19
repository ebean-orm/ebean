package com.avaje.tests.xml.runtime;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.avaje.ebean.text.StringFormatter;
import com.avaje.ebean.text.StringParser;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

public class XrNode extends XrBase implements XoiNode {

    protected final boolean assocBeanValue;

    protected final XoiAttribute[] attributes;
    protected final boolean hasAttributes;

    protected final String beginTag;
    protected final String beginTagEnd;
    protected final String endTag;
    
    protected final XoiNode[] childNodes;
    protected final boolean hasChildNodes;
    
    public XrNode(String nodeName, ElPropertyValue prop, BeanDescriptor<?> parentDesc, XoiNode[] childNodes, XoiAttribute[] attributes) {
        this(nodeName, prop, parentDesc, null, null, childNodes, attributes, true);
    }

    public XrNode(String nodeName, ElPropertyValue prop, BeanDescriptor<?> parentDesc, StringFormatter formatter,
            StringParser parser, XoiNode[] childNodes, XoiAttribute[] attributes) {

        this(nodeName, prop, parentDesc, formatter,parser, childNodes, attributes, false);
    }
    
    /**
     * Create as a property based node.
     */
    private XrNode(String nodeName, ElPropertyValue prop, BeanDescriptor<?> parentDesc, StringFormatter formatter,
            StringParser parser, XoiNode[] childNodes, XoiAttribute[] attributes, boolean assocBeanValue) {

        super(nodeName, prop, parentDesc, formatter, parser);
        
        this.assocBeanValue = assocBeanValue;
        this.attributes = attributes;
        this.hasAttributes = (attributes != null && attributes.length > 0);

        this.childNodes = childNodes;
        this.hasChildNodes = (childNodes!= null && childNodes.length > 0);

        boolean selfClosing = false;
        
        this.beginTagEnd = selfClosing ? "/>" : ">";
        this.beginTag = "<" + nodeName;
        this.endTag = selfClosing ? "" : "</" + nodeName + ">";
    }
    
    public String toString() {
        return nodeName;
    }
    
    public Object createBean() {
        return beanDescriptor.createBean();
    }
    
    public String getNodeName() {
        return nodeName;
    }

    public void readNode(Node node, XrReadContext ctx) {

        Object parentBean = null;
        if (assocBeanValue){
            parentBean = ctx.getBean();
            Object childBean = beanDescriptor.createBean();
            setObjectValue(parentBean, childBean);
        
            ctx.setBean(childBean);
            
        } else if (prop != null){
            
            String c = node.getTextContent();
            Object v = getParsedValue(c);
            setObjectValue(ctx.getBean(), v);
        }
        
        if (hasAttributes){
            NamedNodeMap attrs = node.getAttributes();
            for (int i = 0; i < attributes.length; i++) {
                attributes[i].readNode(node, attrs, ctx);
            } 
        }
        
        if (hasChildNodes){
            readChildNodes(node, ctx);
        }
        
        if (parentBean != null){
            ctx.setBean(parentBean);
        }
    }

    private void readChildNodes(Node node, XrReadContext ctx) {

        Node childNode = node.getFirstChild();
        int pos = -1;

        do {
            for (; ++pos < childNodes.length;) {
                if (childNode.getNodeName().equals(childNodes[pos].getNodeName())) {
                    childNodes[pos].readNode(childNode, ctx);
                    break;
                } else {
                    System.out.println("no element for " + childNodes[pos].getNodeName());
                }
            }

            childNode = childNode.getNextSibling();

        } while (childNode != null);

    }


    public void writeNode(XrOutputDocument out, Node node, Object bean) throws IOException {

        Object val = prop == null ? null : getObjectValue(bean);
        
        Node childNode;
        if (nodeName == null){
            childNode = node;
            
        } else {    
            Element element = out.getDocument().createElement(nodeName);
            childNode = element;
            out.appendChild(node, element);

            if (hasAttributes) {
                for (int i = 0; i < attributes.length; i++) {
                    attributes[i].writeAttribute(out, element, bean, val);
                } 
            } 
        } 
        
        writeContent(out, childNode, bean, val);
    }
    
    public void writeNode(XrOutputWriter o, Object bean) throws IOException {
        
        Object val = null;
        if (prop != null){
            val = getObjectValue(bean);
            if (val == null && !hasAttributes && !hasChildNodes){
                return;
            }
        }
        
        o.increaseDepth();
        o.write(beginTag);
        if (hasAttributes) {
            for (int i = 0; i < attributes.length; i++) {
                attributes[i].writeAttribute(o, bean, val);
            }
        }
        o.write(beginTagEnd);            
        
        writeContent(o, bean, val);
        
        if (hasChildNodes){
            o.decreaseDepth(true);
        } else {
            o.decreaseDepth(decreaseDepth);
        }
        
        o.write(endTag);
    }

    public void writeContent(XrOutputDocument out, Node e, Object bean, Object value) throws IOException {

        if (!assocBeanValue && value != null){
            String sv = getFormattedValue(value);
            //TODO encode string
            e.setTextContent(sv);
        }

        if (hasChildNodes) {
            Object beanToPass = assocBeanValue ? value : bean;
            for (int i = 0; i < childNodes.length; i++) {
                childNodes[i].writeNode(out, e, beanToPass);
            }
        }
    }
    
    public void writeContent(XrOutputWriter o, Object bean, Object value) throws IOException {

        if (!assocBeanValue && value != null){
            String sv = getFormattedValue(value);
            o.writeEncoded(sv);
        }
        if (hasChildNodes) {
            Object beanToPass = assocBeanValue ? value : bean;
            for (int i = 0; i < childNodes.length; i++) {
                childNodes[i].writeNode(o, beanToPass);
            }
        }
    }

}
