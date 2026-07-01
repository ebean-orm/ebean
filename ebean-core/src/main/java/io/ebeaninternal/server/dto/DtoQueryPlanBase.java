package io.ebeaninternal.server.dto;

import io.ebean.ProfileLocation;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.QueryPlanMetric;
import io.ebean.metric.TimedMetric;
import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.bind.capture.BindCapture;
import io.ebeaninternal.server.query.DQueryPlanOutput;

abstract class DtoQueryPlanBase implements DtoQueryPlan, SpiQueryPlan {

  private final QueryPlanMetric planMetric;
  private final TimedMetric metric;
  private final Class<?> beanType;
  private final String name;
  private final String hash;
  private final String sql;
  private final ProfileLocation profileLocation;
  private final boolean nativeSql;
  private final SpiQueryBindCapture bindCapture;

  DtoQueryPlanBase(DtoMappingRequest request) {
    this.planMetric = request.createMetric();
    this.metric = planMetric.metric();
    this.beanType = request.type();
    this.name = request.name();
    this.hash = request.hash();
    this.sql = request.sql();
    this.profileLocation = request.profileLocation();
    this.nativeSql = request.nativeSql();
    this.bindCapture = request.createBindCapture(this);
  }

  @Override
  public void collect(long exeTime) {
    metric.add(exeTime);
  }

  @Override
  public boolean collectFor(long exeMicros) {
    return bindCapture.collectFor(exeMicros);
  }

  @Override
  public boolean supportsPlanCapture() {
    return nativeSql;
  }

  @Override
  public void setBind(BindCapture capture, long exeMicros, long startNanos) {
    bindCapture.setBind(capture, exeMicros, startNanos);
  }

  @Override
  public void visit(MetricVisitor visitor) {
    planMetric.visit(visitor);
  }

  @Override
  public Class<?> beanType() {
    return beanType;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String hash() {
    return hash;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public ProfileLocation profileLocation() {
    return profileLocation;
  }

  @Override
  public void queryPlanInit(long thresholdMicros) {
    bindCapture.queryPlanInit(thresholdMicros);
  }

  @Override
  public SpiDbQueryPlan createMeta(String bind, String planString) {
    return new DQueryPlanOutput(beanType, name, hash, sql, profileLocation, bind, planString);
  }
}
