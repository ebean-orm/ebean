package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import io.ebean.meta.MetaQueryPlan;
import io.ebeaninternal.api.SpiDbQueryPlan;

/**
 * Captured query plan details.
 */
final class DQueryPlanOutput implements MetaQueryPlan, SpiDbQueryPlan {

  private final Class<?> beanType;
  private final String label;
  private final ProfileLocation profileLocation;

  private final String sql;
  private final String bind;
  private final String plan;
  private final String hash;
  private long queryTimeMicros;
  private long captureCount;

  DQueryPlanOutput(Class<?> beanType, String label, String hash, String sql, ProfileLocation profileLocation, String bind, String plan) {
    this.beanType = beanType;
    this.label = label;
    this.hash = hash;
    this.sql = sql;
    this.profileLocation = profileLocation;
    this.bind = bind;
    this.plan = plan;
  }

  @Override
  public String hash() {
    return hash;
  }

  /**
   * Return the associated bean.
   */
  @Override
  public Class<?> beanType() {
    return beanType;
  }

  /**
   * Return the query label if set.
   */
  @Override
  public String label() {
    return label;
  }

  @Override
  public ProfileLocation profileLocation() {
    return profileLocation;
  }

  /**
   * Return the sql of query.
   */
  @Override
  public String sql() {
    return sql;
  }

  /**
   * Return a description of the bind values used.
   */
  @Override
  public String bind() {
    return bind;
  }

  /**
   * Return the query plan.
   */
  @Override
  public String plan() {
    return plan;
  }

  /**
   * Return the query execution time associated with the capture of bind values used
   * to build the query plan.
   */
  @Override
  public long queryTimeMicros() {
    return queryTimeMicros;
  }

  /**
   * Return the total count of times bind capture has occurred.
   */
  @Override
  public long captureCount() {
    return captureCount;
  }

  @Override
  public String toString() {
    return " BeanType:" + ((beanType == null) ? "" : beanType.getSimpleName()) + " planHash:" + hash + " label:" + label + " queryTimeMicros:" + queryTimeMicros + " captureCount:" + captureCount + "\n SQL:" + sql + "\nBIND:" + bind + "\nPLAN:" + plan;
  }

  /**
   * Additionally set the query execution time and the number of bind captures.
   */
  @Override
  public DQueryPlanOutput with(long queryTimeMicros, long captureCount) {
    this.queryTimeMicros = queryTimeMicros;
    this.captureCount = captureCount;
    return this;
  }
}
