package io.ebeaninternal.server.core;

import io.ebean.meta.*;

import java.util.List;
import java.util.function.Function;

/**
 * DefaultServer based implementation of MetaInfoManager.
 */
final class DefaultMetaInfoManager implements MetaInfoManager {

  private final DefaultServer server;
  private final Function<String, String> naming;

  DefaultMetaInfoManager(DefaultServer server, Function<String, String> naming) {
    this.server = server;
    this.naming = naming;
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
  public BasicMetricVisitor visitBasic() {
    BasicMetricVisitor basic = new BasicMetricVisitor(server.name(), naming);
    visitMetrics(basic);
    return basic;
  }

  @Override
  public MetricReportGenerator createReportGenerator() {
    return new HtmlMetricReportGenerator(server);
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
    public Function<String, String> namingConvention() {
      return MetricNamingMatch.INSTANCE;
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
