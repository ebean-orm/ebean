package com.avaje.ebeaninternal.api;

import java.util.Objects;

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
    hc = hc * 31 + Objects.hashCode(rawSql);
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
        && Objects.equals(e.rawSql, rawSql);
  }
}
