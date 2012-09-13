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
package com.avaje.ebeaninternal.server.deploy;

import java.util.Iterator;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;
import com.avaje.ebeaninternal.server.ldap.LdapPersistenceException;
import com.avaje.ebeaninternal.server.type.ScalarType;

public class BeanPropertySimpleCollection<T> extends BeanPropertyAssocMany<T> {

    private final ScalarType<T> collectionScalarType;
    
    public BeanPropertySimpleCollection(BeanDescriptorMap owner, BeanDescriptor<?> descriptor, DeployBeanPropertySimpleCollection<T> deploy) {
        super(owner, descriptor, deploy);
        this.collectionScalarType = deploy.getCollectionScalarType();
    }

    public void initialise() {
        super.initialise();
    }
    
    @Override
    public Attribute createAttribute(Object bean) {
        Object v = getValue(bean);
        if (v == null){
            return null;
        }
        if (ldapAttributeAdapter != null){
            return ldapAttributeAdapter.createAttribute(v);
        }
        
        BasicAttribute attrs = new BasicAttribute(getDbColumn());
        
        Iterator<?> it = help.getIterator(v);
        if (it != null){
            while (it.hasNext()) {
                Object beanValue = it.next();
                Object attrValue = collectionScalarType.toJdbcType(beanValue);
                attrs.add(attrValue);
            }
        }        
       return attrs;
    }

    @Override
    public void setAttributeValue(Object bean, Attribute attr) {
        try {
            if (attr != null){
                Object beanValue;
                if (ldapAttributeAdapter != null){
                    beanValue = ldapAttributeAdapter.readAttribute(attr);
                    
                } else {
                    boolean vanilla = true;
                    beanValue = help.createEmpty(vanilla);
                    BeanCollectionAdd collAdd = help.getBeanCollectionAdd(beanValue, mapKey);
                
                    NamingEnumeration<?> en  = attr.getAll();
                    while (en.hasMoreElements()) {
                        Object attrValue = (Object) en.nextElement();
                        Object collValue = collectionScalarType.toBeanType(attrValue);
                        collAdd.addBean(collValue);
                    }
                }
                
                setValue(bean, beanValue);
            }
        } catch (NamingException e) {
            throw new LdapPersistenceException(e);
        }
    }
    
    
}
