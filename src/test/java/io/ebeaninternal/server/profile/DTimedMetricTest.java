package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DTimedMetricTest {

  @Test
  public void addSinceNanos() throws InterruptedException {

    DTimedMetric metric = new DTimedMetric(MetricType.L2, "addSinceNanos");

    long start = System.nanoTime();
    Thread.sleep(10);

    metric.addSinceNanos(start);

    DTimeMetricStats stats = metric.collect(true);
    assertThat(stats.getCount()).isEqualTo(1);
    assertThat(stats.getTotal()).isGreaterThan(10);
    assertThat(stats.getMax()).isEqualTo(stats.getTotal());

    metric.addSinceNanos(start, 42);

    stats = metric.collect(true);
    assertThat(stats.getCount()).isEqualTo(1);
    assertThat(stats.getTotal()).isGreaterThan(10);
    assertThat(stats.getMax()).isEqualTo(stats.getTotal());
    assertThat(stats.getBeanCount()).isEqualTo(42);
  }
}
