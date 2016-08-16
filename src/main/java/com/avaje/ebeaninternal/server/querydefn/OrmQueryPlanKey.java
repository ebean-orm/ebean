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

  private final String m2mIncludeTable;
  private final String orderByAsSting;
  private final SpiExpression where;
  private final SpiExpression having;
  private final RawSql.Key rawSqlKey;
  private final boolean hasIdValue;
  private final SpiQuery.Type type;
  private final int maxRows;
  private final int firstRow;
  private final boolean disableLazyLoading;
  private final boolean distinct;
  private final boolean sqlDistinct;
  private final String mapKey;
  private final SpiQuery.TemporalMode temporalMode;
  private final boolean forUpdate;
  private final String rootTableAlias;
  private final OrmUpdateProperties updateProperties;

  private final int planHash;
  private final int bindCount;

  public OrmQueryPlanKey(TableJoin m2mIncludeTable, SpiQuery.Type type, OrmQueryDetail detail, int maxRows, int firstRow, boolean disableLazyLoading, OrderBy<?> orderBy, boolean distinct, boolean sqlDistinct, String mapKey, Object id, BindParams bindParams, SpiExpression whereExpressions, SpiExpression havingExpressions, SpiQuery.TemporalMode temporalMode, boolean forUpdate, String rootTableAlias, RawSql rawSql, OrmUpdateProperties updateProperties) {

    this.m2mIncludeTable = m2mIncludeTable == null ? null : m2mIncludeTable.getTable();
    this.type = type;
    this.maxRows = maxRows;
    this.firstRow = firstRow;
    this.disableLazyLoading = disableLazyLoading;
    this.orderByAsSting = (orderBy == null) ? null : orderBy.toStringFormat();
    this.distinct = distinct;
    this.sqlDistinct = sqlDistinct;
    this.mapKey = mapKey;
    this.hasIdValue = (id != null);
    this.where = (whereExpressions == null) ? null : whereExpressions.copyForPlanKey();
    this.having = (havingExpressions == null) ? null : havingExpressions.copyForPlanKey();
    this.temporalMode = temporalMode;
    this.forUpdate = forUpdate;
    this.rootTableAlias = rootTableAlias;
    this.updateProperties = updateProperties;
    this.rawSqlKey = (rawSql == null) ? null : rawSql.getKey();

    // exclude bind values and things unrelated to the sql being generated
    HashQueryPlanBuilder builder = new HashQueryPlanBuilder();

    builder.add((type == null ? 0 : type.ordinal() + 1));
    builder.add(distinct).add(sqlDistinct);
    builder.add(firstRow).add(maxRows);
    builder.add(orderBy).add(forUpdate);
    builder.add(mapKey);
    builder.add(disableLazyLoading);
    builder.add(hasIdValue);
    builder.add(temporalMode);
    builder.add(rawSqlKey == null ? 0 : rawSqlKey.hashCode());
    builder.add(this.m2mIncludeTable);
    builder.add(rootTableAlias);

    if (detail != null) {
      detail.queryPlanHash(builder);
    }
    if (bindParams != null) {
      bindParams.buildQueryPlanHash(builder);
    }
    if (where != null) {
      where.queryPlanHash(builder);
    }
    if (having != null) {
      having.queryPlanHash(builder);
    }
    if (updateProperties != null) {
      updateProperties.buildQueryPlanHash(builder);
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
    if (m2mIncludeTable != null ? !m2mIncludeTable.equals(that.m2mIncludeTable) : that.m2mIncludeTable != null) return false;
    if (orderByAsSting != null ? !orderByAsSting.equals(that.orderByAsSting) : that.orderByAsSting != null) return false;
    if (where != null ? !where.isSameByPlan(that.where) : that.where != null) return false;
    if (having != null ? !having.isSameByPlan(that.having) : that.having != null) return false;
    if (updateProperties != null ? !updateProperties.isSameByPlan(that.updateProperties) : that.updateProperties != null) return false;
    if (rawSqlKey != null ? !rawSqlKey.equals(that.rawSqlKey) : that.rawSqlKey != null) return false;
    if (mapKey != null ? !mapKey.equals(that.mapKey) : that.mapKey != null) return false;
    return rootTableAlias != null ? rootTableAlias.equals(that.rootTableAlias) : that.rootTableAlias == null;
  }
}
