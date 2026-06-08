package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.bind.capture.BindCapture;
import io.ebeaninternal.server.util.Md5;

/**
 * Query plan for a native SQL {@code SqlQuery}.
 * <p>
 * Unlike ORM and DTO query plans there is no associated bean type. The timing
 * metric is held separately by the relational query engine (the {@code sql.query.*}
 * {@code TimedMetricMap}); this plan only owns the bind capture used to later
 * collect the database query plan via EXPLAIN.
 */
public final class SqlQueryPlan implements SpiQueryPlan {

  private final String name;
  private final String hash;
  private final String sql;
  private final SpiQueryBindCapture bindCapture;

  SqlQueryPlan(SpiEbeanServer server, String name, String sql) {
    this.name = name;
    this.sql = sql;
    this.hash = Md5.hash(sql, name);
    this.bindCapture = server.createQueryBindCapture(this);
  }

  /**
   * Return true if the bind values for this query should be captured (based on
   * the query execution time exceeding the armed threshold).
   */
  public boolean collectFor(long exeMicros) {
    return bindCapture.collectFor(exeMicros);
  }

  /**
   * Set the captured bind values used to later collect the database query plan.
   */
  public void setBind(BindCapture capture, long exeMicros, long startNanos) {
    bindCapture.setBind(capture, exeMicros, startNanos);
  }

  @Override
  public Class<?> beanType() {
    return null;
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
    return null;
  }

  @Override
  public void queryPlanInit(long thresholdMicros) {
    bindCapture.queryPlanInit(thresholdMicros);
  }

  @Override
  public SpiDbQueryPlan createMeta(String bind, String planString) {
    return new DQueryPlanOutput(null, name, hash, sql, null, bind, planString);
  }
}
