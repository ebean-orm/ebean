package io.ebeaninternal.server.querydefn;

import io.ebeaninternal.api.CQueryPlanKey;
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

  OrmQueryPlanKey(String description, int maxRows, int firstRow, SpiRawSql rawSql) {
    this.description = description;
    this.maxRows = maxRows;
    this.firstRow = firstRow;
    this.rawSqlKey = (rawSql == null) ? null : rawSql.getKey();
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

  @Override
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
