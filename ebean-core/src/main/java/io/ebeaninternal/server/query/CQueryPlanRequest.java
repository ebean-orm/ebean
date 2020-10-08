package io.ebeaninternal.server.query;

import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanRequest;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Captures database query plans.
 */
class CQueryPlanRequest {

  private final List<MetaQueryPlan> plans = new ArrayList<>();

  private final Connection connection;
  private final long since;
  private final int maxCount;
  private final long maxTime;
  private Iterator<CQueryBindCapture> iterator;

  CQueryPlanRequest(Connection connection, QueryPlanRequest req, Iterator<CQueryBindCapture> iterator) {
    this.connection = connection;
    this.iterator = iterator;
    this.maxCount = req.getMaxCount();
    long reqSince = req.getSince();
    this.since = (reqSince == 0) ? Long.MAX_VALUE: reqSince;
    long maxTimeMillis = req.getMaxTimeMillis();
    this.maxTime = maxTimeMillis > 0 ? System.currentTimeMillis() + maxTimeMillis : 0;
  }

  /**
   * Return the connection used to collect the db query plan.
   */
  Connection getConnection() {
    return connection;
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
  long getSince() {
    return since;
  }

  /**
   * Return the captured query plans.
   */
  List<MetaQueryPlan> getPlans() {
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
    if (next.collectQueryPlan(this)) {
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
