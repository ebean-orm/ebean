package com.avaje.ebeaninternal.server.ldap;

import javax.persistence.PersistenceException;

public class LdapPersistenceException extends PersistenceException {

    private static final long serialVersionUID = -3170359404117927668L;

    public LdapPersistenceException(Throwable e){
        super(e);
    }
    
    public LdapPersistenceException(String msg, Throwable e){
        super(msg, e);
    }

    public LdapPersistenceException(String msg){
        super(msg);
    }

}
