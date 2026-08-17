package org.tests.transaction;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxIsolation;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTransactionalReadOnly extends BaseTestCase {

  @Test
  public void test_readonly_datasource() {

    resetAllMetrics();
    executeTransactionalUsingReadOnlyDataSource();

    final List<MetaTimedMetric> timedMetrics = collectMetrics().timedMetrics();
    final Optional<MetaTimedMetric> txnReadOnly = metric(timedMetrics, "txn.readonly");
    assertThat(txnReadOnly.get().count()).isEqualTo(1);
    assertThat(metric(timedMetrics, "txn")).isEmpty();
  }

  @Test
  public void test_main_datasource() {

    resetAllMetrics();
    executeTransactionalUsingMainDataSource();

    final List<MetaTimedMetric> timedMetrics = collectMetrics().timedMetrics();
    final Optional<MetaTimedMetric> txnMain = metric(timedMetrics, "txn.main");
    assertThat(txnMain.get().count()).isEqualTo(1);
    assertThat(metric(timedMetrics, "txn.readonly")).isEmpty();
  }

  /**
   * #3407 read-only TxScope must honor isolation (previously dropped for createReadOnlyTransaction).
   */
  @Test
  public void test_readonly_honors_isolation() throws SQLException {
    TxScope scope = TxScope.required()
      .setReadOnly(true)
      .setIsolation(TxIsolation.SERIALIZABLE);

    DB.execute(scope, () -> {
      Transaction txn = DB.currentTransaction();
      assertThat(txn).isNotNull();
      try {
        assertThat(txn.connection().getTransactionIsolation())
          .isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
      DB.find(Customer.class).findCount();
    });
  }

  @Test
  public void test_readonly_annotation_honors_isolation() throws SQLException {
    executeTransactionalReadOnlyWithIsolation();
  }

  private Optional<MetaTimedMetric> metric(List<MetaTimedMetric> timedMetrics, String name) {
    return timedMetrics.stream()
        .filter(metaTimedMetric -> metaTimedMetric.name().equals(name))
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

  @Transactional(readOnly = true, isolation = TxIsolation.SERIALIZABLE)
  private void executeTransactionalReadOnlyWithIsolation() throws SQLException {
    Transaction txn = DB.currentTransaction();
    assertThat(txn).isNotNull();
    assertThat(txn.connection().getTransactionIsolation())
      .isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
    DB.find(Customer.class).findCount();
  }
}
