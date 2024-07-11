package io.ebeaninternal.server.query;

import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanRequest;
import io.ebeaninternal.api.SpiTransactionManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Captures database query plans.
 */
final class CQueryPlanRequest {

  private final List<MetaQueryPlan> plans = new ArrayList<>();

  private final SpiTransactionManager transactionManager;
  private final long since;
  private final int maxCount;
  private final long maxTime;
  private final Iterator<CQueryBindCapture> iterator;


  CQueryPlanRequest(SpiTransactionManager transactionManager, QueryPlanRequest req, Iterator<CQueryBindCapture> iterator) {
    this.transactionManager = transactionManager;
    this.iterator = iterator;
    this.maxCount = req.maxCount();
    long reqSince = req.since();
    this.since = (reqSince == 0) ? Long.MAX_VALUE: reqSince;
    long maxTimeMillis = req.maxTimeMillis();
    this.maxTime = maxTimeMillis > 0 ? System.currentTimeMillis() + maxTimeMillis : 0;
  }

  /**
   * Add the collected query plan.
   */
  void add(MetaQueryPlan dbQueryPlan) {
    plans.add(dbQueryPlan);
  }

  /**
   * Return the min epoch time in millis for minimum bind capture age.
   */
  long since() {
    return since;
  }

  /**
   * Return the captured query plans.
   */
  List<MetaQueryPlan> plans() {
    return plans;
  }

  /**
   * Return true to continue database query plan capture.
   */
  boolean hasNext() {
    return moreByCount() && moreByTime() && iterator.hasNext();
  }

  /**
   * Capture the next database query plan.
   */
  void nextCapture() {
    final CQueryBindCapture next = iterator.next();
    if (next.collectQueryPlan(this, transactionManager)) {
      iterator.remove();
    }
  }

  private boolean moreByCount() {
    return maxCount == 0 || maxCount > plans.size();
  }

  private boolean moreByTime() {
    return maxTime == 0 || maxTime > System.currentTimeMillis();
  }
}
