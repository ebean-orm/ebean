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
package com.avaje.ebeaninternal.server.ddl;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.deploy.InheritInfoVisitor;

/**
 * Base BeanVisitor that can help visiting inherited properties.
 * 
 * @author rbygrave
 */
public abstract class AbstractBeanVisitor implements BeanVisitor {
    
    /**
     * Visit all the other inheritance properties that are not on the root.
     */
    public void visitInheritanceProperties(BeanDescriptor<?> descriptor, PropertyVisitor pv) {

        InheritInfo inheritInfo = descriptor.getInheritInfo();
        if (inheritInfo != null && inheritInfo.isRoot()){
            // add all properties on the children objects
            InheritChildVisitor childVisitor = new InheritChildVisitor(pv);
            inheritInfo.visitChildren(childVisitor);
        }
    }
    

    /**
     * Helper used to visit all the inheritInfo/BeanDescriptor in
     * the inheritance hierarchy (to add their 'local' properties).
     */
    protected static class InheritChildVisitor implements InheritInfoVisitor {

        final PropertyVisitor pv;
        
        protected InheritChildVisitor(PropertyVisitor pv) {
            this.pv = pv;
        }
        
        public void visit(InheritInfo inheritInfo) {
            BeanProperty[] propertiesLocal = inheritInfo.getBeanDescriptor().propertiesLocal();
            VisitorUtil.visit(propertiesLocal, pv);         
        }
    }
}
