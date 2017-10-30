package io.ebeaninternal.server.querydefn;

import io.ebean.OrderBy;
import io.ebean.Query;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.CQueryPlanKey;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.rawsql.SpiRawSql;

/**
 * Query plan key for ORM queries.
 */
class OrmQueryPlanKey implements CQueryPlanKey {

  private final SpiRawSql.Key rawSqlKey;
  private final int maxRows;
  private final int firstRow;
  private final int planHash;
  private final String description;

  OrmQueryPlanKey(String discValue, TableJoin m2mIncludeTable, SpiQuery.Type type, OrmQueryDetail detail, int maxRows, int firstRow, boolean disableLazyLoading,
                  OrderBy<?> orderBy, boolean distinct, boolean sqlDistinct, String mapKey, Object id, BindParams bindParams,
                  SpiExpression whereExpressions, SpiExpression havingExpressions, SpiQuery.TemporalMode temporalMode,
                  Query.ForUpdate forUpdate, String rootTableAlias, SpiRawSql rawSql, OrmUpdateProperties updateProperties) {

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
    this.maxRows = maxRows;
    this.firstRow = firstRow;
    this.rawSqlKey = (rawSql == null) ? null : rawSql.getKey();

    if (detail != null) {
      sb.append(" detail[");
      detail.queryPlanHash(sb);
      sb.append("]");
    }
    if (bindParams != null) {
      sb.append(" bindParams[");
      bindParams.buildQueryPlanHash(sb);
      sb.append("]");
    }
    if (whereExpressions != null) {
      sb.append(" where[");
      whereExpressions.queryPlanHash(sb);
      sb.append("]");
    }
    if (havingExpressions != null) {
      sb.append(" having[");
      havingExpressions.queryPlanHash(sb);
      sb.append("]");
    }
    if (updateProperties != null) {
      sb.append(" update[");
      updateProperties.buildQueryPlanHash(sb);
      sb.append("]");
    }

    this.description = sb.toString();
    int hc = description.hashCode();
    hc = hc * 92821 + (maxRows);
    hc = hc * 92821 + (firstRow);
    this.planHash = hc;
  }

  @Override
  public String getPartialKey() {
    return description;
  }

  @Override
  public int hashCode() {
    return planHash;
  }

  public String toString() {
    return description + " maxRows:" + maxRows + " firstRow:" + firstRow + " rawSqlKey:" + rawSqlKey + " planHash:" + planHash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OrmQueryPlanKey that = (OrmQueryPlanKey) o;

    if (maxRows != that.maxRows) return false;
    if (firstRow != that.firstRow) return false;
    if (!description.equals(that.description)) return false;
    return rawSqlKey != null ? rawSqlKey.equals(that.rawSqlKey) : that.rawSqlKey == null;
  }
}
