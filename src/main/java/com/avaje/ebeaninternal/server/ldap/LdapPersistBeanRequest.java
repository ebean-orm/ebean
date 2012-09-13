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

import java.util.Set;

import javax.naming.ldap.LdapName;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.core.ConcurrencyMode;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanManager;

public class LdapPersistBeanRequest<T> extends PersistRequestBean<T> {

    private final DefaultLdapPersister persister;
    
    public LdapPersistBeanRequest(SpiEbeanServer server, T bean, Object parentBean, BeanManager<T> mgr, DefaultLdapPersister persister) {
        
        super(server, bean, parentBean, mgr, null, null);
        this.persister = persister;
    }
    
    public LdapPersistBeanRequest(SpiEbeanServer server, T bean, Object parentBean, BeanManager<T> mgr, DefaultLdapPersister persister,
             Set<String> updateProps, ConcurrencyMode concurrencyMode) {
        
        super(server, bean, parentBean, mgr, null, null, updateProps, concurrencyMode);
        this.persister = persister;
    }

    public LdapName createLdapName() {
        return beanDescriptor.createLdapName(bean);
    }
    
    @Override
    public int executeNow() {
        
        return persister.persist(this);
    }

    @Override
    public int executeOrQueue() {
        return executeNow();
    }

    @Override
    public void initTransIfRequired() {
        // no transaction at this stage for Ldap
    }

    @Override
    public void commitTransIfRequired() {
        // no transaction at this stage for Ldap
    }

    @Override
    public void rollbackTransIfRequired() {
        // no transaction at this stage for Ldap
    }
    
    
}
