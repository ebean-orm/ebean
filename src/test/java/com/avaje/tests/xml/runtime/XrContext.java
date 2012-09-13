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
package com.avaje.tests.xml.runtime;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.avaje.tests.xml.oxm.OxmContext;

public class XrContext implements OxmContext {

    Map<String,XoiNode> tagToNodeMap;
    Map<Class<?>,XoiNode> classToNodeMap;

    public XrContext(Map<String,XoiNode> tagToNodeMap, Map<Class<?>,XoiNode> classToNodeMap) {
        this.tagToNodeMap = tagToNodeMap;
        this.classToNodeMap = classToNodeMap;
    }
    
    public void writeBean(Object bean, Document document) throws IOException {
        
        XoiNode xoiNode =  getXoiNode(bean);
        
        XrOutputDocument out = new XrOutputDocument(document);
        xoiNode.writeNode(out, null, bean);
    }

    public void writeBean(Object bean, Writer writer) throws IOException {
        XoiNode xoiNode = getXoiNode(bean);
        
        XrOutputWriter o = new XrOutputWriter(writer);
        xoiNode.writeNode(o, bean);
    }

    
    
    public Object readBean(Node node) {
        
        XoiNode xoiNode = getXoiNode(node.getNodeName());
        
        Object bean = xoiNode.createBean(false);
        
        XrReadContext ctx = new XrReadContext(bean);
                
        xoiNode.readNode(node, ctx);
        return ctx.getBean();
    }

    private XoiNode getXoiNode(Object bean) {
        Class<?> cls = bean.getClass();
        XoiNode xoiNode = classToNodeMap.get(cls);
        if (xoiNode == null){
            throw new RuntimeException("No handler for type "+cls);
        } else {
            return xoiNode;
        }
    }
    
    private XoiNode getXoiNode(String nodeName) {
        
        XoiNode xoiNode = tagToNodeMap.get(nodeName);
        if (xoiNode == null){
            throw new RuntimeException("No handler for node "+nodeName);
        } else {
            return xoiNode;
        }
    }
}
