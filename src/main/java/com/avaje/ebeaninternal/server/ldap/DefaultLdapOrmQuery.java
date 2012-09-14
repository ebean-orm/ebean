package com.avaje.ebeaninternal.server.ldap;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebeaninternal.server.querydefn.DefaultOrmQuery;

public class DefaultLdapOrmQuery<T> extends DefaultOrmQuery<T> {

    private static final long serialVersionUID = -4344629258591773124L;

    public DefaultLdapOrmQuery(Class<T> beanType, EbeanServer server, ExpressionFactory expressionFactory, String query) {
        super(beanType, server, expressionFactory, query);
    }
}
