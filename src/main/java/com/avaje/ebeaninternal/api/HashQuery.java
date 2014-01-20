package com.avaje.ebeaninternal.api;

/**
 * A hash key for a query including both the query plan and bind values.
 */
public class HashQuery {

  private final HashQueryPlan planHash;
  
  private final int bindHash;

  /**
   * Create the HashQuery.
   */
  public HashQuery(HashQueryPlan planHash, int bindHash) {
    this.planHash = planHash;
    this.bindHash = bindHash;
  }

  /**
   * Return the query plan hash.
   */
  public HashQueryPlan getPlanHash() {
    return planHash;
  }

  /**
   * Return the bind values hash.
   */
  public int getBindHash() {
    return bindHash;
  }

  public int hashCode() {
    int hc = 31 * planHash.hashCode();
    hc = 31 * hc + bindHash;
    return hc;
  }
  
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof HashQuery)) {
      return false;
    }
    
    HashQuery e = (HashQuery) obj;
    return e.bindHash == bindHash && e.planHash.equals(planHash);
  }
}
