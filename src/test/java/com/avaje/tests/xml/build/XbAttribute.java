package com.avaje.tests.xml.build;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.tests.xml.oxm.OxmNode;
import com.avaje.tests.xml.runtime.XoiAttribute;
import com.avaje.tests.xml.runtime.XrAttribute;

public class XbAttribute extends XbBase {

    
    public XbAttribute(OxmNode rootNode, String nodeName, String propertyName){
        super(rootNode, nodeName, propertyName);
    }

    public XoiAttribute create(BeanDescriptor<?> descriptor, boolean parentAssocBean) {
        
        ElPropertyValue prop = descriptor.getElGetValue(propertyName);
        return new XrAttribute(nodeName, prop, descriptor, parentAssocBean, formatter, parser);
    }
}
