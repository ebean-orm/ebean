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
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.fail;

public class TestExecuteComplete extends BaseTestCase {


  @ForPlatform(Platform.H2)
  @Test
  public void execute_when_errorOnCommit_threadLocalIsCleared() {

    try {
      DB.execute(TxScope.required().setBatch(PersistBatch.ALL), () -> {

        Customer customer = DB.getReference(Customer.class, 42424242L);
        Order order = new Order();
        order.setCustomer(customer);

        DB.save(order);
      });
      fail();
    } catch (DataIntegrityException e) {
      // assert the thread local has been cleaned up
      assertThat(getInScopeTransaction()).isNull();
    }
  }

  @ForPlatform(Platform.H2)
  @Test
  public void nestedExecute_when_errorOnCommit_threadLocalIsCleared() {

    try {
      DB.execute(TxScope.required().setBatch(PersistBatch.ALL), () ->
      DB.execute(() -> {

          Customer customer = DB.getReference(Customer.class, 42424242L);
          Order order = new Order();
          order.setCustomer(customer);

          DB.save(order);
        }));
      fail();
    } catch (DataIntegrityException e) {
      // assert the thread local has been cleaned up
      assertThat(getInScopeTransaction()).isNull();
    }
  }

  @ForPlatform(Platform.H2)
  @Test
  public void transactional_errorOnCommit_expect_threadScopeCleanup() {

    try {
      errorOnCommit();
      fail();
    } catch (DataIntegrityException e) {
      assertThat(getInScopeTransaction()).isNull();
    }
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

    assertThat(getInScopeTransaction()).isNull();
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

    assertThat(getInScopeTransaction()).isNull();
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

    assertThat(getInScopeTransaction()).isNull();
  }

  @ForPlatform(Platform.H2)
  @Test
  public void implicit_query_expect_threadScopeCleanup() {

    DB.find(Customer.class).findList();

    assertThat(getInScopeTransaction()).isNull();
  }

  @ForPlatform(Platform.H2)
  @Test
  public void implicit_save_expect_threadScopeCleanup() {

    Customer cust = new Customer();
    cust.setName("Roland");
    DB.save(cust);

    assertThat(getInScopeTransaction()).isNull();
  }

  @ForPlatform(Platform.H2)
  @Test
  public void no_transaction_expect_threadScopeCleanup() {

    try (Transaction txn = DB.beginTransaction(TxScope.notSupported())) {
      SpiTransaction txn2 = getInScopeTransaction();
      // The NoTransaction placeholder can normally only occur inside
      // a scopedTrans. (Class is package private, so check
      assertThat(txn2.toString()).contains("NoTransaction");
      assertThat(txn2.toString()).contains("NoTransaction");
    }

    assertThat(getInScopeTransaction()).isNull();
  }

}
