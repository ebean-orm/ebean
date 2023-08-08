package io.ebeaninternal.server.profile;

import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaTimedMetric;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class DTimedMetricTest {

  @Test
  public void addSinceNanos() throws InterruptedException {

    DTimedMetric metric = new DTimedMetric("addSinceNanos");

    long start = System.nanoTime();
    Thread.sleep(11);

    metric.addSinceNanos(start);

    DTimeMetricStats stats = metric.collect(true);
    assertThat(stats.count()).isEqualTo(1);
    assertThat(stats.total()).isGreaterThan(10);
    assertThat(stats.max()).isEqualTo(stats.total());

    metric.addSinceNanos(start);

    stats = metric.collect(true);
    assertThat(stats.count()).isEqualTo(1);
    assertThat(stats.total()).isGreaterThan(10);
    assertThat(stats.max()).isEqualTo(stats.total());
  }

  @Test
  public void addBatchSince() throws InterruptedException {

    DTimedMetric metric = new DTimedMetric("addSinceNanos");

    long start = System.nanoTime();
    Thread.sleep(11);

    metric.addBatchSince(start, 5);

    DTimeMetricStats stats = metric.collect(true);
    assertThat(stats.count()).isEqualTo(5);
    assertThat(stats.total()).isGreaterThan(10000);
    assertThat(stats.max()).isEqualTo(stats.total() / 5);
    assertThat(stats.max()).isGreaterThan(10000 / 5);

    metric.addBatchSince(start, 2);

    stats = metric.collect(true);
    assertThat(stats.count()).isEqualTo(2);
    assertThat(stats.total()).isGreaterThan(10000);
    assertThat(stats.max()).isEqualTo(stats.total() / 2);
  }

  Function<String, String> naming = (String name) -> "prefix[" + name.replace('.', '-') + "]";

  @Test
  void visit() {
    DTimedMetric metric = new DTimedMetric("org.timed");
    metric.add(560);
    metric.add(500);
    {
      BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming);
      metric.visit(visitor);
      List<MetaTimedMetric> result = visitor.timedMetrics();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).name()).isEqualTo("prefix[org-timed]");
      assertThat(result.get(0).count()).isEqualTo(2);
      assertThat(result.get(0).total()).isEqualTo(1060);
    }

    metric.add(160);
    metric.add(100);
    metric.add(150);
    {
      BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming);
      metric.visit(visitor);
      List<MetaTimedMetric> result = visitor.timedMetrics();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).name()).isEqualTo("prefix[org-timed]");
      assertThat(result.get(0).count()).isEqualTo(3);
      assertThat(result.get(0).total()).isEqualTo(410);
    }
  }
}
