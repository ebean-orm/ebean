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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.tests.xml.oxm.OxmNode;
import com.avaje.tests.xml.runtime.XoiAttribute;
import com.avaje.tests.xml.runtime.XoiNode;
import com.avaje.tests.xml.runtime.XrCollection;
import com.avaje.tests.xml.runtime.XrNode;

public class XbNode extends XbBase implements OxmNode {

    private Map<String,XbAttribute> attributes = new LinkedHashMap<String, XbAttribute>();

    private List<XbNode> childNodes = new ArrayList<XbNode>();
    
    private final String rootElementName;
    private final BeanDescriptor<?> rootDescriptor;
    
    public XbNode(String rootElementName, BeanDescriptor<?> rootDescriptor) {
        super(rootElementName);
        this.rootElementName = rootElementName;
        this.rootDescriptor = rootDescriptor;
        this.rootNode = this;
    }
    
    public XbNode(OxmNode rootNode, String propertyName, String nodeName) {
        super(rootNode, nodeName, propertyName);
        this.rootElementName = null;
        this.rootDescriptor = null;
    }

    public Class<?> getRootBeanType() {
        return rootDescriptor.getBeanType();
    }
    
    public String getRootElementName() {
        return rootElementName;
    }

    public OxmNode addWrapperElement(String elementName){
        return addElement(null, elementName);
    }
    
    public OxmNode addElement(String propertyName){
        String elementName = getNamingConventionNodeName(propertyName);
        return addElement(propertyName, elementName);
    }
    
    public OxmNode addElement(String propertyName, String elementName){
        
        XbNode n = new XbNode(rootNode, propertyName, elementName);
        childNodes.add(n);
        return n;
    }
    
    public OxmNode addAttribute(String propertyName){
        String attrName = getNamingConventionNodeName(propertyName);
        return addAttribute(propertyName, attrName);
    }
    
    public OxmNode addAttribute(String propertyName, String attrName){
        XbAttribute xbAttribute = attributes.get(attrName);
        if (xbAttribute != null){
            throw new RuntimeException("Attribute with this name ["+attrName+"]already exists");
        } 
        xbAttribute = new XbAttribute(rootNode, attrName, propertyName);
        return this;
    }

    public XoiNode createNode() {
        return createNode(rootDescriptor);
    }
    
    protected XoiNode createNode(BeanDescriptor<?> d) {
    
        XoiAttribute[] attrs = new XoiAttribute[attributes.size()];
        
        boolean assocManyProperty = false;
        boolean assocOneProperty = false;
        
        ElPropertyValue prop = null;
        
        if (propertyName != null){
            prop = d.getElGetValue(propertyName);
            if (prop == null){
                throw new RuntimeException("Property "+propertyName+" not found from "+d.getFullName());
            }
            BeanProperty p = prop.getBeanProperty();
            if (p instanceof BeanPropertyAssoc<?>){
                // move to the relative/target descriptor before 
                // evaluating the attributes and children for this node
                BeanDescriptor<?> targetDescriptor = ((BeanPropertyAssoc<?>)p).getTargetDescriptor();
                if (targetDescriptor == null) {
                    if (EntityType.XMLELEMENT.equals(d.getEntityType())){
                        Class<?> targetType = p.getPropertyType();
                        targetDescriptor = d.getBeanDescriptor(targetType);
                    }
                }
                
                if (targetDescriptor != null){
                    d = targetDescriptor;
                }
                assocManyProperty = (p instanceof BeanPropertyAssocMany<?>);
                assocOneProperty = (p instanceof BeanPropertyAssocOne<?>);
            }
        }   

        int i = 0;
        for (XbAttribute attrBuilder : attributes.values()) {
            XoiAttribute xoAttr = attrBuilder.create(d, assocOneProperty);
            attrs[i++] = xoAttr;
        }
        
        XoiNode[] children = null;
        if (!childNodes.isEmpty()){
            children = new XoiNode[childNodes.size()];
            for (int j = 0; j < children.length; j++) {
                XbNode xbNode = childNodes.get(j);
                children[j] = xbNode.createNode(d);
            }
        }

        if (assocManyProperty){
            boolean invokeFetch = false;
            return new XrCollection(nodeName, prop, children, invokeFetch);
        }
        if (assocOneProperty){
            return new XrNode(nodeName, prop, d, children, attrs);            
        }
     
        return new XrNode(nodeName, prop, d, formatter, parser, children, attrs);
    }
}
