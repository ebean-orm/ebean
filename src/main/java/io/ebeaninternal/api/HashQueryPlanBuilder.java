package io.ebeaninternal.api;

import java.util.Set;

/**
 * Used to build HashQueryPlan instances.
 */
public class HashQueryPlanBuilder {

  private int planHash;

  private int bindCount;

  public HashQueryPlanBuilder() {
    this.planHash = 92821;
  }

  public String toString() {
    return planHash + ":" + bindCount;
  }

  /**
   * Add a class to the hash calculation.
   */
  public HashQueryPlanBuilder add(Class<?> cls) {
    planHash = planHash * 92821 + cls.getName().hashCode();
    return this;
  }

  /**
   * Add an object to the hash calculation.
   */
  public HashQueryPlanBuilder add(Object object) {
    planHash = planHash * 92821 + (object == null ? 0 : object.hashCode());
    return this;
  }

  /**
   * Add the set with order being important.
   */
  public HashQueryPlanBuilder addOrdered(Set<?> set) {
    if (set == null) {
      add(false);
    } else {
      add(true);
      for (Object o : set) {
        add(o);
      }
    }
    return this;
  }

  /**
   * Add an integer to the hash calculation.
   */
  public HashQueryPlanBuilder add(int hashValue) {
    planHash = planHash * 92821 + (hashValue);
    return this;
  }

  /**
   * Add a boolean to the hash calculation.
   */
  public HashQueryPlanBuilder add(boolean booleanValue) {
    planHash = planHash * 92821 + (booleanValue ? 92821 : 0);
    return this;
  }

  /**
   * Add a number to the bind count for the hash.
   */
  public void bind(int extraBindCount) {
    bindCount += extraBindCount;
  }

  public void bindIfNotNull(Object someValue) {
    if (someValue != null) {
      bindCount++;
    }
  }

  /**
   * Build and return the calculated HashQueryPlan.
   */
  public String build() {
    return planHash + "_" + bindCount;
  }

  public int getPlanHash() {
    return planHash;
  }

  public int getBindCount() {
    return bindCount;
  }
}
