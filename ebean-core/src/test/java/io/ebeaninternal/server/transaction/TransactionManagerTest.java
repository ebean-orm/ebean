package io.ebeaninternal.server.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebeaninternal.api.ScopedTransaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiTransactionManager;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Customer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionManagerTest extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TransactionManagerTest.class);

  @Test
  public void beginExternalTransaction() throws SQLException {

    SpiEbeanServer server = spiEbeanServer();

    SpiTransactionManager transactionManager = server.getTransactionManager();

    DataSource dataSource = transactionManager.getDataSource();
    Connection connection = dataSource.getConnection();

    SpiTransaction externalTxn = new ExternalJdbcTransaction("external0", true, connection, null);

    // push an externally managed transaction onto scope
    ScopedTransaction scopedTransaction = transactionManager.externalBeginTransaction(externalTxn, TxScope.required());

    Transaction current = Transaction.current();
    assertThat(current).as("external transaction is in scope").isSameAs(scopedTransaction);

    Customer.find.byName("In external");

    log.info("inner begin");
    Transaction inner = Ebean.beginTransaction();
    try {

      current = Transaction.current();
      assertThat(current).as("still using external transaction, nested transaction pushed").isSameAs(scopedTransaction);

      Customer.find.byName("In inner");
      log.info("inner commit");
      inner.commit();
    } finally {
      log.info("inner end");
      inner.end();
    }

    current = Transaction.current();
    assertThat(current).as("external transaction still in scope").isSameAs(scopedTransaction);

    Customer.find.byName("external still active");


    log.info("external transaction ends with commit and remove");
    connection.commit();
    // remove externally managed transaction out of scope
    transactionManager.externalRemoveTransaction();


    current = Transaction.current();
    assertThat(current).as("external transaction out of scope").isNull();

    Customer.find.byNameStatus("New implicit transaction", Customer.Status.ACTIVE);
  }
}
