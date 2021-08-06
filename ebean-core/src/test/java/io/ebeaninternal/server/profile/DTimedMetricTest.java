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
    Thread.sleep(10);

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
}
