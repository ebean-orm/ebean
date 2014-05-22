package com.avaje.ebeaninternal.api;

/**
 * Used to build HashQueryPlan instances.
 */
public class HashQueryPlanBuilder {

  private int planHash;
  
  private int bindCount;

  private String rawSql;
  
  public HashQueryPlanBuilder() {
    this.planHash = 31;
  }

  public String toString() {
    return planHash+":"+bindCount+(rawSql != null ? ":r" : "");
  }
  
  /**
   * Add a class to the hash calculation.
   */
  public HashQueryPlanBuilder add(Class<?> cls) {
    planHash = planHash * 31 + cls.getName().hashCode();
    return this;
  }

  /**
   * Add an object to the hash calculation.
   */
  public HashQueryPlanBuilder add(Object object) {
    planHash = planHash * 31 + (object == null ? 0 : object.hashCode());
    return this;
  }

  /**
   * Add an integer to the hash calculation.
   */
  public HashQueryPlanBuilder add(int hashValue) {
    planHash = planHash * 31 + (hashValue);
    return this;
  }
  
  /**
   * Add a boolean to the hash calculation.
   */
  public HashQueryPlanBuilder add(boolean booleanValue) {
    planHash = planHash * 31 + (booleanValue ? 31 : 0);
    return this;
  }
  
  /**
   * Add a number to the bind count for the hash.
   */
  public HashQueryPlanBuilder bind(int extraBindCount) {
    bindCount += extraBindCount;
    return this;
  }

  /**
   * Add raw sql to the hash.
   */
  public HashQueryPlanBuilder addRawSql(String rawSql) {
    this.rawSql = rawSql;
    return this;
  }

  /**
   * Build and return the calculated HashQueryPlan.
   */
  public HashQueryPlan build() {
    return new HashQueryPlan(rawSql, planHash, bindCount);
  }

  
}
