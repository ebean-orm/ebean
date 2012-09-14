package com.avaje.ebeaninternal.server.ldap;

import java.util.List;

import javax.naming.directory.DirContext;

import com.avaje.ebean.config.ldap.LdapContextFactory;

public class LdapOrmQueryEngine {

    private final boolean defaultVanillaMode;
    
    private final LdapContextFactory contextFactory;
    
    public LdapOrmQueryEngine(boolean defaultVanillaMode, LdapContextFactory contextFactory) {
        this.defaultVanillaMode = defaultVanillaMode;
        this.contextFactory = contextFactory;
    }
    
    public <T> T findId(LdapOrmQueryRequest<T> request) {
    	 DirContext dc = contextFactory.createContext();
         LdapOrmQueryExecute<T> exe = new LdapOrmQueryExecute<T>(request, defaultVanillaMode, dc);
         
         return exe.findId();
    }
    
    public <T> List<T> findList(LdapOrmQueryRequest<T> request) {
        
        DirContext dc = contextFactory.createContext();
        
        LdapOrmQueryExecute<T> exe = new LdapOrmQueryExecute<T>(request, defaultVanillaMode, dc);
        
        return exe.findList();
    }
}
