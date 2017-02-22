package io.ebeaninternal.server.query;

import io.ebeaninternal.api.CQueryPlanKey;

/**
 * QueryPlanKey for RawSql queries.
 */
class RawSqlQueryPlanKey implements CQueryPlanKey {

  private final String sql;
  private final boolean rawSql;
  private final boolean rowNumberIncluded;
  private final String logWhereSql;

  RawSqlQueryPlanKey(String sql, boolean rawSql, boolean rowNumberIncluded, String logWhereSql) {
    this.sql = sql;
    this.rawSql = rawSql;
    this.rowNumberIncluded = rowNumberIncluded;
    this.logWhereSql = logWhereSql;
  }

  @Override
  public String toString() {
    return getPartialKey() + ":r";
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
    if (rowNumberIncluded != that.rowNumberIncluded) return false;
    if (!sql.equals(that.sql)) return false;
    return logWhereSql != null ? logWhereSql.equals(that.logWhereSql) : that.logWhereSql == null;
  }

  @Override
  public int hashCode() {
    int result = sql.hashCode();
    result = 92821 * result + (rawSql ? 1 : 0);
    result = 92821 * result + (rowNumberIncluded ? 1 : 0);
    result = 92821 * result + logWhereSql.hashCode();
    return result;
  }
}
