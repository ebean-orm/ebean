package io.ebeaninternal.server.querydefn;

import io.ebean.OrderBy;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.CQueryPlanKey;
import io.ebeaninternal.api.HashQueryPlanBuilder;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.TableJoin;

/**
 * Query plan key for ORM queries.
 */
class OrmQueryPlanKey implements CQueryPlanKey {

  private final SpiExpression where;
  private final SpiExpression having;
  private final RawSql.Key rawSqlKey;
  private final int maxRows;
  private final int firstRow;
  private final OrmUpdateProperties updateProperties;

  private final int planHash;
  private final int bindCount;
  private final String options;

  OrmQueryPlanKey(String discValue, TableJoin m2mIncludeTable, SpiQuery.Type type, OrmQueryDetail detail, int maxRows, int firstRow, boolean disableLazyLoading,
                  OrderBy<?> orderBy, boolean distinct, boolean sqlDistinct, String mapKey, Object id, BindParams bindParams,
                  SpiExpression whereExpressions, SpiExpression havingExpressions, SpiQuery.TemporalMode temporalMode,
                  Query.ForUpdate forUpdate, String rootTableAlias, RawSql rawSql, OrmUpdateProperties updateProperties) {

    StringBuilder sb = new StringBuilder(300);
    if (type != null) {
      sb.append("t:").append(type.ordinal());
    }
    if (discValue != null) {
      sb.append("disc:").append(discValue);
    }
    if (temporalMode != SpiQuery.TemporalMode.CURRENT) {
      sb.append(",temp:").append(temporalMode.ordinal());
    }
    if (forUpdate != null) {
      sb.append(",forUpd:").append(forUpdate.ordinal());
    }
    if (id != null) {
      sb.append(",id:");
    }
    if (distinct) {
      sb.append(",dist:");
    }
    if (sqlDistinct) {
      sb.append(",sqlD:");
    }
    if (disableLazyLoading) {
      sb.append(",disLazy:");
    }
    if (rootTableAlias != null) {
      sb.append(",root:").append(rootTableAlias);
    }
    if (orderBy != null) {
      sb.append(",orderBy:").append(orderBy.toStringFormat());
    }
    if (m2mIncludeTable != null) {
      sb.append(",m2m:").append(m2mIncludeTable.getTable());
    }
    if (mapKey != null) {
      sb.append(",mapKey:").append(mapKey);
    }
    this.options = sb.toString();
    this.maxRows = maxRows;
    this.firstRow = firstRow;
    this.where = (whereExpressions == null) ? null : whereExpressions.copyForPlanKey();
    this.having = (havingExpressions == null) ? null : havingExpressions.copyForPlanKey();
    this.updateProperties = updateProperties;
    this.rawSqlKey = (rawSql == null) ? null : rawSql.getKey();

    // exclude bind values and things unrelated to the sql being generated
    HashQueryPlanBuilder builder = new HashQueryPlanBuilder();
    builder.add(options.hashCode());
    builder.add(firstRow).add(maxRows);
    builder.add(rawSqlKey == null ? 0 : rawSqlKey.hashCode());

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
    if (!options.equals(that.options)) return false;
    if (where != null ? !where.isSameByPlan(that.where) : that.where != null) return false;
    if (having != null ? !having.isSameByPlan(that.having) : that.having != null) return false;
    if (updateProperties != null ? !updateProperties.isSameByPlan(that.updateProperties) : that.updateProperties != null) return false;
    return rawSqlKey != null ? rawSqlKey.equals(that.rawSqlKey) : that.rawSqlKey == null;
  }
}
