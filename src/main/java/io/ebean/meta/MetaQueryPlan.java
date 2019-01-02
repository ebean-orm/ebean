package io.ebean.meta;

public interface MetaQueryPlan {

  Class<?> getBeanType();

  /**
   * Return a string representation of the query plan hash.
   */
  String getQueryPlanHash();

  String getLabel();

  String getSql();

  String getBind();

  String getPlan();

  long getQueryTimeMicros();

  long getCaptureCount();
}
