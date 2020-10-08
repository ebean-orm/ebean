package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.Transactional;
import io.ebean.meta.MetaTimedMetric;
import org.junit.Test;
import org.tests.model.basic.Customer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTransactionalReadOnly extends BaseTestCase {

  @Test
  public void test_readonly_datasource() {

    resetAllMetrics();
    executeTransactionalUsingReadOnlyDataSource();

    final List<MetaTimedMetric> timedMetrics = collectMetrics().getTimedMetrics();
    final Optional<MetaTimedMetric> txnReadOnly = metric(timedMetrics, "txn.readonly");
    assertThat(txnReadOnly.get().getCount()).isEqualTo(1);
    assertThat(metric(timedMetrics, "txn")).isEmpty();
  }

  @Test
  public void test_main_datasource() {

    resetAllMetrics();
    executeTransactionalUsingMainDataSource();

    final List<MetaTimedMetric> timedMetrics = collectMetrics().getTimedMetrics();
    final Optional<MetaTimedMetric> txnMain = metric(timedMetrics, "txn.main");
    assertThat(txnMain.get().getCount()).isEqualTo(1);
    assertThat(metric(timedMetrics, "txn.readonly")).isEmpty();
  }

  private Optional<MetaTimedMetric> metric(List<MetaTimedMetric> timedMetrics, String name) {
    return timedMetrics.stream()
        .filter(metaTimedMetric -> metaTimedMetric.getName().equals(name))
        .findFirst();
  }

  @Transactional(readOnly = true)
  private void executeTransactionalUsingReadOnlyDataSource() {
    DB.find(Customer.class).findCount();
  }

  @Transactional
  private void executeTransactionalUsingMainDataSource() {
    DB.find(Customer.class).findCount();
  }
}
