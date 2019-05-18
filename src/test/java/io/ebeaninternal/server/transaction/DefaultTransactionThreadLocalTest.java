package io.ebeaninternal.server.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.UTDetail;
import org.tests.model.basic.UTMaster;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DefaultTransactionThreadLocalTest extends BaseTestCase {

  @ForPlatform({Platform.H2})
  @Test
  public void get() {
    Ebean.execute(() -> {
      SpiTransaction txn = getInScopeTransaction();
      assertNotNull(txn);
    });

    // thread local should be set to null
    assertNull(getInScopeTransaction());
  }

  @ForPlatform({Platform.H2})
  @Test
  public void tryWithResources_expect_scopeCleanup() {

    try (Transaction transaction = Ebean.beginTransaction()) {
      assertNotNull(transaction);
      SpiTransaction txn = getInScopeTransaction();
      assertSame(txn, transaction);

      try (Transaction nested = Ebean.beginTransaction()) {
        assertNotNull(nested);
        SpiTransaction txnNested = getInScopeTransaction();
        assertSame(txnNested, nested);
      }

      assertNotNull(getInScopeTransaction());
    }

    assertNull(getInScopeTransaction());
  }

  @ForPlatform({Platform.H2})
  @Test
  public void afterCommit_expect_scopeCleanup() {

    try (Transaction transaction = Ebean.beginTransaction()) {
      transaction.commit();
      assertNull(getInScopeTransaction());
    }
    assertNull(getInScopeTransaction());
  }

  @ForPlatform({Platform.H2})
  @Test
  public void afterRollback_expect_scopeCleanup() {

    try (Transaction transaction = Ebean.beginTransaction()) {

      transaction.rollback();
      assertNull(getInScopeTransaction());
    }
    assertNull(getInScopeTransaction());
  }

  @ForPlatform({Platform.H2})
  @Test
  public void end_withoutActiveTransaction_isFine() {

    assertNull(Ebean.currentTransaction());
    Ebean.endTransaction();
  }

  @ForPlatform({Platform.H2})
  @Test
  public void multi_database_threadlocals() {

    Database db = DB.getDefault();
    Database otherDb = createOtherDatabase();
    try {
      try (Transaction dbTxn = db.beginTransaction()) {
        try (Transaction otherDbTxn = otherDb.beginTransaction()) {
          assertThat(dbTxn).isNotSameAs(otherDbTxn);
        }
      }
    } finally {
      otherDb.shutdown(true, false);
    }
  }


  private Database createOtherDatabase() {

    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2ebasicver");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);

    config.setRegister(false);
    config.setDefaultServer(false);
    config.getClasses().add(EBasicVer.class);
    config.getClasses().add(UTMaster.class);
    config.getClasses().add(UTDetail.class);

    return DatabaseFactory.create(config);
  }
}
