package io.ebeaninternal.server.profile;

import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaQueryMetric;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class DQueryPlanMetricTest {

  Function<String, String> naming = (String name) -> "prefix[" + name.replace('.', '-') + "]";

  @Test
  void visit() {

    DQueryPlanMeta meta = new DQueryPlanMeta(Object.class, "dto.Object.lab", "lab", null, "sql", "hash");
    DTimedMetric metric = new DTimedMetric("org.timed.plan");
    DQueryPlanMetric planMetric = new DQueryPlanMetric(meta, metric);

    metric.add(560);
    metric.add(260);
    {
      BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming);
      planMetric.visit(visitor);
      List<MetaQueryMetric> result = visitor.queryMetrics();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).name()).isEqualTo("prefix[dto-Object-lab]");
      assertThat(result.get(0).count()).isEqualTo(2);
      assertThat(result.get(0).total()).isEqualTo(820);
    }
    metric.add(410);
    {
      BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming);
      planMetric.visit(visitor);
      List<MetaQueryMetric> result = visitor.queryMetrics();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).name()).isEqualTo("prefix[dto-Object-lab]");
      assertThat(result.get(0).count()).isEqualTo(1);
      assertThat(result.get(0).total()).isEqualTo(410);
    }
  }

  @Test
  void visitCumulativeResetsMax() {

    DQueryPlanMeta meta = new DQueryPlanMeta(Object.class, "dto.Object.lab", "lab", null, "sql", "hash");
    DTimedMetric metric = new DTimedMetric("org.timed.plan");
    DQueryPlanMetric planMetric = new DQueryPlanMetric(meta, metric);

    metric.add(560);
    metric.add(260);
    {
      BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming, false, true, true, true);
      planMetric.visit(visitor);
      List<MetaQueryMetric> result = visitor.queryMetrics();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).name()).isEqualTo("prefix[dto-Object-lab]");
      assertThat(result.get(0).count()).isEqualTo(2);
      assertThat(result.get(0).total()).isEqualTo(820);
      assertThat(result.get(0).max()).isEqualTo(560);
    }
    {
      BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming, false, true, true, true);
      planMetric.visit(visitor);
      List<MetaQueryMetric> result = visitor.queryMetrics();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).name()).isEqualTo("prefix[dto-Object-lab]");
      assertThat(result.get(0).count()).isEqualTo(2);
      assertThat(result.get(0).total()).isEqualTo(820);
      assertThat(result.get(0).max()).isEqualTo(0);
    }

    metric.add(410);
    {
      BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming, false, true, true, true);
      planMetric.visit(visitor);
      List<MetaQueryMetric> result = visitor.queryMetrics();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).name()).isEqualTo("prefix[dto-Object-lab]");
      assertThat(result.get(0).count()).isEqualTo(3);
      assertThat(result.get(0).total()).isEqualTo(1230);
      assertThat(result.get(0).max()).isEqualTo(410);
    }
  }
}
