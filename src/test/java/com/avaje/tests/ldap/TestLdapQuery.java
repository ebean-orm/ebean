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
package com.avaje.tests.ldap;

import java.util.List;

import javax.naming.NamingException;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.ldap.LdapConfig;
import com.avaje.ebean.config.ldap.LdapContextFactory;
import com.avaje.tests.model.ldap.LDPerson;
import com.avaje.tests.model.ldap.LDPerson.Status;

public class TestLdapQuery extends TestCase {

    public void test() throws NamingException {
        
        boolean b = true;
        if (b){
            // turn this test off for the moment
            return;
        }

//        GlobalProperties.put("ebean.ldapContextFactory", MockContextFactory.class.getName());
        
        ServerConfig config = new ServerConfig();
        config.setName("ldap");
        config.setRegister(false);
        config.setDefaultServer(false);
        config.loadFromProperties();

//        // built automatically from ebean.ldapContextFactory
//        LdapContextFactory ldapContextFactory = config.getLdapConfig().getContextFactory();
//        Assert.assertNotNull(ldapContextFactory);
        
        LdapContextFactory contextFactory = new MockContextFactory();
        
        LdapConfig ldapConfig = new LdapConfig();
        ldapConfig.setContextFactory(contextFactory);
        config.setLdapConfig(ldapConfig);
        config.getDataSourceConfig().setOffline(true);
        config.setDatabasePlatformName("oracle");
        
        EbeanServer server = EbeanServerFactory.create(config);

        LDPerson pRob = server.find(LDPerson.class)
            .select("userId, cn")
            .setId("rbygraveTest01")
            .findUnique();
        
        Assert.assertNotNull(pRob);
        
        List<LDPerson> list = server.find(LDPerson.class)
            .select("userId,status")
            .where().like("userId","lz*").eq("status", LDPerson.Status.ACTIVE)
            .findList();
        
        System.out.println(list);

        LDPerson p = new LDPerson();
        p.setUserId("rbygraveTest01");
        p.setStatus(Status.ACTIVE);
        p.setCn("Test01");
        p.setSn("Testing123");
        p.setGivenName("Ban Dana");
        p.setUserPassword("qwerty");

        server.save(p);
    }
}
