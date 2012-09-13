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
package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.SqlLimitResponse;
import com.avaje.ebean.config.dbplatform.SqlLimiter;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryLimitRequest;
import com.avaje.ebeaninternal.server.util.BindParamsParser;

public class CQueryBuilderRawSql implements Constants {

    private final SqlLimiter sqlLimiter;
    private final DatabasePlatform dbPlatform;

    CQueryBuilderRawSql(SqlLimiter sqlLimiter, DatabasePlatform dbPlatform) {
        this.sqlLimiter = sqlLimiter;
        this.dbPlatform = dbPlatform;
    }
    
    /**
     * Build the full SQL Select statement for the request.
     */
    public SqlLimitResponse buildSql(OrmQueryRequest<?> request, CQueryPredicates predicates, RawSql.Sql rsql) {
        
        if (!rsql.isParsed()){
            String sql = rsql.getUnparsedSql();
            BindParams bindParams = request.getQuery().getBindParams();
            if (bindParams != null && bindParams.requiresNamedParamsPrepare()){
                // convert named parameters into positioned parameters
                sql = BindParamsParser.parse(bindParams, sql);
            }
            
            return new SqlLimitResponse(sql, false);
        }

        String orderBy = getOrderBy(predicates, rsql);
    
        // build the actual sql String
        String sql = buildMainQuery(orderBy, request, predicates, rsql);
        
        SpiQuery<?> query = request.getQuery();
        if (query.hasMaxRowsOrFirstRow() && sqlLimiter != null) {
            // wrap with a limit offset or ROW_NUMBER() etc
            return sqlLimiter.limit(new OrmQueryLimitRequest(sql, orderBy, query, dbPlatform));
            
        } else {
            // add back select keyword (it was removed to support sqlQueryLimiter)
            String prefix = "select "+ (rsql.isDistinct() ? "distinct " : "");
            sql = prefix + sql;
            return new SqlLimitResponse(sql, false);
        }
    }
    
    private String buildMainQuery(String orderBy, OrmQueryRequest<?> request, CQueryPredicates predicates, RawSql.Sql sql) {
        
        StringBuilder sb = new StringBuilder();
        sb.append(sql.getPreFrom());
        sb.append(" ");
        sb.append(NEW_LINE);
        
        String s = sql.getPreWhere();
        BindParams bindParams = request.getQuery().getBindParams();
        if (bindParams != null && bindParams.requiresNamedParamsPrepare()){
            // convert named parameters into positioned parameters
            // Named Parameters only allowed prior to dynamic where
            // clause (so not allowed in having etc - use unparsed)
            s = BindParamsParser.parse(bindParams, s);
        }
        sb.append(s);
        sb.append(" ");

        String dynamicWhere = null;
        if (request.getQuery().getId() != null) {
            // need to convert this as well. This avoids the
            // assumption that id has its proper dbColumn assigned
            // which may change if using multiple raw sql statements
            // against the same bean.
            BeanDescriptor<?> descriptor = request.getBeanDescriptor();
            //FIXME: I think this is broken... needs to be logical 
            // and then parsed for RawSqlSelect...
            dynamicWhere = descriptor.getIdBinderIdSql();
        }

        String dbWhere = predicates.getDbWhere();
        if (!isEmpty(dbWhere)) {
            if (dynamicWhere == null) {
                dynamicWhere = dbWhere;
            } else {
                dynamicWhere += " and " + dbWhere;
            }
        }

        if (!isEmpty(dynamicWhere)) {
            sb.append(NEW_LINE);            
            if (sql.isAndWhereExpr()) {
                sb.append("and ");
            } else {
                sb.append("where ");
            }
            sb.append(dynamicWhere);
            sb.append(" ");
        }

        String preHaving = sql.getPreHaving();
        if (!isEmpty(preHaving)) {
            sb.append(NEW_LINE);
            sb.append(preHaving);
            sb.append(" ");
        }

        String dbHaving = predicates.getDbHaving();
        if (!isEmpty(dbHaving)) {
            sb.append(" ");
            sb.append(NEW_LINE);
            if (sql.isAndHavingExpr()) {
                sb.append("and ");
            } else {
                sb.append("having ");
            }
            sb.append(dbHaving);
            sb.append(" ");
        }

        if (!isEmpty(orderBy)) {
            sb.append(NEW_LINE);
            sb.append(" order by ").append(orderBy);
        }

        return sb.toString().trim();
    }
    
    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private String getOrderBy(CQueryPredicates predicates, RawSql.Sql sql) {
        String orderBy = predicates.getDbOrderBy();
        if (orderBy != null) {
            return orderBy;
        } else {
            return sql.getOrderBy();          
        }
    }
}
