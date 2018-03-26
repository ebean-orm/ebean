package io.ebeaninternal.server.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DefaultTransactionThreadLocalTest extends BaseTestCase {

  @ForPlatform({Platform.H2})
  @Test
  public void get() {
    Ebean.execute(() -> {
      SpiTransaction txn = DefaultTransactionThreadLocal.get("h2");
      assertNotNull(txn);
    });

    // thread local should be set to null
    assertNull(DefaultTransactionThreadLocal.get("h2"));
  }

  @ForPlatform({Platform.H2})
  @Test
  public void tryWithResources_expect_scopeCleanup() {

    try (Transaction transaction = Ebean.beginTransaction()) {
      assertNotNull(transaction);
      SpiTransaction txn = DefaultTransactionThreadLocal.get("h2");
      assertSame(txn, transaction);

      try (Transaction nested = Ebean.beginTransaction()) {
        assertNotNull(nested);
        SpiTransaction txnNested = DefaultTransactionThreadLocal.get("h2");
        assertSame(txnNested, nested);
      }

      assertNotNull(DefaultTransactionThreadLocal.get("h2"));
    }

    assertNull(DefaultTransactionThreadLocal.get("h2"));
  }

  @ForPlatform({Platform.H2})
  @Test
  public void afterCommit_expect_scopeCleanup() {

    try (Transaction transaction = Ebean.beginTransaction()) {
      transaction.commit();
      assertNull(DefaultTransactionThreadLocal.get("h2"));
    }
    assertNull(DefaultTransactionThreadLocal.get("h2"));
  }

  @ForPlatform({Platform.H2})
  @Test
  public void afterRollback_expect_scopeCleanup() {

    try (Transaction transaction = Ebean.beginTransaction()) {

      transaction.rollback();
      assertNull(DefaultTransactionThreadLocal.get("h2"));
    }
    assertNull(DefaultTransactionThreadLocal.get("h2"));
  }

  @ForPlatform({Platform.H2})
  @Test
  public void end_withoutActiveTransaction_isFine() {

    assertNull(Ebean.currentTransaction());
    Ebean.endTransaction();
  }

}
