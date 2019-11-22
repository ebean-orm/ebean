package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import io.ebean.meta.MetaQueryPlan;

/**
 * Captured query plan details.
 */
class DQueryPlanOutput implements MetaQueryPlan {

  private final Class<?> beanType;
  private final String label;
  private final ProfileLocation profileLocation;

  private final String sql;
  private final String bind;
  private final String plan;

  private String sqlHash;
  private long queryTimeMicros;
  private long captureCount;

  DQueryPlanOutput(Class<?> beanType, String label, String sql, String bind, String plan, ProfileLocation profileLocation) {
    this.beanType = beanType;
    this.label = label;
    this.sql = sql;
    this.bind = bind;
    this.plan = plan;
    this.profileLocation = profileLocation;
  }

  @Override
  public String getSqlHash() {
    return sqlHash;
  }

  /**
   * Return the associated bean.
   */
  @Override
  public Class<?> getBeanType() {
    return beanType;
  }

  /**
   * Return the query label if set.
   */
  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public ProfileLocation getProfileLocation() {
    return profileLocation;
  }

  /**
   * Return the sql of query.
   */
  @Override
  public String getSql() {
    return sql;
  }

  /**
   * Return a description of the bind values used.
   */
  @Override
  public String getBind() {
    return bind;
  }

  /**
   * Return the query plan.
   */
  @Override
  public String getPlan() {
    return plan;
  }

  /**
   * Return the query execution time associated with the capture of bind values used
   * to build the query plan.
   */
  @Override
  public long getQueryTimeMicros() {
    return queryTimeMicros;
  }

  /**
   * Return the total count of times bind capture has occurred.
   */
  @Override
  public long getCaptureCount() {
    return captureCount;
  }

  @Override
  public String toString() {
    return " BeanType:" + ((beanType == null) ? "" : beanType.getSimpleName()) + " planHash:" + sqlHash + " label:" + label + " queryTimeMicros:" + queryTimeMicros + " captureCount:" + captureCount + "\n SQL:" + sql + "\nBIND:" + bind + "\nPLAN:" + plan;
  }

  /**
   * Additionally set the query execution time and the number of bind captures.
   */
  void with(long queryTimeMicros, long captureCount, String sqlHash) {
    this.queryTimeMicros = queryTimeMicros;
    this.captureCount = captureCount;
    this.sqlHash = sqlHash;
  }
}
