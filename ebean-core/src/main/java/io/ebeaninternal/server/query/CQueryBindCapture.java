package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.bind.capture.BindCapture;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

final class CQueryBindCapture implements SpiQueryBindCapture {

  private static final double multiplier = 1.5d;

  private final ReentrantLock lock = new ReentrantLock();
  private final CQueryPlanManager manager;
  private final SpiQueryPlan queryPlan;

  private BindCapture bindCapture;
  private long queryTimeMicros;
  private long thresholdMicros;
  private long captureCount;

  private long lastBindCapture;

  CQueryBindCapture(CQueryPlanManager manager, SpiQueryPlan queryPlan, long thresholdMicros) {
    this.manager = manager;
    this.queryPlan = queryPlan;
    this.thresholdMicros = thresholdMicros;
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
  public boolean collectQueryPlan(CQueryPlanRequest request) {
    if (bindCapture == null || request.since() < lastBindCapture) {
      // no bind capture since the last capture
      return false;
    }

    final Instant whenCaptured = Instant.ofEpochMilli(this.lastBindCapture);
    final BindCapture last = this.bindCapture;
    final long startNanos = System.nanoTime();
    SpiDbQueryPlan queryPlan = manager.collectPlan(request.connection(), this.queryPlan, last);
    if (queryPlan != null) {
      final long captureMicros = TimeUnit.MICROSECONDS.convert(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
      request.add(queryPlan.with(queryTimeMicros, captureCount, captureMicros, whenCaptured));
      // effectively turn off bind capture for this plan
      thresholdMicros = Long.MAX_VALUE;
      return true;
    }
    return false;
  }

}
