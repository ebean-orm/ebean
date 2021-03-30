package io.ebeaninternal.server.query;

import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanRequest;
import io.ebean.metric.TimedMetric;
import io.ebeaninternal.api.ExtraMetrics;
import io.ebeaninternal.api.QueryPlanManager;
import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.ebeaninternal.server.type.bindcapture.BindCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyList;

public class CQueryPlanManager implements QueryPlanManager {

  private static final Logger log = LoggerFactory.getLogger(CQueryPlanManager.class);

  private static final Object dummy = new Object();

  private final ConcurrentHashMap<CQueryBindCapture, Object> plans = new ConcurrentHashMap<>();

  private final TransactionManager transactionManager;

  private final QueryPlanLogger planLogger;

  private final TimedMetric timeCollection;

  private final TimedMetric timeBindCapture;

  private long defaultThreshold;

  public CQueryPlanManager(TransactionManager transactionManager, long defaultThreshold, QueryPlanLogger planLogger, ExtraMetrics extraMetrics) {
    this.transactionManager = transactionManager;
    this.defaultThreshold = defaultThreshold;
    this.planLogger = planLogger;
    this.timeCollection = extraMetrics.getPlanCollect();
    this.timeBindCapture = extraMetrics.getBindCapture();
  }

  @Override
  public void setDefaultThreshold(long thresholdMicros) {
    this.defaultThreshold = thresholdMicros;
  }

  @Override
  public SpiQueryBindCapture createBindCapture(SpiQueryPlan queryPlan) {
    return new CQueryBindCapture(this, queryPlan, defaultThreshold);
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
    try (Connection connection = transactionManager.getQueryPlanConnection()) {
      CQueryPlanRequest req = new CQueryPlanRequest(connection, request, plans.keySet().iterator());
      while (req.hasNext()) {
        req.nextCapture();
      }
      return req.getPlans();
    } catch (SQLException e) {
      log.error("Error during query plan collection", e);
      return emptyList();
    }
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
