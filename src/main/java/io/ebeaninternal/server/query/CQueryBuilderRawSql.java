package io.ebeaninternal.server.query;

import io.ebeaninternal.server.rawsql.SpiRawSql;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.querydefn.OrmQueryLimitRequest;
import io.ebeaninternal.server.util.BindParamsParser;

class CQueryBuilderRawSql {

  private final SqlLimiter sqlLimiter;
  private final DatabasePlatform dbPlatform;

  CQueryBuilderRawSql(SqlLimiter sqlLimiter, DatabasePlatform dbPlatform) {
    this.sqlLimiter = sqlLimiter;
    this.dbPlatform = dbPlatform;
  }

  /**
   * Build the full SQL Select statement for the request.
   */
  SqlLimitResponse buildSql(OrmQueryRequest<?> request, CQueryPredicates predicates, SpiRawSql.Sql rsql) {

    if (rsql == null) {
      // this is a ResultSet based RawSql query - just use some placeholder for the SQL
      return new SqlLimitResponse("--ResultSetBasedRawSql", false);
    }

    if (!rsql.isParsed()) {
      String sql = rsql.getUnparsedSql();
      BindParams bindParams = request.getQuery().getBindParams();
      if (bindParams != null && bindParams.requiresNamedParamsPrepare()) {
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
      String prefix = "select " + (rsql.isDistinct() ? "distinct " : "");
      sql = prefix + sql;
      return new SqlLimitResponse(sql, false);
    }
  }

  private String buildMainQuery(String orderBy, OrmQueryRequest<?> request, CQueryPredicates predicates, SpiRawSql.Sql sql) {

    StringBuilder sb = new StringBuilder();
    sb.append(sql.getPreFrom());
    sb.append(" ");

    String s = sql.getPreWhere();
    BindParams bindParams = request.getQuery().getBindParams();
    if (bindParams != null && bindParams.requiresNamedParamsPrepare()) {
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
      dynamicWhere = descriptor.getIdBinderIdSql(null);
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
      if (sql.isAndWhereExpr()) {
        sb.append(" and ");
      } else {
        sb.append(" where ");
      }
      sb.append(dynamicWhere);
      sb.append(" ");
    }

    String preHaving = sql.getPreHaving();
    if (!isEmpty(preHaving)) {
      sb.append(preHaving);
      sb.append(" ");
    }

    String dbHaving = predicates.getDbHaving();
    if (!isEmpty(dbHaving)) {
      sb.append(" ");
      if (sql.isAndHavingExpr()) {
        sb.append("and ");
      } else {
        sb.append("having ");
      }
      sb.append(dbHaving);
      sb.append(" ");
    }

    if (!isEmpty(orderBy)) {
      sb.append(" ").append(sql.getOrderByPrefix()).append(" ").append(orderBy);
    }

    return sb.toString().trim();
  }

  private boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }

  private String getOrderBy(CQueryPredicates predicates, SpiRawSql.Sql sql) {
    String orderBy = predicates.getDbOrderBy();
    if (orderBy != null) {
      return orderBy;
    } else {
      return sql.getOrderBy();
    }
  }
}
