/**
 * Copyright (C) 2006  Robin Bygrave
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
     * Rollback the transaction if it was created for this request.
     */
    public void rollbackTransIfRequired() {
        if (createdTransaction) {
            trans.rollback();
        }
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

                // commented out for performance reasons...
                // TODO: review performance of trans.setReadOnly(true)
                // trans.setReadOnly(true);
                createdTransaction = true;
            }
        }
    }

    /**
     * End the transaction if it was locally created.
     */
    public void endTransIfRequired() {
        if (createdTransaction) {
            // we can rollback as a readOnly transaction.
            trans.rollback();
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
