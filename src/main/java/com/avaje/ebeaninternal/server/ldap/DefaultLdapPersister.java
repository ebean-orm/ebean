package com.avaje.ebeaninternal.server.ldap;

import java.util.Iterator;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;

import com.avaje.ebean.config.ldap.LdapContextFactory;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLdapPersister {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLdapPersister.class);

    private final LdapContextFactory contextFactory;

    public DefaultLdapPersister(LdapContextFactory dirContextFactory) {
        this.contextFactory = dirContextFactory;
    }

    public int persist(LdapPersistBeanRequest<?> request) {

        switch (request.getType()) {
        case INSERT:
            return insert(request);
        case UPDATE:
            return update(request);
        case DELETE:
            return delete(request);

        default:
            throw new LdapPersistenceException("Invalid type " + request.getType());
        }
    }

    private int insert(LdapPersistBeanRequest<?> request) {

        DirContext dc = contextFactory.createContext();

        Name name = request.createLdapName();
        Attributes attrs = createAttributes(request, false, request.getLoadedProperties());

        if (logger.isDebugEnabled()) {
            logger.debug("Ldap Insert Name:" + name + " Attrs:" + attrs);
        }
        try {
            dc.bind(name, null, attrs);
            return 1;

        } catch (NamingException e) {
            throw new LdapPersistenceException(e);
        }
    }

    private int delete(LdapPersistBeanRequest<?> request) {

        DirContext dc = contextFactory.createContext();
        Name name = request.createLdapName();

        if (logger.isDebugEnabled()) {
            logger.debug("Ldap Delete Name:" + name);
        }

        try {
            dc.unbind(name);
            return 1;

        } catch (NamingException e) {
            throw new LdapPersistenceException(e);
        }
    }

    private int update(LdapPersistBeanRequest<?> request) {

        Name name = request.createLdapName();

        Set<String> updatedProperties = request.getUpdatedProperties();
        if (updatedProperties == null || updatedProperties.isEmpty()) {
            logger.info("Ldap Update has no changed properties?  Name:" + name);
            return 0;
        }

        DirContext dc = contextFactory.createContext();
        Attributes attrs = createAttributes(request, true, updatedProperties);

        if (logger.isDebugEnabled()) {
            logger.debug("Ldap Update Name:" + name + " Attrs:" + attrs);
        }

        try {
            dc.modifyAttributes(name, DirContext.REPLACE_ATTRIBUTE, attrs);
            return 1;

        } catch (NamingException e) {
            throw new LdapPersistenceException(e);
        }
    }

    private Attributes createAttributes(LdapPersistBeanRequest<?> request, boolean update, Set<String> props) {

        BeanDescriptor<?> desc = request.getBeanDescriptor();

        Attributes attrs = desc.createAttributes();
        if (update) {
            attrs = new BasicAttributes(true);
        } else {
            attrs = desc.createAttributes();
        }

        Object bean = request.getBean();

        if (props != null) {
            for (String propName : props) {
                BeanProperty p = desc.getBeanPropertyFromPath(propName);
                Attribute attr = p.createAttribute(bean);
                if (attr != null) {
                    attrs.put(attr);
                }
            }
        } else {
            Iterator<BeanProperty> it = desc.propertiesAll();
            while (it.hasNext()) {
                BeanProperty p = it.next();
                Attribute attr = p.createAttribute(bean);
                if (attr != null) {
                    attrs.put(attr);
                }
            }
        }

        return attrs;
    }
}
