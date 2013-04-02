package com.avaje.ebeaninternal.server.ldap;

import java.util.Set;

import javax.naming.ldap.LdapName;

import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
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
