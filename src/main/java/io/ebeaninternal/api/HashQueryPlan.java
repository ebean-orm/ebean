package io.ebeaninternal.api;

/**
 * A hash for a query plan.
 */
public class HashQueryPlan {

  private final String rawSql;

  private final int planHash;

  private final int bindCount;

  public HashQueryPlan(String rawSql, int planHash, int bindCount) {
    this.rawSql = rawSql;
    this.planHash = planHash;
    this.bindCount = bindCount;
  }

  @Override
  public String toString() {
    return planHash + ":" + bindCount + (rawSql != null ? ":r" : "");
  }

  /**
   * Return as a partial key. For rawSql hash the sql is part of the key and as such
   * needs to be included in order to have a complete key. Typically the MD5 of the sql
   * can be used as a shot form proxy for the actual sql.
   */
  public String getPartialKey() {
    return planHash + "_" + bindCount;
  }

  @Override
  public int hashCode() {
    int hc = planHash;
    hc = hc * 92821 + bindCount;
    hc = hc * 92821 + (rawSql == null ? 0 : rawSql.hashCode());
    return hc;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof HashQueryPlan)) {
      return false;
    }

    HashQueryPlan e = (HashQueryPlan) obj;
    //noinspection StringEquality
    return e.planHash == planHash
      && e.bindCount == bindCount
      && ((e.rawSql == rawSql) || (e.rawSql != null && e.rawSql.equals(rawSql)));
  }
}
