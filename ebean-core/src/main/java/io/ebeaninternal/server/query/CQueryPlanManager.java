package io.ebeaninternal.server.query;

import io.ebean.config.CurrentTenantProvider;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanRequest;
import io.ebean.metric.TimedMetric;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.ebeaninternal.server.bind.capture.BindCapture;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyList;

public final class CQueryPlanManager implements QueryPlanManager {

  private static final Object dummy = new Object();

  private final ConcurrentHashMap<CQueryBindCapture, Object> plans = new ConcurrentHashMap<>();
  private final TransactionManager transactionManager;
  private final CurrentTenantProvider tenantProvider;
  private final QueryPlanLogger planLogger;
  private final TimedMetric timeCollection;
  private final TimedMetric timeBindCapture;
  private long defaultThreshold;

  public CQueryPlanManager(TransactionManager transactionManager,
                           CurrentTenantProvider tenantProvider,
                           long defaultThreshold, QueryPlanLogger planLogger, ExtraMetrics extraMetrics) {
    this.transactionManager = transactionManager;
    this.tenantProvider = tenantProvider;
    this.defaultThreshold = defaultThreshold;
    this.planLogger = planLogger;
    this.timeCollection = extraMetrics.planCollect();
    this.timeBindCapture = extraMetrics.bindCapture();
  }

  @Override
  public void setDefaultThreshold(long thresholdMicros) {
    this.defaultThreshold = thresholdMicros;
  }

  @Override
  public SpiQueryBindCapture createBindCapture(SpiQueryPlan queryPlan) {
    return new CQueryBindCapture(this, queryPlan, defaultThreshold, tenantProvider);
  }

  public void notifyBindCapture(CQueryBindCapture planBind, long startNanos) {
    plans.put(planBind, dummy);
    timeBindCapture.addSinceNanos(startNanos);
  }

  @Override
  public List<MetaQueryPlan> collect(QueryPlanRequest request) {
    if (plans.isEmpty()) {
      return emptyList();
    }
    return collectPlans(request);
  }

  private List<MetaQueryPlan> collectPlans(QueryPlanRequest request) {

    CQueryPlanRequest req = new CQueryPlanRequest(transactionManager, request, plans.keySet().iterator());
      while (req.hasNext()) {
        req.nextCapture();
      }
      return req.plans();
  }

  public SpiDbQueryPlan collectPlan(Connection connection, SpiQueryPlan queryPlan, BindCapture last) {
    long startNanos = System.nanoTime();
    try {
      return planLogger.collectPlan(connection, queryPlan, last);
    } finally {
      timeCollection.addSinceNanos(startNanos);
    }
  }
}
