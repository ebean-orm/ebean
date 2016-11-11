package com.avaje.tests.batchinsert;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
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
    EbeanServer server = Ebean.getDefaultServer();
    Transaction txn = server.beginTransaction();
    try {
      server.save(createEntityWithName("before-savepoint"));
      txn.flushBatch();
      Savepoint sp = txn.getConnection().setSavepoint();
      try {
        server.save(createEntityWithName("confict"));
        server.save(createEntityWithName("confict")); // unique key violation
      } catch (PersistenceException e) {
        txn.getConnection().rollback(sp);
        server.save(createEntityWithName("after-savepoint"));
      }

      txn.commit();
    } finally {
      txn.end();
    }

    assertThat(server.find(EBasicWithUniqueCon.class).where().eq("name", "before-savepoint").findList()).isNotEmpty();
    assertThat(server.find(EBasicWithUniqueCon.class).where().eq("name", "after-savepoint").findList()).isNotEmpty();
    assertThat(server.find(EBasicWithUniqueCon.class).where().eq("name", "confict").findList()).isEmpty();
  }

  @Test
  public void testBatchedInsertFailure() {
    final EbeanServer server = Ebean.getDefaultServer();
    server.save(createEntityWithName("foo"));
    testBatchOnCascadeIsExceptionSafe(server, () -> {
      server.save(createEntityWithName("foo")); // duplicate name on insert
    });
  }

  @Test
  public void testBatchedUpdateFailure() {
    final EbeanServer server = Ebean.getDefaultServer();
    server.save(createEntityWithName("bla"));
    final EBasicWithUniqueCon bar = createEntityWithName("bar");
    server.save(bar);
    testBatchOnCascadeIsExceptionSafe(server, () -> {
      bar.setName("bla");
      server.save(bar); // duplicate name on update
    });
  }

  @Test
  public void testBatchedDeleteFailure() {
    final EbeanServer server = Ebean.getDefaultServer();
    final EOptOneC c = new EOptOneC();
    server.save(c);
    final EOptOneB b = new EOptOneB();
    b.setC(c);
    server.save(b);
    testBatchOnCascadeIsExceptionSafe(server, () -> {
      server.delete(c); // foreign key violation
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
    EBasicWithUniqueCon it = new EBasicWithUniqueCon();
    it.setName(name);
    return it;
  }
}
