package com.avaje.ebeaninternal.server.ldap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.QueryResultVisitor;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.SpiOrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

public class LdapOrmQueryRequest<T> implements SpiOrmQueryRequest<T> {

    private final SpiQuery<T> query;
    private final BeanDescriptor<T> desc;
    private final LdapOrmQueryEngine queryEngine;
    
    public LdapOrmQueryRequest(SpiQuery<T> query, BeanDescriptor<T> desc, LdapOrmQueryEngine queryEngine) {
        this.query = query;
        this.desc = desc;
        this.queryEngine = queryEngine;
    }
    
    public BeanDescriptor<T> getBeanDescriptor() {
        return desc;
    }

    public SpiQuery<T> getQuery() {
        return query;
    }

    public Object findId() {
    	return queryEngine.findId(this);
    }

    public List<Object> findIds() {
        throw new RuntimeException("Not Implemented yet");
    }

    public List<T> findList() {
        return queryEngine.findList(this);
    }
    
    public void findVisit(QueryResultVisitor<T> visitor) {
        throw new RuntimeException("Not Implemented yet");
    }

    public QueryIterator<T> findIterate() {
        throw new RuntimeException("Not Implemented yet");
    }

    public Map<?, ?> findMap() {
        throw new RuntimeException("Not Implemented yet");
    }

    public int findRowCount() {
        throw new RuntimeException("Not Implemented yet");
    }

    public Set<?> findSet() {
        throw new RuntimeException("Not Implemented yet");
    }

    public T getFromPersistenceContextOrCache() {
        return null;
    }

    public BeanCollection<T> getFromQueryCache() {
        return null;
    }

    public void initTransIfRequired() {
        // nothing to do here
    }

    public void rollbackTransIfRequired() {
        // nothing to do here
    }

    public void endTransIfRequired() {
        // nothing to do here
    }

}
