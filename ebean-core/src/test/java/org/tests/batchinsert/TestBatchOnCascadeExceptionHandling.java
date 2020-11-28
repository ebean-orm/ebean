package org.tests.batchinsert;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.persist.BatchControl;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.tests.model.basic.EBasicWithUniqueCon;
import org.tests.model.basic.EOptOneB;
import org.tests.model.basic.EOptOneC;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.sql.Savepoint;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBatchOnCascadeExceptionHandling extends BaseTestCase {

  @IgnorePlatform({Platform.HANA, Platform.NUODB})
  @Test
  public void testBatchScenarioWithSavepoint() throws SQLException {
    server().save(createEntityWithName("conflict", "before"));

    Transaction txn = server().beginTransaction();
    try {
      EBasicWithUniqueCon v2 = createEntityWithName("conflict", "after");
      txn.flushBatch();
      Savepoint sp = txn.getConnection().setSavepoint();
      try {
        server().save(v2); // unique key violation
      } catch (PersistenceException e) {
        txn.getConnection().rollback(sp);

        EBasicWithUniqueCon conflicting = server().find(EBasicWithUniqueCon.class).where().eq("name", "conflict").findOne();
        assertThat(conflicting).isNotNull();
        server().delete(conflicting);
        server().save(v2); // try again
      }
      txn.commit();
    } finally {
      txn.end();
    }

    EBasicWithUniqueCon winner = server().find(EBasicWithUniqueCon.class).where().eq("name", "conflict").findOne();
    assertThat(winner).isNotNull();
    assertThat(winner.getDescription()).isEqualTo("after");
  }

  @IgnorePlatform(Platform.NUODB)
  @Test
  public void testBatchedInsertFailure() {
    server().save(createEntityWithName("foo"));
    testBatchOnCascadeIsExceptionSafe(server(), () -> {
      server().save(createEntityWithName("foo")); // duplicate name on insert
    });
  }

  @IgnorePlatform(Platform.NUODB)
  @Test
  public void testBatchedUpdateFailure() {
    server().save(createEntityWithName("bla"));
    final EBasicWithUniqueCon bar = createEntityWithName("bar");
    server().save(bar);
    testBatchOnCascadeIsExceptionSafe(server(), () -> {
      bar.setName("bla");
      server().save(bar); // duplicate name on update
    });
  }

  @IgnorePlatform(Platform.NUODB)
  @Test
  public void testBatchedDeleteFailure() {
    final EOptOneC c = new EOptOneC();
    server().save(c);
    final EOptOneB b = new EOptOneB();
    b.setC(c);
    server().save(b);
    testBatchOnCascadeIsExceptionSafe(server(), () -> {
      server().delete(c); // foreign key violation
    });
  }

  protected void testBatchOnCascadeIsExceptionSafe(EbeanServer server, Runnable failingOperation) {
    Transaction txn = server.beginTransaction();
    try {
      assertThat(txn.isBatchMode()).isFalse();
      assertThat(txn.isBatchOnCascade()).isSameAs(PersistBatch.ALL.equals(spiEbeanServer().getDatabasePlatform().getPersistBatchOnCascade()));

      failingOperation.run();
      Assertions.fail("PersistenceException expected");
    } catch (PersistenceException e) {
      assertThat(txn.isBatchMode()).as("batch mode").isFalse(); // should not have changed
      BatchControl bc = ((SpiTransaction) txn).getBatchControl();
      assertThat(bc == null || bc.isEmpty()).as("batch emtpy").isTrue();
    } finally {
      txn.end();
    }
  }

  protected EBasicWithUniqueCon createEntityWithName(String name) {
    return createEntityWithName(name, null);
  }

  protected EBasicWithUniqueCon createEntityWithName(String name, String description) {
    EBasicWithUniqueCon it = new EBasicWithUniqueCon();
    it.setName(name);
    it.setDescription(description);
    return it;
  }
}
