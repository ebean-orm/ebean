package com.avaje.tests.xml.runtime;

import java.io.IOException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.avaje.ebean.text.StringFormatter;
import com.avaje.ebean.text.StringParser;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

public class XrAttribute extends XrBase implements XoiAttribute {

    private boolean parentAssocBean;
    
    /**
     * Create as an property based attribute.
     */
    public XrAttribute(String attrName, ElPropertyValue prop, BeanDescriptor<?> parentDesc, boolean parentAssocBean, StringFormatter formatter, StringParser parser) {
        super(attrName, prop, parentDesc, formatter, parser);
        this.parentAssocBean = parentAssocBean;
    }

    public XrAttribute(String attrName, ElPropertyValue prop, BeanDescriptor<?> parentDesc, boolean parentAssocBean) {
        this(attrName, prop, parentDesc, parentAssocBean, null, null);
    }
    
    public void writeAttribute(XrOutputDocument out, Element e, Object bean, Object value) throws IOException {

        Object beanToUse = parentAssocBean ? value : bean;

        Object v = getObjectValue(beanToUse);
        String sv = getFormattedValue(v);
        Attr attr = out.getDocument().createAttribute(nodeName);
        attr.setValue(sv);

        e.setAttributeNode(attr);
    }

    public void writeAttribute(XrOutputWriter o, Object bean, Object value) throws IOException {

        Object beanToUse = parentAssocBean ? value : bean;
        Object v = getObjectValue(beanToUse);
        String sv = getFormattedValue(v);

        o.write(" ");
        o.write(nodeName);
        o.write("=\"");
        o.write(sv);
        o.write("\"");
    }

    public void readNode(Node node, NamedNodeMap attributes, XrReadContext ctx) {
        
        Node namedItem = attributes.getNamedItem(nodeName);
        if (namedItem != null){
            String c = namedItem.getTextContent();
            Object v = getParsedValue(c);
            setObjectValue(ctx.getBean(), v);
        }
    }

    
}
