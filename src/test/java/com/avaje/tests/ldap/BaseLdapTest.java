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
