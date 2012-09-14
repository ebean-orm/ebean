package com.avaje.tests.xml.oxm;


public interface OxmContextBuilder {

    public OxmNode addRootElement(String rootElementName, Class<?> rootType);

    public OxmContext createContext();
}