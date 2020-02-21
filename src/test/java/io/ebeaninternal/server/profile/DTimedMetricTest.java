package io.ebeaninternal.server.profile;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DTimedMetricTest {

  @Test
  public void addSinceNanos() throws InterruptedException {

    DTimedMetric metric = new DTimedMetric("addSinceNanos");

    long start = System.nanoTime();
    Thread.sleep(10);

    metric.addSinceNanos(start);

    DTimeMetricStats stats = metric.collect(true);
    assertThat(stats.getCount()).isEqualTo(1);
    assertThat(stats.getTotal()).isGreaterThan(10);
    assertThat(stats.getMax()).isEqualTo(stats.getTotal());

    metric.addSinceNanos(start);

    stats = metric.collect(true);
    assertThat(stats.getCount()).isEqualTo(1);
    assertThat(stats.getTotal()).isGreaterThan(10);
    assertThat(stats.getMax()).isEqualTo(stats.getTotal());
  }

  @Test
  public void addBatchSince() throws InterruptedException {

    DTimedMetric metric = new DTimedMetric("addSinceNanos");

    long start = System.nanoTime();
    Thread.sleep(10);

    metric.addBatchSince(start, 5);

    DTimeMetricStats stats = metric.collect(true);
    assertThat(stats.getCount()).isEqualTo(5);
    assertThat(stats.getTotal()).isGreaterThan(10000);
    assertThat(stats.getMax()).isEqualTo(stats.getTotal() / 5);
    assertThat(stats.getMax()).isGreaterThan(10000 / 5);

    metric.addBatchSince(start, 2);

    stats = metric.collect(true);
    assertThat(stats.getCount()).isEqualTo(2);
    assertThat(stats.getTotal()).isGreaterThan(10000);
    assertThat(stats.getMax()).isEqualTo(stats.getTotal() / 2);
  }
}
