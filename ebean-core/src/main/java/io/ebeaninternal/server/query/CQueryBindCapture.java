package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

import java.util.concurrent.locks.ReentrantLock;

class CQueryBindCapture implements SpiQueryBindCapture {

  private static final double multiplier = 1.3d;

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
    // effective enable bind capture for this plan
    this.thresholdMicros = thresholdMicros;
    this.captureCount = 0;
  }

  /**
   * Collect the query plan using already captured bind values.
   */
  public boolean collectQueryPlan(CQueryPlanRequest request) {
    if (bindCapture == null || request.getSince() < lastBindCapture) {
      // no bind capture since the last capture
      return false;
    }

    final BindCapture last = this.bindCapture;

    SpiDbQueryPlan queryPlan = manager.collectPlan(request.getConnection(), this.queryPlan, last);
    if (queryPlan != null) {
      request.add(queryPlan.with(queryTimeMicros, captureCount));
      // effectively turn off bind capture for this plan
      thresholdMicros = Long.MAX_VALUE;
      return true;
    }
    return false;
  }

}
