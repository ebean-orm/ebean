package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.DataIntegrityException;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.transaction.DefaultTransactionThreadLocal;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThatThrownBy;

public class TestExecuteComplete extends BaseTestCase {

  @Rule public TestName name = new TestName();

  @After
  public void checkForLeak() {
    if (DefaultTransactionThreadLocal.clear()) {
      System.out.println(name.getMethodName() + " had stuck transactionmap.");
    } else {
      System.out.println(name.getMethodName() + " cleared everything.");
    }
  }

  @ForPlatform(Platform.H2)
  @Test
  public void execute_when_errorOnCommit_threadLocalIsCleared() {

    assertThatThrownBy(
        () -> DB.execute(TxScope.required().setBatch(PersistBatch.ALL),
            () -> {
              Customer customer = DB.getReference(Customer.class, 42424242L);
              Order order = new Order();
              order.setCustomer(customer);
              DB.save(order);
            }
        )
    ).isInstanceOf(DataIntegrityException.class);
      // assert the thread local has been cleaned up
      SpiTransaction txn = DefaultTransactionThreadLocal.get("h2");
      assertThat(txn).isNull();

  }

  @ForPlatform(Platform.H2)
  @Test
  public void nestedExecute_when_errorOnCommit_threadLocalIsCleared() {

    assertThatThrownBy(
        () -> DB.execute(TxScope.required().setBatch(PersistBatch.ALL),
            () -> DB.execute(
                () -> {
                  Customer customer = DB.getReference(Customer.class, 42424242L);
                  Order order = new Order();
                  order.setCustomer(customer);
                  DB.save(order);
                }
            )
        )
    ).isInstanceOf(DataIntegrityException.class);
    // assert the thread local has been cleaned up
    SpiTransaction txn = DefaultTransactionThreadLocal.get("h2");
    assertThat(txn).isNull();

  }

  @ForPlatform(Platform.H2)
  @Test
  public void transactional_errorOnCommit_expect_threadScopeCleanup() {

    assertThatThrownBy(this::errorOnCommit).isInstanceOf(DataIntegrityException.class);

    SpiTransaction txn = DefaultTransactionThreadLocal.get("h2");
    assertThat(txn).isNull();
  }

  @Transactional(batchSize = 10)
  private void errorOnCommit() {
    Customer customer = DB.getReference(Customer.class, 42424242L);
    Order order = new Order();
    order.setCustomer(customer);

    DB.save(order);
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
