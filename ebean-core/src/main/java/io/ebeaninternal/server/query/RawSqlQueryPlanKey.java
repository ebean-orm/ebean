package io.ebeaninternal.server.query;

import io.ebeaninternal.api.CQueryPlanKey;

import java.util.Objects;

/**
 * QueryPlanKey for RawSql queries.
 */
final class RawSqlQueryPlanKey implements CQueryPlanKey {

  private final String sql;
  private final boolean rawSql;
  private final String logWhereSql;

  RawSqlQueryPlanKey(String sql, boolean rawSql, String logWhereSql) {
    this.sql = sql;
    this.rawSql = rawSql;
    this.logWhereSql = logWhereSql;
  }

  @Override
  public String toString() {
    return getPartialKey() + ":r";
  }

  @Override
  public CQueryPlanKey withDeleteByIds() {
    throw new IllegalStateException("Not allowed");
  }

  /**
   * Return as a partial key. For rawSql hash the sql is part of the key and as such
   * needs to be included in order to have a complete key. Typically the MD5 of the sql
   * can be used as a short form proxy for the actual sql.
   */
  @Override
  public String getPartialKey() {
    return hashCode() + "_0";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RawSqlQueryPlanKey that = (RawSqlQueryPlanKey) o;

    if (rawSql != that.rawSql) return false;
    if (!sql.equals(that.sql)) return false;
    return Objects.equals(logWhereSql, that.logWhereSql);
  }

  @Override
  public int hashCode() {
    int result = sql.hashCode();
    result = 92821 * result + (rawSql ? 1 : 0);
    result = 92821 * result + logWhereSql.hashCode();
    return result;
  }
}
