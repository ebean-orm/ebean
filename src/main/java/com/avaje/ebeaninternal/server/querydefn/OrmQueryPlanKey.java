package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.OrderBy;
import com.avaje.ebean.RawSql;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.CQueryPlanKey;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.TableJoin;

/**
 * Query plan key for ORM queries.
 */
public class OrmQueryPlanKey implements CQueryPlanKey {

  private final TableJoin includeTableJoin;
  private final String orderByAsSting;
  private final OrmQueryDetail detail;
  private final BindParams bindParams;
  private final SpiExpression whereExpressions;
  private final SpiExpression havingExpressions;
  private final RawSql rawSql;
  private final boolean hasIdValue;

  private final SpiQuery.Type type;
  private final int maxRows;
  private final int firstRow;
  private final boolean disableLazyLoading;
  private final String rawWhereClause;
  private final String query;
  private final String additionalWhere;
  private final String additionalHaving;
  private final boolean distinct;
  private final boolean sqlDistinct;
  private final String mapKey;
  private final SpiQuery.TemporalMode temporalMode;
  private final boolean forUpdate;
  private final String rootTableAlias;

  private final int planHash;
  private final int bindCount;

  public OrmQueryPlanKey(TableJoin includeTableJoin, SpiQuery.Type type, OrmQueryDetail detail, int maxRows, int firstRow, boolean disableLazyLoading, String rawWhereClause, OrderBy<?> orderBy, String query, String additionalWhere, String additionalHaving, boolean distinct, boolean sqlDistinct, String mapKey, Object id, BindParams bindParams, SpiExpression whereExpressions, SpiExpression havingExpressions, SpiQuery.TemporalMode temporalMode, boolean forUpdate, String rootTableAlias, RawSql rawSql) {

    this.includeTableJoin = includeTableJoin;
    this.type = type;
    this.detail = detail;
    this.maxRows = maxRows;
    this.firstRow = firstRow;
    this.disableLazyLoading = disableLazyLoading;
    this.rawWhereClause = rawWhereClause;
    this.orderByAsSting = (orderBy == null) ? null : orderBy.toStringFormat();
    this.query = query;
    this.additionalWhere = additionalWhere;
    this.additionalHaving = additionalHaving;
    this.distinct = distinct;
    this.sqlDistinct = sqlDistinct;
    this.mapKey = mapKey;
    this.hasIdValue = (id != null);
    this.bindParams = bindParams;
    this.whereExpressions = whereExpressions;
    this.havingExpressions = havingExpressions;
    this.temporalMode = temporalMode;
    this.forUpdate = forUpdate;
    this.rootTableAlias = rootTableAlias;
    this.rawSql = rawSql;

    // exclude bind values and things unrelated to the sql being generated
    HashQueryPlanBuilder builder = new HashQueryPlanBuilder();

    builder.add((type == null ? 0 : type.ordinal() + 1));
    builder.add(distinct).add(sqlDistinct).add(query);
    builder.add(firstRow).add(maxRows);
    builder.add(orderBy).add(forUpdate);
    builder.add(rawWhereClause).add(additionalWhere).add(additionalHaving);
    builder.add(mapKey);
    builder.add(disableLazyLoading);
    builder.add(hasIdValue);
    builder.add(temporalMode);
    builder.add(rawSql == null ? 0 : rawSql.queryHash());
    builder.add(includeTableJoin != null ? includeTableJoin.queryHash() : 0);
    builder.add(rootTableAlias);

    if (detail != null) {
      detail.queryPlanHash(builder);
    }
    if (bindParams != null) {
      bindParams.buildQueryPlanHash(builder);
    }
    if (whereExpressions != null) {
      whereExpressions.queryPlanHash(builder);
    }
    if (havingExpressions != null) {
      havingExpressions.queryPlanHash(builder);
    }

    this.planHash = builder.getPlanHash();
    this.bindCount = builder.getBindCount();
  }

  @Override
  public String getPartialKey() {
    return planHash + "_" + bindCount;
  }

  @Override
  public int hashCode() {
    return planHash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OrmQueryPlanKey that = (OrmQueryPlanKey) o;

    if (planHash != that.planHash) return false;
    if (bindCount != that.bindCount) return false;

    if (maxRows != that.maxRows) return false;
    if (firstRow != that.firstRow) return false;
    if (disableLazyLoading != that.disableLazyLoading) return false;

    if (distinct != that.distinct) return false;
    if (sqlDistinct != that.sqlDistinct) return false;
    if (forUpdate != that.forUpdate) return false;
    if (hasIdValue != that.hasIdValue) return false;
    if (type != that.type) return false;
    if (temporalMode != that.temporalMode) return false;
    if (includeTableJoin != null ? !includeTableJoin.equals(that.includeTableJoin) : that.includeTableJoin != null) return false;
    if (orderByAsSting != null ? !orderByAsSting.equals(that.orderByAsSting) : that.orderByAsSting != null) return false;

    if (whereExpressions != null ? !whereExpressions.isSameByPlan(that.whereExpressions) : that.whereExpressions != null) return false;
    if (havingExpressions != null ? !havingExpressions.isSameByPlan(that.havingExpressions) : that.havingExpressions != null) return false;

//    if (detail != null ? !detail.equals(that.detail) : that.detail != null) return false;
//    if (bindParams != null ? !bindParams.equals(that.bindParams) : that.bindParams != null) return false;
//    if (rawSql != null ? !rawSql.equals(that.rawSql) : that.rawSql != null) return false;

    if (rawWhereClause != null ? !rawWhereClause.equals(that.rawWhereClause) : that.rawWhereClause != null) return false;
    if (query != null ? !query.equals(that.query) : that.query != null) return false;
    if (additionalWhere != null ? !additionalWhere.equals(that.additionalWhere) : that.additionalWhere != null) return false;
    if (additionalHaving != null ? !additionalHaving.equals(that.additionalHaving) : that.additionalHaving != null) return false;
    if (mapKey != null ? !mapKey.equals(that.mapKey) : that.mapKey != null) return false;
    return rootTableAlias != null ? rootTableAlias.equals(that.rootTableAlias) : that.rootTableAlias == null;
  }
}
