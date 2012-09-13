/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
