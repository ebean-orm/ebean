package io.ebeaninternal.server.core;

import io.ebean.meta.AbstractMetricVisitor;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaInfoManager;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.MetricData;
import io.ebean.meta.MetricVisitor;
import io.ebean.meta.QueryPlanInit;
import io.ebean.meta.QueryPlanRequest;
import io.ebean.meta.ServerMetrics;
import io.ebean.meta.ServerMetricsAsJson;

import java.util.List;

/**
 * DefaultServer based implementation of MetaInfoManager.
 */
public class DefaultMetaInfoManager implements MetaInfoManager {

  private final DefaultServer server;

  DefaultMetaInfoManager(DefaultServer server) {
    this.server = server;
  }

  @Override
  public List<MetaQueryPlan> queryPlanInit(QueryPlanInit initRequest) {
    return server.queryPlanInit(initRequest);
  }

  @Override
  public List<MetaQueryPlan> queryPlanCollectNow(QueryPlanRequest request) {
    return server.queryPlanCollectNow(request);
  }

  @Override
  public void visitMetrics(MetricVisitor visitor) {
    server.visitMetrics(visitor);
  }

  @Override
  public ServerMetrics collectMetrics() {
    return visitBasic();
  }

  @Override
  public ServerMetricsAsJson collectMetricsAsJson() {
    return new DumpMetricsJson(server);
  }

  @Override
  public List<MetricData> collectMetricsAsData() {
    return new DumpMetricsData(server).data();
  }

  @Override
  public BasicMetricVisitor visitBasic() {
    BasicMetricVisitor basic = new BasicMetricVisitor();
    visitMetrics(basic);
    return basic;
  }

  @Override
  public void resetAllMetrics() {
    server.visitMetrics(new ResetVisitor());
  }

  /**
   * Visitor that resets the statistics but doesn't collect them.
   */
  private static class ResetVisitor extends AbstractMetricVisitor {

    ResetVisitor() {
      super(true, true, true, true);
    }

    @Override
    public void visitTimed(MetaTimedMetric metric) {
      // ignore
    }

    @Override
    public void visitQuery(MetaQueryMetric metric) {
      // ignore
    }

    @Override
    public void visitCount(MetaCountMetric metric) {
      // ignore
    }
  }

}
