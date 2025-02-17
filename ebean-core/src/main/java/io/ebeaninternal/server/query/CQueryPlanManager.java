package io.ebeaninternal.server.query;

import io.ebean.config.QueryPlanCapture;
import io.ebean.config.QueryPlanListener;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanRequest;
import io.ebean.metric.TimedMetric;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.core.DefaultServer;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.ebeaninternal.server.bind.capture.BindCapture;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Collections.emptyList;

public final class CQueryPlanManager implements QueryPlanManager {

  private static final System.Logger log = CoreLog.internal;
  private static final Object dummy = new Object();

  private final ConcurrentHashMap<CQueryBindCapture, Object> plans = new ConcurrentHashMap<>();
  private final TransactionManager transactionManager;
  private final QueryPlanLogger planLogger;
  private final TimedMetric timeCollection;
  private final TimedMetric timeBindCapture;
  private final DefaultServer server;
  private final QueryPlanListener listener;
  private final int maxCount;
  private final long maxTimeMillis;
  private long defaultThreshold;

  public CQueryPlanManager(DefaultServer server, TransactionManager transactionManager, long defaultThreshold, QueryPlanLogger planLogger, ExtraMetrics extraMetrics) {
    this.server = server;
    this.transactionManager = transactionManager;
    this.defaultThreshold = defaultThreshold;
    this.planLogger = planLogger;
    this.timeCollection = extraMetrics.planCollect();
    this.timeBindCapture = extraMetrics.bindCapture();

    final var config = server.config();
    this.maxCount = config.getQueryPlanCaptureMaxCount();
    this.maxTimeMillis = config.getQueryPlanCaptureMaxTimeMillis();
    final var planListener = config.getQueryPlanListener();
    this.listener = (planListener != null) ? planListener : DefaultQueryPlanListener.INSTANT;
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
  public void startPlanCapture() {
    final var config = server.config();
    if (config.isQueryPlanCapture()) {
      long secs = config.getQueryPlanCapturePeriodSecs();
      if (secs > 10) {
        log.log(INFO, "capture query plan enabled, every {0}secs", secs);
        server.backgroundExecutor().scheduleWithFixedDelay(this::collectQueryPlans, secs, secs, TimeUnit.SECONDS);
      }
    }
  }

  private void collectQueryPlans() {
    List<MetaQueryPlan> plans = collect(new QueryPlanRequest(maxCount, maxTimeMillis));
    listener.process(new QueryPlanCapture(server, plans));
  }

  @Override
  public List<MetaQueryPlan> collect(QueryPlanRequest request) {
    if (plans.isEmpty()) {
      return emptyList();
    }
    try (Connection connection = transactionManager.queryPlanConnection()) {
      CQueryPlanRequest req = new CQueryPlanRequest(connection, request, plans.keySet().iterator());
      while (req.hasNext()) {
        req.nextCapture();
      }
      return req.plans();
    } catch (SQLException e) {
      CoreLog.log.log(ERROR, "Error during query plan collection", e);
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
