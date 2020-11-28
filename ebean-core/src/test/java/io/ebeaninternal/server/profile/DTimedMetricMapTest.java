package io.ebeaninternal.server.profile;

import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaTimedMetric;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DTimedMetricMapTest {

  @Test
  public void addSinceNanos() throws InterruptedException {

    DTimedMetricMap metricMap = new DTimedMetricMap("addSinceNanos");

    long nanos = System.nanoTime();
    Thread.sleep(10);

    metricMap.addSinceNanos("some", nanos);

    BasicMetricVisitor visitor = new BasicMetricVisitor();
    metricMap.visit(visitor);

    MetaTimedMetric timedMetric = visitor.getTimedMetrics().get(0);
    assertThat(timedMetric.getCount()).isEqualTo(1);
    assertThat(timedMetric.getTotal()).isGreaterThan(10);

    metricMap.addSinceNanos("some", nanos);

    visitor = new BasicMetricVisitor();
    metricMap.visit(visitor);

    timedMetric = visitor.getTimedMetrics().get(0);
    assertThat(timedMetric.getCount()).isEqualTo(1);
    assertThat(timedMetric.getTotal()).isGreaterThan(10);
  }
}
