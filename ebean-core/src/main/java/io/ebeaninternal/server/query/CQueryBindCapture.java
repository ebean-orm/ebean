package io.ebeaninternal.server.query;

import io.ebean.config.CurrentTenantProvider;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.bind.capture.BindCapture;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.ERROR;

final class CQueryBindCapture implements SpiQueryBindCapture {

  private static final double multiplier = 1.5d;

  private final ReentrantLock lock = new ReentrantLock();
  private final CQueryPlanManager manager;
  private final SpiQueryPlan queryPlan;
  private final CurrentTenantProvider tenantProvider;

  private BindCapture bindCapture;
  private long queryTimeMicros;
  private long thresholdMicros;
  private long captureCount;
  private Object tenantId;

  private long lastBindCapture;

  CQueryBindCapture(CQueryPlanManager manager, SpiQueryPlan queryPlan, long thresholdMicros, CurrentTenantProvider tenantProvider) {
    this.manager = manager;
    this.queryPlan = queryPlan;
    this.thresholdMicros = thresholdMicros;
    this.tenantProvider = tenantProvider;
  }

  /**
   * Return true if we should capture the bind values for this query.
   */
  @Override
  public boolean collectFor(long timeMicros) {
    return timeMicros > thresholdMicros && captureCount < 10;
  }

  @Override
  public void setBind(BindCapture bindCapture, long queryTimeMicros, long startNanos) {
    lock.lock();
    try {
      this.thresholdMicros = Math.round(queryTimeMicros * multiplier);
      this.captureCount++;
      this.bindCapture = bindCapture;
      this.tenantId = tenantProvider == null ? null : tenantProvider.currentId();
      this.queryTimeMicros = queryTimeMicros;
      lastBindCapture = System.currentTimeMillis();
      manager.notifyBindCapture(this, startNanos);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void queryPlanInit(long thresholdMicros) {
    lock.lock();
    try {
      // effective enable bind capture for this plan
      this.thresholdMicros = thresholdMicros;
      this.captureCount = 0;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Collect the query plan using already captured bind values.
   */
  public boolean collectQueryPlan(CQueryPlanRequest request, SpiTransactionManager transactionManager) {
    if (bindCapture == null || request.since() < lastBindCapture) {
      // no bind capture since the last capture
      return false;
    }


    final Instant whenCaptured = Instant.ofEpochMilli(this.lastBindCapture);
    final BindCapture last = this.bindCapture;
    final Object tenantId = this.tenantId;
    final long startNanos = System.nanoTime();
    SpiDbQueryPlan queryPlan;
    try (Connection connection = transactionManager.queryPlanConnection(tenantId)) {
      queryPlan = manager.collectPlan(connection, this.queryPlan, last);
      if (!connection.getAutoCommit()) {
        // CHECKME: commit or rollback here?
        // arguments for rollback: the collecting should never modify data.
        // if there are collectors that may copy the plan into tables, it's up to the collector to
        // commit the transaction.
        connection.rollback();
      }
    } catch (SQLException e) {
      CoreLog.log.log(ERROR, "Error during query plan collection", e);
      return false;
    }
    if (queryPlan != null) {
      final long captureMicros = TimeUnit.MICROSECONDS.convert(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
      request.add(queryPlan.with(queryTimeMicros, captureCount, captureMicros, whenCaptured, tenantId));
      // effectively turn off bind capture for this plan
      thresholdMicros = Long.MAX_VALUE;
      return true;
    }
    return false;
  }

}
