package io.ebeaninternal.server.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.DefaultServer;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DefaultTransactionThreadLocalTest extends BaseTestCase {

  private SpiTransaction currentTransaction() {
    DefaultServer srv = (DefaultServer) server();
    return ((TransactionManager)(srv.getTransactionManager())).scope().getInScope();
  }

  @ForPlatform({Platform.H2})
  @Test
  public void get() {
    Ebean.execute(() -> {
      SpiTransaction txn = currentTransaction();
      assertNotNull(txn);
    });

    // thread local should be set to null
    assertNull(currentTransaction());
  }

  @ForPlatform({Platform.H2})
  @Test
  public void tryWithResources_expect_scopeCleanup() {

    try (Transaction transaction = Ebean.beginTransaction()) {
      assertNotNull(transaction);
      SpiTransaction txn = currentTransaction();
      assertSame(txn, transaction);

      try (Transaction nested = Ebean.beginTransaction()) {
        assertNotNull(nested);
        Transaction txnNested = currentTransaction();
        assertSame(txnNested, nested);
      }

      assertNotNull(currentTransaction());
    }

    assertNull(currentTransaction());
  }

  @ForPlatform({Platform.H2})
  @Test
  public void afterCommit_expect_scopeCleanup() {

    try (Transaction transaction = Ebean.beginTransaction()) {
      transaction.commit();
      assertNull(currentTransaction());
    }
    assertNull(currentTransaction());
  }

  @ForPlatform({Platform.H2})
  @Test
  public void afterRollback_expect_scopeCleanup() {

    try (Transaction transaction = Ebean.beginTransaction()) {

      transaction.rollback();
      assertNull(currentTransaction());
    }
    assertNull(currentTransaction());
  }

  @ForPlatform({Platform.H2})
  @Test
  public void end_withoutActiveTransaction_isFine() {

    assertNull(Ebean.currentTransaction());
    Ebean.endTransaction();
  }

}
