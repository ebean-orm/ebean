package com.avaje.tests.ldap;

import javax.naming.directory.DirContext;

import com.avaje.ebean.config.ldap.LdapContextFactory;

public class MockContextFactory implements LdapContextFactory {

    public DirContext createContext() {
        return new MockDirContext();
    }

    
}
