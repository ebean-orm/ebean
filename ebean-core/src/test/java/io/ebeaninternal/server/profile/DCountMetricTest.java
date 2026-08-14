package io.ebeaninternal.server.profile;

import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetricVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class DCountMetricTest {

  Function<String, String> naming = (String name) -> "prefix[" + name.replace('.', '-') + "]";

  @Test
  void visit() {

    DCountMetric counter = new DCountMetric("org.hello");
    counter.add(7);
    {
      BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming);
      counter.visit(visitor);
      List<MetaCountMetric> result = visitor.countMetrics();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).name()).isEqualTo("prefix[org-hello]");
      assertThat(result.get(0).count()).isEqualTo(7);
    }
    {
      // second collection
      counter.add(4);
      counter.add(8);
      BasicMetricVisitor visitor2 = new BasicMetricVisitor("v", naming);
      counter.visit(visitor2);

      List<MetaCountMetric> result2 = visitor2.countMetrics();
      assertThat(result2).hasSize(1);
      assertThat(result2.get(0).name()).isEqualTo("prefix[org-hello]");
      assertThat(result2.get(0).count()).isEqualTo(12);
    }
  }

  @Test
  void cumulativeAndDeltaAreIndependent() {
    DCountMetric counter = new DCountMetric("org.hello");
    counter.add(7);

    assertThat(counter.get(false)).isEqualTo(7);
    assertThat(counter.get(false)).isEqualTo(7);
    assertThat(counter.get(true)).isEqualTo(7);

    counter.add(5);
    assertThat(counter.get(false)).isEqualTo(5);
    assertThat(counter.get(true)).isEqualTo(5);
    assertThat(counter.get(true)).isEqualTo(0);
  }

  @Test
  void valueAdderSupportsExplicitCollectionOperations() {
    var values = new ValueAdder();
    values.add(7);

    assertThat(values.cumulative()).isEqualTo(7);
    assertThat(values.delta()).isEqualTo(7);

    values.add(5);
    assertThat(values.cumulative()).isEqualTo(12);
    assertThat(values.delta()).isEqualTo(5);
    assertThat(values.getAndReset()).isEqualTo(12);
    assertThat(values.cumulative()).isEqualTo(0);
    assertThat(values.delta()).isEqualTo(0);
  }

  @Test
  void visitorCanCollectDeltaWithoutResettingCumulativeValue() {
    var counter = new DCountMetric("org.hello");
    counter.add(7);

    var cumulative = new BasicMetricVisitor("db", naming, MetricVisitor.Mode.CUMULATIVE, true, true, true);
    counter.visit(cumulative);
    assertThat(cumulative.countMetrics()).hasSize(1);
    assertThat(cumulative.countMetrics().get(0).count()).isEqualTo(7);

    var delta = new BasicMetricVisitor("db", naming, MetricVisitor.Mode.DELTA, true, true, true);
    counter.visit(delta);
    assertThat(delta.countMetrics()).hasSize(1);
    assertThat(delta.countMetrics().get(0).count()).isEqualTo(7);

    counter.add(5);
    delta = new BasicMetricVisitor("db", naming, MetricVisitor.Mode.DELTA, true, true, true);
    counter.visit(delta);
    assertThat(delta.countMetrics()).hasSize(1);
    assertThat(delta.countMetrics().get(0).count()).isEqualTo(5);

    cumulative = new BasicMetricVisitor("db", naming, MetricVisitor.Mode.CUMULATIVE, true, true, true);
    counter.visit(cumulative);
    assertThat(cumulative.countMetrics().get(0).count()).isEqualTo(12);
  }
}
