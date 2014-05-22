package com.avaje.ebeaninternal.api;

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

  public String toString() {
    return planHash+":"+bindCount+(rawSql != null ? ":r" : "");
  }
  
  public int hashCode() {
    int hc = planHash;
    hc = hc * 31 + bindCount;
    hc = hc * 31 + (rawSql == null ? 0 : rawSql.hashCode());
    return hc;
  }
  
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof HashQueryPlan)) {
      return false;
    }
    
    HashQueryPlan e = (HashQueryPlan) obj;
    return e.planHash == planHash 
        && e.bindCount == bindCount
        &&  ((e.rawSql == rawSql) || (e.rawSql != null && e.rawSql.equals(rawSql)));
  }
}
