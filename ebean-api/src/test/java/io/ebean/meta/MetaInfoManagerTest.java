package io.ebean.meta;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetaInfoManagerTest {

  @Test
  void collectMetricsBooleanDefaultsToCollectMetrics() {
    ServerMetrics metrics = new BasicMetricVisitor();
    MetaInfoManager manager = new MetaInfoManager() {
      @Override
      public ServerMetrics collectMetrics() {
        return metrics;
      }

      @Override
      public void visitMetrics(MetricVisitor visitor) {
      }

      @Override
      public BasicMetricVisitor visitBasic() {
        return new BasicMetricVisitor();
      }

      @Override
      public void resetAllMetrics() {
      }

      @Override
      public List<MetaQueryPlan> queryPlanInit(QueryPlanInit initRequest) {
        return List.of();
      }

      @Override
      public List<MetaQueryPlan> queryPlanCollectNow(QueryPlanRequest request) {
        return List.of();
      }
    };

    assertThat(manager.collectMetrics(false)).isSameAs(metrics);
  }
}
