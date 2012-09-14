package com.avaje.tests.ldap;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.ldap.LdapConfig;
import com.avaje.ebean.config.ldap.LdapContextFactory;

public abstract class BaseLdapTest extends TestCase {

    public void one() {
        Assert.assertTrue(true);
    }
    
    protected EbeanServer createServer() {

        LdapContextFactory contextFactory = new MockContextFactory();

        ServerConfig config = new ServerConfig();
        config.setName("h2");
        config.setRegister(false);
        config.setDefaultServer(false);
        config.loadFromProperties();

        LdapConfig ldapConfig = new LdapConfig();

        ldapConfig.setContextFactory(contextFactory);
        config.setLdapConfig(ldapConfig);

        return EbeanServerFactory.create(config);
    }
}
