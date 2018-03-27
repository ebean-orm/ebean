package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DataIntegrityException;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.transaction.DefaultTransactionThreadLocal;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestExecuteComplete extends BaseTestCase {


  @ForPlatform(Platform.H2)
  @Test
  public void execute_when_errorOnCommit_threadLocalIsCleared() {

    try {
      Ebean.execute(TxScope.required().setBatch(PersistBatch.ALL), () -> {

        Customer customer = Ebean.getReference(Customer.class, 42424242L);
        Order order = new Order();
        order.setCustomer(customer);

        Ebean.save(customer);
      });
    } catch (DataIntegrityException e) {
      // assert the thread local has been cleaned up
      SpiTransaction txn = DefaultTransactionThreadLocal.get("h2");
      assertThat(txn).isNull();
    }
  }

  @ForPlatform(Platform.H2)
  @Test
  public void nestedExecute_when_errorOnCommit_threadLocalIsCleared() {

    try {
      Ebean.execute(TxScope.required().setBatch(PersistBatch.ALL), () ->
        Ebean.execute(() -> {

          Customer customer = Ebean.getReference(Customer.class, 42424242L);
          Order order = new Order();
          order.setCustomer(customer);

          Ebean.save(customer);
        }));
    } catch (DataIntegrityException e) {
      // assert the thread local has been cleaned up
      SpiTransaction txn = DefaultTransactionThreadLocal.get("h2");
      assertThat(txn).isNull();
    }
  }

  @ForPlatform(Platform.H2)
  @Test
  public void transactional_errorOnCommit_expect_threadScopeCleanup() {

    try {
      errorOnCommit();
    } catch (DataIntegrityException e) {
      SpiTransaction txn = DefaultTransactionThreadLocal.get("h2");
      assertThat(txn).isNull();
    }
  }

  @Transactional(batchSize = 10)
  private void errorOnCommit() {
    Customer customer = Ebean.getReference(Customer.class, 42424242L);
    Order order = new Order();
    order.setCustomer(customer);

    Ebean.save(customer);
  }

  @ForPlatform(Platform.H2)
  @Test
  public void normal_expect_threadScopeCleanup() {

    Transaction txn1 = server().beginTransaction();
    try {
      txn1.commit();
    } finally {
      txn1.end();
    }

    SpiTransaction txn2 = DefaultTransactionThreadLocal.get("h2");
    assertThat(txn2).isNull();
  }

  @ForPlatform(Platform.H2)
  @Test
  public void missingEnd_expect_threadScopeCleanup() {

    Transaction txn1 = server().beginTransaction();
    try {
      txn1.commit();
    } finally {
      // accidentally omit end()
      //txn1.end();
    }

    SpiTransaction txn2 = DefaultTransactionThreadLocal.get("h2");
    assertThat(txn2).isNull();
  }

  @ForPlatform(Platform.H2)
  @Test
  public void missingEnd_withRollbackOnly_expect_threadScopeCleanup() {

    Transaction txn1 = server().beginTransaction();
    try {
      txn1.rollback();
    } finally {
      // accidentally omit end()
      //txn1.end();
    }

    SpiTransaction txn2 = DefaultTransactionThreadLocal.get("h2");
    assertThat(txn2).isNull();
  }

}
