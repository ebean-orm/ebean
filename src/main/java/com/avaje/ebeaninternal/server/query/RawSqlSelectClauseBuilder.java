package com.avaje.ebeaninternal.server.query;

import javax.persistence.PersistenceException;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.SqlLimitResponse;
import com.avaje.ebean.config.dbplatform.SqlLimiter;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.DRawSqlSelect;
import com.avaje.ebeaninternal.server.deploy.DeployNamedQuery;
import com.avaje.ebeaninternal.server.deploy.DeployParser;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryLimitRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for SqlSelectClause based on raw sql.
 * <p>
 * Its job is to execute the sql, read the meta data to determine the columns to
 * bean property mapping.
 * </p>
 */
public class RawSqlSelectClauseBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RawSqlSelectClauseBuilder.class);

    private final Binder binder;

    private final SqlLimiter dbQueryLimiter;
    private final DatabasePlatform dbPlatform;

    public RawSqlSelectClauseBuilder(DatabasePlatform dbPlatform, Binder binder) {

        this.binder = binder;
        this.dbQueryLimiter = dbPlatform.getSqlLimiter();
        this.dbPlatform = dbPlatform;
    }

    /**
     * Build based on the includes and using the BeanJoinTree.
     */
    public <T> CQuery<T> build(OrmQueryRequest<T> request) throws PersistenceException {

        SpiQuery<T> query = request.getQuery();
        BeanDescriptor<T> desc = request.getBeanDescriptor();

        DeployNamedQuery namedQuery = desc.getNamedQuery(query.getName());
        DRawSqlSelect sqlSelect = namedQuery.getSqlSelect();

        // create a parser for this specific SqlSelect... has to be really
        // as each SqlSelect could have different table alias etc
        DeployParser parser = sqlSelect.createDeployPropertyParser();

        CQueryPredicates predicates = new CQueryPredicates(binder, request);
        // prepare and convert logical property names to dbColumns etc
        predicates.prepareRawSql(parser);

        SqlTreeAlias alias = new SqlTreeAlias(sqlSelect.getTableAlias());
        predicates.parseTableAlias(alias);

        String sql = null;
        try {

            boolean includeRowNumColumn = false;
            String orderBy = sqlSelect.getOrderBy(predicates);

            // build the actual sql String
            sql = sqlSelect.buildSql(orderBy, predicates, request);
            if (query.hasMaxRowsOrFirstRow() && dbQueryLimiter != null) {
                // wrap with a limit offset or ROW_NUMBER() etc
                SqlLimitResponse limitSql = dbQueryLimiter.limit(new OrmQueryLimitRequest(sql, orderBy, query, dbPlatform));
                includeRowNumColumn = limitSql.isIncludesRowNumberColumn();

                sql = limitSql.getSql();
            } else {
                // add back select keyword
                // ... was removed to support dbQueryLimiter
                sql = "select " + sql;
            }

            SqlTree sqlTree = sqlSelect.getSqlTree();

            CQueryPlan queryPlan = new CQueryPlan(request, sql, sqlTree, true, includeRowNumColumn, "");
            CQuery<T> compiledQuery = new CQuery<T>(request, predicates, queryPlan);

            return compiledQuery;

        } catch (Exception e) {

            String msg = "Error with " + desc.getFullName() + " query:\r" + sql;
            logger.error(msg);
            throw new PersistenceException(e);
        }
    }

}
