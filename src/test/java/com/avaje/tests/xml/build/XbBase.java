package com.avaje.tests.xml.build;

import com.avaje.ebean.text.StringFormatter;
import com.avaje.ebean.text.StringParser;
import com.avaje.tests.xml.oxm.OxmNode;

public abstract class XbBase {

    protected OxmNode rootNode;
    protected final String nodeName;
    protected final String propertyName;
    protected StringParser parser;
    protected StringFormatter formatter;
    protected boolean requiresXmlEncoding;

    public XbBase(String nodeName) {
        this.nodeName = nodeName;
        this.propertyName = null;
    }
    
    public XbBase(OxmNode rootNode, String nodeName, String propertyName) {
        this.rootNode = rootNode;
        this.nodeName = nodeName;
        this.propertyName = propertyName;
    }

    public String getNamingConventionNodeName(String propertyName) {
//        if (rootNode != null){
//            return rootNode.getNamingConventionNodeName(propertyName);
//        }
        return propertyName;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public String getPropertyName() {
        return propertyName;
    }

    public StringFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(StringFormatter formatter) {
        this.formatter = formatter;
    }

    public StringParser getParser() {
        return parser;
    }

    public void setParser(StringParser parser) {
        this.parser = parser;
    }

    public boolean isRequiresXmlEncoding() {
        return requiresXmlEncoding;
    }

    public void setRequiresXmlEncoding(boolean requiresXmlEncoding) {
        this.requiresXmlEncoding = requiresXmlEncoding;
    }
    
}
