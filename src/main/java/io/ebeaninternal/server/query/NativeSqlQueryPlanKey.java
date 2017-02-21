package io.ebeaninternal.server.query;

import io.ebeaninternal.api.CQueryPlanKey;

/**
 * QueryPlanKey for native sql queries.
 */
public class NativeSqlQueryPlanKey implements CQueryPlanKey {

  private final String sql;

  public NativeSqlQueryPlanKey(String sql) {
    this.sql = sql;
  }

  @Override
  public String toString() {
    return getPartialKey();
  }

  /**
   * Return as a partial key. For rawSql hash the sql is part of the key and as such
   * needs to be included in order to have a complete key. Typically the MD5 of the sql
   * can be used as a short form proxy for the actual sql.
   */
  @Override
  public String getPartialKey() {
    return hashCode() + "_n";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NativeSqlQueryPlanKey that = (NativeSqlQueryPlanKey) o;
    return sql.equals(that.sql);
  }

  @Override
  public int hashCode() {
    return sql.hashCode();
  }
}
