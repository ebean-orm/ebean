package com.avaje.tests.xml.oxm;

/**
 * Naming convention used in XML to Object mapping.
 * 
 * @author rbygrave
 */
public interface OxmNamingConvention {

    /**
     * Returns the default node/element/attribute name for the given property name.
     */
    public String getNodeName(String propertyName);
}
