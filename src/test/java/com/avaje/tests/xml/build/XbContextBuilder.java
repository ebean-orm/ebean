package com.avaje.tests.xml.build;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.xml.oxm.OxmContext;
import com.avaje.tests.xml.oxm.OxmContextBuilder;
import com.avaje.tests.xml.oxm.OxmNamingConvention;
import com.avaje.tests.xml.runtime.XoiNode;
import com.avaje.tests.xml.runtime.XrContext;

public class XbContextBuilder implements OxmContextBuilder {
    
    private final SpiEbeanServer ebeanServer;

    private final OxmNamingConvention namingConvention;
        
    String rootNodeName;
    
    Map<String,XbNode> rootNodes = new HashMap<String,XbNode>();


    public XbContextBuilder(SpiEbeanServer ebeanServer, OxmNamingConvention namingConvention){
        this.ebeanServer = ebeanServer;
        this.namingConvention = namingConvention;
    }
    
    protected String getNamingConventionNodeName(String propertyName) {

        if (namingConvention != null){
            return namingConvention.getNodeName(propertyName);
        } else {
            return propertyName;
        }
    }
    
    public XbNode addRootElement(String rootElementName, Class<?> rootType) {
        
        BeanDescriptor<?> descriptor = ebeanServer.getBeanDescriptor(rootType);
        XbNode rootNode = new XbNode(rootElementName, descriptor);
        rootNodes.put(rootElementName, rootNode);
        
        return rootNode;
    }

    public OxmContext createContext() {
        
        Map<Class<?>,XoiNode> classNodeMap = new HashMap<Class<?>, XoiNode>(rootNodes.size()+4);
        Map<String,XoiNode> tagNodeMap = new HashMap<String, XoiNode>(rootNodes.size()+4);
        
        Iterator<XbNode> it = rootNodes.values().iterator();
        while (it.hasNext()) {
            XbNode xbNode = it.next();
            XoiNode xoiNode = xbNode.createNode();
            
            classNodeMap.put(xbNode.getRootBeanType(), xoiNode);
            tagNodeMap.put(xbNode.getRootElementName(), xoiNode);
        }
        
        return new XrContext(tagNodeMap, classNodeMap);
    }
    
    
}
