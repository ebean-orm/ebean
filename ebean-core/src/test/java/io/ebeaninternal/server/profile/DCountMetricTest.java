package io.ebeaninternal.server.profile;

import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaCountMetric;
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
}
