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
package com.avaje.ebeaninternal.server.ldap;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

public class LdapBeanBuilder<T> {

    private static final Logger logger = Logger.getLogger(LdapBeanBuilder.class.getName());
    
    private final BeanDescriptor<T> beanDescriptor;
    
    private final boolean vanillaMode;
    
    private Set<String> loadedProps;
    
    public LdapBeanBuilder(BeanDescriptor<T> beanDescriptor, boolean vanillaMode) {
        this.beanDescriptor = beanDescriptor;
        this.vanillaMode = vanillaMode;
    }
    
    @SuppressWarnings("unchecked")
    public T readAttributes(Attributes attributes) throws NamingException {

        Object bean = beanDescriptor.createBean(vanillaMode);

        NamingEnumeration<? extends Attribute> all = attributes.getAll();

        boolean setLoadedProps = false;
        if (loadedProps == null) {
            setLoadedProps = true;
            loadedProps = new LinkedHashSet<String>();
        }

        while (all.hasMoreElements()) {
            Attribute attr = all.nextElement();
            String attrName = attr.getID();

            BeanProperty prop = beanDescriptor.getBeanPropertyFromDbColumn(attrName);
            if (prop == null) {
            	if ("objectclass".equalsIgnoreCase(attrName)) {
            		// this is expected
            	} else {
	                logger.info("... hmm, no property to map to attribute[" + attrName + "] value["+attr.get()+"]");
            	}
                
            } else {
                prop.setAttributeValue(bean, attr);
                if (setLoadedProps) {
                    loadedProps.add(prop.getName());
                }
            }
        }

        if (bean instanceof EntityBean) {
            EntityBeanIntercept ebi = ((EntityBean) bean)._ebean_getIntercept();
            ebi.setLoadedProps(loadedProps);
            ebi.setLoaded();
        }

        BeanPersistController persistController = beanDescriptor.getPersistController();
        if (persistController != null) {
            persistController.postLoad(bean, loadedProps);
        }

        return (T)bean;
    }

}
