package com.avaje.ebeaninternal.server.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.Transaction;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiSqlQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;

/**
 * Wraps the objects involved in executing a SqlQuery.
 */
public final class RelationalQueryRequest {

    private final SpiSqlQuery query;

    private final RelationalQueryEngine queryEngine;

    private final SpiEbeanServer ebeanServer;

    private SpiTransaction trans;

    private boolean createdTransaction;

    private SpiQuery.Type queryType;

    /**
     * Create the BeanFindRequest.
     */
    public RelationalQueryRequest(SpiEbeanServer server, RelationalQueryEngine engine, SqlQuery q, Transaction t) {
        this.ebeanServer = server;
        this.queryEngine = engine;
        this.query = (SpiSqlQuery) q;
        this.trans = (SpiTransaction) t;
    }

    /**
     * Create a transaction if none currently exists.
     */
    public void initTransIfRequired() {
        if (trans == null) {
            trans = ebeanServer.getCurrentServerTransaction();
            if (trans == null || !trans.isActive()) {
                // create a local readOnly transaction
                trans = ebeanServer.createServerTransaction(false, -1);
                createdTransaction = true;
            }
        }
    }

    /**
     * End the transaction if it was locally created.
     */
    public void endTransIfRequired() {
        if (createdTransaction) {
            trans.endQueryOnly();
        }
    }

    @SuppressWarnings("unchecked")
    public List<SqlRow> findList() {
        queryType = SpiQuery.Type.LIST;
        return (List<SqlRow>) queryEngine.findMany(this);
    }

    @SuppressWarnings("unchecked")
    public Set<SqlRow> findSet() {
        queryType = SpiQuery.Type.SET;
        return (Set<SqlRow>) queryEngine.findMany(this);
    }

    @SuppressWarnings("unchecked")
    public Map<?, SqlRow> findMap() {
        queryType = SpiQuery.Type.MAP;
        return (Map<?, SqlRow>) queryEngine.findMany(this);
    }

    /**
     * Return the find that is to be performed.
     */
    public SpiSqlQuery getQuery() {
        return query;
    }

    /**
     * Return the type (List, Set or Map) that this fetch returns.
     */
    public SpiQuery.Type getQueryType() {
        return queryType;
    }

    public EbeanServer getEbeanServer() {
        return ebeanServer;
    }

    public SpiTransaction getTransaction() {
        return trans;
    }

    public boolean isLogSql() {
        return trans.isLogSql();
    }

    public boolean isLogSummary() {
        return trans.isLogSummary();
    }

}
