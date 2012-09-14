package com.avaje.tests.xml.oxm;

public interface OxmNode {

    public OxmNode addWrapperElement(String elementName);

    public OxmNode addElement(String propertyName);

    public OxmNode addElement(String propertyName, String elementName);

    public OxmNode addAttribute(String propertyName);

    public OxmNode addAttribute(String propertyName, String attrName);

}