package org.tests.transaction;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.m2m.MnyB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestCommitAndContinue extends BaseTestCase {

  private static final Logger logger = LoggerFactory.getLogger("org.avaje.ebean.TXN");

  @Test
  @Transactional
  @IgnorePlatform({Platform.SQLSERVER, Platform.HSQLDB, Platform.COCKROACH}) // they will dead lock
  void transactional_partialSuccess() {

    MnyB a = new MnyB("a100");
    MnyB b = new MnyB("b200");

    a.save();

    // commit at this point
    DB.currentTransaction().commitAndContinue();

    try {
      b.save();

      // some error occurs
      throw new IllegalStateException();

    } catch (IllegalStateException e) {
      // mark the transaction as rollback
      DB.currentTransaction().setRollbackOnly();

      // use a different transaction to assert
      Database server = DB.getDefault();
      try (Transaction anotherTxn = server.createTransaction()) {
        // success prior to commitAndContinue
        assertNotNull(server.find(MnyB.class, a.getId(), anotherTxn));
        // insert failed after commitAndContinue - sqlserver dead locks here
        assertNull(server.find(MnyB.class, b.getId(), anotherTxn));
      }
    }
  }

  /**
   * The @Transactional is nicer to me.
   */
  @Test
  @IgnorePlatform({Platform.SQLSERVER, Platform.HSQLDB, Platform.COCKROACH, Platform.YUGABYTE}) // they will dead lock
  void tryFinally_partialSuccess() {

    MnyB a = new MnyB("a100");
    MnyB b = new MnyB("b200");

    Database server = DB.getDefault();
    Transaction txn = server.beginTransaction();
    try {
      a.save();
      // commit at this point
      txn.commitAndContinue();
      try {
        b.save();

        // some error occurs
        throw new IllegalStateException();

      } catch (IllegalStateException e) {
        // mark the transaction as rollback
        txn.setRollbackOnly();

        // use a different transaction to assert
        try (Transaction anotherTxn = server.createTransaction()) {
          // success prior to commitAndContinue
          assertNotNull(server.find(MnyB.class, a.getId(), anotherTxn));
          // insert failed after commitAndContinue
          assertNull(server.find(MnyB.class, b.getId(), anotherTxn));
        }
        //anotherTxn.end();
      }

      // does not commit due to the txn.setRollbackOnly();
      txn.commit();

    } finally {
      //server.endTransaction();
      txn.end();
    }
  }

  @Test
  @Transactional
  @IgnorePlatform({Platform.SQLSERVER, Platform.HSQLDB, Platform.COCKROACH}) // they will dead lock
  void transactional_partialSuccess_secondTransactionInsert() {

    MnyB a = new MnyB("a100");
    MnyB b = new MnyB("b200");
    MnyB c = new MnyB("c300");

    a.save();

    // commit at this point
    DB.currentTransaction().commitAndContinue();

    try {
      b.save();

      // some error occurs
      throw new IllegalStateException();

    } catch (IllegalStateException e) {
      // mark the transaction as rollback
      DB.currentTransaction().setRollbackOnly();

      // use a different transaction to do something useful
      Database server = DB.getDefault();
      Transaction txn2 = server.createTransaction();
      try {
        server.save(c, txn2);
        txn2.commit();
      } finally {
        txn2.end();
      }
    }

    // asserts

    Database server = DB.getDefault();
    try (Transaction txnForAssert = server.createTransaction()) {
      // success prior to commitAndContinue
      assertNotNull(server.find(MnyB.class, a.getId(), txnForAssert));

      // insert failed after commitAndContinue
      assertNull(server.find(MnyB.class, b.getId(), txnForAssert));

      // successful insert using txn2
      assertNotNull(server.find(MnyB.class, c.getId(), txnForAssert));
    }
  }

  @Test
  void basic() {
    MnyB a = new MnyB("a");
    MnyB b = new MnyB("b");
    MnyB c = new MnyB("c");

    Transaction txn = DB.beginTransaction();
    try {
      a.save();
      txn.commitAndContinue();

      txn.setBatchMode(true);
      b.save();
      logger.info("... pre commitAndContinue");
      txn.commitAndContinue();

      c.save();
      txn.commit();

    } finally {
      txn.end();
    }
  }

  @Test
  void testAutoCommit() {
    DB.find(MnyB.class).delete();

    MnyB a = new MnyB("a");
    MnyB b = new MnyB("b");
    MnyB c = new MnyB("c");

    try (Transaction txn = DB.beginTransaction()) {
      a.save();
      b.save();
      c.save();
    }
    assertThat(DB.find(MnyB.class).findCount()).isEqualTo(0);

    // try again with new beans
    a = new MnyB("a");
    b = new MnyB("b");
    c = new MnyB("c");
    try (Transaction txn = DB.beginTransaction()) {
      txn.setMaxTransactionSize(2);
      a.save();
      b.save();
      // txn.commitAndContinue() is executed here
      c.save();
    }
    assertThat(DB.find(MnyB.class).findCount()).isEqualTo(2);
  }

  /**
   * Demonstrate the auto commit and continue feature.
   * <p>
   * On DB2 (and maybe also on other DBMS) you'll get the error "Sqlcode: -964, Sqlstate: 57011"
   * (transaction log for the database is full) when doing huge imports in one transaction.
   * <p>
   * To avoid this, you can specify a "maxTransaction" size, so that ebean performs automatically
   * a "commit and contine" after a certain amount of updates.
   * <p>
   * For simulation, reduce the LOGSECOND to 1 with
   * <code>docker exec -i ut_db2 su - admin -c "db2 update db cfg for unit using LOGSECOND 1"</code>
   * If there is no limit on transaction size, the test will fail after about ~112000 inserts.
   */
  @Test
  @Disabled
  void testLogSpace() {
    //DB.find(MnyB.class).delete();

    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchMode(true);
      txn.setMaxTransactionSize(100_000); // without this line, the test will fail
      for (int i = 0; i < 1_000_000; i++) {
        MnyB b = new MnyB("x" + i);
        b.save();
        if (i % 1000 == 0) {
          System.out.println(i);
        }
      }
    }
  }

  @Test
  @Transactional
  void runTransactional() {

    new MnyB("a100").save();
    new MnyB("a101").save();

    DB.currentTransaction().commitAndContinue();

    new MnyB("a200").save();
    new MnyB("a201").save();
  }

}
