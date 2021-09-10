package io.ebeaninternal.server.deploy;

import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaTimedMetric;
import io.ebeaninternal.server.core.PersistRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanIudMetricsTest {


  @Test
  public void addBatch() {

    BeanIudMetrics iudMetrics = new BeanIudMetrics("one");

    final long startNanos = System.nanoTime() - 10000;
    iudMetrics.addBatch(PersistRequest.Type.INSERT, startNanos, 4);

    BasicMetricVisitor basic = new BasicMetricVisitor();
    iudMetrics.visit(basic);

    List<MetaTimedMetric> timed = basic.timedMetrics();
    assertThat(timed).hasSize(1);

    assertThat(timed.get(0).count()).isEqualTo(4);
    assertThat(timed.get(0).name()).isEqualTo("iud.one.insertBatch");

    iudMetrics.addBatch(PersistRequest.Type.UPDATE, startNanos, 1);
    iudMetrics.addBatch(PersistRequest.Type.DELETE_SOFT, startNanos, 2);
    iudMetrics.addBatch(PersistRequest.Type.DELETE, startNanos, 4);
    iudMetrics.addBatch(PersistRequest.Type.DELETE_PERMANENT, startNanos, 8);
    iudMetrics.addBatch(PersistRequest.Type.INSERT, startNanos, 16);

    basic = new BasicMetricVisitor();
    iudMetrics.visit(basic);
    timed = basic.timedMetrics();
    assertThat(timed).hasSize(3);

    assertThat(timed.get(0).count()).isEqualTo(16);
    assertThat(timed.get(0).name()).isEqualTo("iud.one.insertBatch");
    assertThat(timed.get(1).count()).isEqualTo(3);
    assertThat(timed.get(1).name()).isEqualTo("iud.one.updateBatch");
    assertThat(timed.get(2).count()).isEqualTo(12);
    assertThat(timed.get(2).name()).isEqualTo("iud.one.deleteBatch");
  }

  @Test
  public void addNoBatch() {

    BeanIudMetrics iudMetrics = new BeanIudMetrics("one");

    final long startNanos = System.nanoTime() - 10000;
    iudMetrics.addNoBatch(PersistRequest.Type.INSERT, startNanos);
    iudMetrics.addNoBatch(PersistRequest.Type.UPDATE, startNanos);
    iudMetrics.addNoBatch(PersistRequest.Type.DELETE_SOFT, startNanos);
    iudMetrics.addNoBatch(PersistRequest.Type.DELETE, startNanos);
    iudMetrics.addNoBatch(PersistRequest.Type.DELETE_PERMANENT, startNanos);

    BasicMetricVisitor basic = new BasicMetricVisitor();
    iudMetrics.visit(basic);

    List<MetaTimedMetric> timed = basic.timedMetrics();
    assertThat(timed).hasSize(3);

    assertThat(timed.get(0).count()).isEqualTo(1);
    assertThat(timed.get(0).name()).isEqualTo("iud.one.insert");
    assertThat(timed.get(1).count()).isEqualTo(2);
    assertThat(timed.get(1).name()).isEqualTo("iud.one.update");
    assertThat(timed.get(2).count()).isEqualTo(2);
    assertThat(timed.get(2).name()).isEqualTo("iud.one.delete");
  }

}
