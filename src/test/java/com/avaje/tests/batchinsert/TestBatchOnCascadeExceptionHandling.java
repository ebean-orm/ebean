package com.avaje.tests.batchinsert;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.PersistBatch;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.persist.BatchControl;
import com.avaje.tests.model.basic.EBasicWithUniqueCon;
import com.avaje.tests.model.basic.EOptOneB;
import com.avaje.tests.model.basic.EOptOneC;
import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.sql.Savepoint;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestBatchOnCascadeExceptionHandling extends BaseTestCase {

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

        EBasicWithUniqueCon conflicting = server().find(EBasicWithUniqueCon.class).where().eq("name", "conflict").findUnique();
        assertThat(conflicting).isNotNull();
        server().delete(conflicting);
        server().save(v2); // try again
      }
      txn.commit();
    } finally {
      txn.end();
    }

    EBasicWithUniqueCon winner = server().find(EBasicWithUniqueCon.class).where().eq("name", "conflict").findUnique();
    assertThat(winner).isNotNull();
    assertThat(winner.getDescription()).isEqualTo("after");
  }

  @Test
  public void testBatchedInsertFailure() {
    server().save(createEntityWithName("foo"));
    testBatchOnCascadeIsExceptionSafe(server(), () -> {
      server().save(createEntityWithName("foo")); // duplicate name on insert
    });
  }

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
    assertThat(txn.getBatch()).isSameAs(PersistBatch.NONE);
    assertThat(txn.getBatchOnCascade()).isSameAs(PersistBatch.ALL);

    try {
      failingOperation.run();
      Assertions.fail("PersistenceException expected");
    } catch (PersistenceException e) {
      assertThat(txn.getBatch()).as("batch mode").isSameAs(PersistBatch.NONE); // should not have changed
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
