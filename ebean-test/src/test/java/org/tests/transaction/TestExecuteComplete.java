package org.tests.transaction;

import io.ebean.DB;
import io.ebean.DataIntegrityException;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class TestExecuteComplete extends BaseTestCase {

  @BeforeAll
  static void before() {
    ResetBasicData.reset();
  }

  @ForPlatform(Platform.H2)
  @Test
  public void execute_when_errorOnCommit_threadLocalIsCleared() {
    try {
      DB.execute(TxScope.required().setBatch(PersistBatch.ALL), () -> {

        Customer customer = DB.reference(Customer.class, 42424242L);
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

          Customer customer = DB.reference(Customer.class, 42424242L);
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
  void transactional_errorOnCommit_expect_threadScopeCleanup() {
    try {
      errorOnCommit();
      fail();
    } catch (DataIntegrityException e) {
      assertThat(getInScopeTransaction()).isNull();
    }
  }

  @Transactional(batchSize = 10)
  private void errorOnCommit() {
    Customer customer = DB.reference(Customer.class, 42424242L);
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

    try {
      assertThat(getInScopeTransaction()).isNull();
    } finally {
      DB.delete(cust);
    }
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

  @ForPlatform(Platform.H2)
  @Test
  public void test_nested_userobjects() {
    try (Transaction txn1 = DB.beginTransaction()) {
      assertThat(getInScopeTransaction()).isNotNull();
      getInScopeTransaction().putUserObject("foo", "bar");

      try (Transaction txn2 = DB.beginTransaction()) {
        assertThat(getInScopeTransaction().getUserObject("foo")).isEqualTo("bar");
        getInScopeTransaction().putUserObject("foo", "xxx");
        getInScopeTransaction().putUserObject("test", "xxx");
        assertThat(getInScopeTransaction().getUserObject("foo")).isEqualTo("xxx");
        txn2.commit();
      }
      // CHECKME: What would we expect here? I would expect "bar" but get "xxx"
      // NOTE: with TxScope.requiresNew() - I'll get "bar"
      assertThat(getInScopeTransaction().getUserObject("test")).isNull();
      assertThat(getInScopeTransaction().getUserObject("foo")).isEqualTo("bar");
    }
  }

}
