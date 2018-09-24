package io.ebeaninternal.server.core;

import io.ebean.meta.AbstractMetricVisitor;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaInfoManager;
import io.ebean.meta.MetaOrmQueryMetric;
import io.ebean.meta.MetaOrmQueryNode;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.MetricVisitor;

import java.util.ArrayList;
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
  public void visitMetrics(MetricVisitor visitor) {
    server.visitMetrics(visitor);
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

  @Override
  public List<MetaOrmQueryNode> collectNodeStatistics(boolean reset) {

    List<MetaOrmQueryNode> list = new ArrayList<>();
    for (CObjectGraphNodeStatistics nodeStatistics : server.objectGraphStats.values()) {
      if (!nodeStatistics.isEmpty()) {
        list.add(nodeStatistics.get(reset));
      }
    }
    return list;
  }

  /**
   * Visitor that resets the statistics but doesn't collect them.
   */
  private static class ResetVisitor extends AbstractMetricVisitor {

    ResetVisitor() {
      super(true, true, true);
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
    public void visitOrmQuery(MetaOrmQueryMetric metric) {
      // ignore
    }
  }

}
