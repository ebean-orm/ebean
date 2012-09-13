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
