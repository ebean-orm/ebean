package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import io.ebean.meta.MetaQueryPlan;
import io.ebeaninternal.api.SpiDbQueryPlan;

import java.time.Instant;

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
  private long captureMicros;
  private Instant whenCaptured;
  private Object tenantId;

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

  @Override
  public Class<?> beanType() {
    return beanType;
  }

  @Override
  public String label() {
    return label;
  }

  @Override
  public ProfileLocation profileLocation() {
    return profileLocation;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public String bind() {
    return bind;
  }

  @Override
  public String plan() {
    return plan;
  }

  @Override
  public Object tenantId() {
    return tenantId;
  }

  @Override
  public long queryTimeMicros() {
    return queryTimeMicros;
  }

  @Override
  public long captureCount() {
    return captureCount;
  }

  @Override
  public long captureMicros() {
    return captureMicros;
  }

  @Override
  public Instant whenCaptured() {
    return whenCaptured;
  }

  @Override
  public String toString() {
    return " BeanType:" + ((beanType == null) ? "" : beanType.getSimpleName())
      + " planHash:" + hash
      + " label:" + label
      + " queryTimeMicros:" + queryTimeMicros
      + " captureCount:" + captureCount
      + (tenantId == null ? "" : (" tenant:" + tenantId))
      + "\n SQL:" + sql
      + "\nBIND:" + bind
      + "\nPLAN:" + plan;
  }

  @Override
  public DQueryPlanOutput with(long queryTimeMicros, long captureCount, long captureMicros, Instant whenCaptured, Object tenantId) {
    this.queryTimeMicros = queryTimeMicros;
    this.captureCount = captureCount;
    this.captureMicros = captureMicros;
    this.whenCaptured = whenCaptured;
    this.tenantId = tenantId;
    return this;
  }
}
