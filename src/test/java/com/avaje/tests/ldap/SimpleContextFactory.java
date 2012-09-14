package com.avaje.tests.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.avaje.ebean.config.ldap.LdapContextFactory;

public class SimpleContextFactory implements LdapContextFactory {

    String url;
    String principal;
    String credentials;
    
    public SimpleContextFactory(String url, String principal, String credentials) {
        this.url = url;
        this.principal = principal;
        this.credentials = credentials;
    }
    
    public DirContext createContext() {
                    
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, credentials);

        try {
            return new InitialDirContext(env);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

}

