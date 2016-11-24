package com.avaje.tests.transaction;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.tests.model.m2m.MnyB;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeFalse;

public class TestCommitAndContinue extends BaseTestCase {

  private static final Logger logger = LoggerFactory.getLogger("org.avaje.ebean.TXN");

  @Test
  @Transactional
  public void transactional_partialSuccess() {
    assumeFalse("Skipping test because MS SQL Server will dead lock here.", isMsSqlServer());
    MnyB a = new MnyB("a100");
    MnyB b = new MnyB("b200");

    a.save();

    // commit at this point
    Ebean.currentTransaction().commitAndContinue();

    try {
      b.save();

      // some error occurs
      throw new IllegalStateException();

    } catch (IllegalStateException e) {
      // mark the transaction as rollback
      Ebean.currentTransaction().setRollbackOnly();

      // use a different transaction to assert
      EbeanServer server = Ebean.getDefaultServer();
      Transaction anotherTxn = server.createTransaction();

      // success prior to commitAndContinue
      assertNotNull(server.find(MnyB.class, a.getId(), anotherTxn));

      // insert failed after commitAndContinue
      assertNull(server.find(MnyB.class, b.getId(), anotherTxn));
    }
  }

  /**
   * The @Transactional is nicer to me.
   */
  @Test
  public void tryFinally_partialSuccess() {
    assumeFalse("Skipping test because MS SQL Server will dead lock here.", isMsSqlServer());
    MnyB a = new MnyB("a100");
    MnyB b = new MnyB("b200");

    EbeanServer server = Ebean.getDefaultServer();
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
        Transaction anotherTxn = server.createTransaction();
        // success prior to commitAndContinue
        assertNotNull(server.find(MnyB.class, a.getId(), anotherTxn));
        // insert failed after commitAndContinue
        assertNull(server.find(MnyB.class, b.getId(), anotherTxn));
      }

      // does not commit due to the txn.setRollbackOnly();
      txn.commit();

    } finally {
      server.endTransaction();
    }
  }

  @Test
  @Transactional
  public void transactional_partialSuccess_secondTransactionInsert() {
    assumeFalse("Skipping test because MS SQL Server will dead lock here.", isMsSqlServer());
    MnyB a = new MnyB("a100");
    MnyB b = new MnyB("b200");
    MnyB c = new MnyB("c300");

    a.save();

    // commit at this point
    Ebean.currentTransaction().commitAndContinue();

    try {
      b.save();

      // some error occurs
      throw new IllegalStateException();

    } catch (IllegalStateException e) {
      // mark the transaction as rollback
      Ebean.currentTransaction().setRollbackOnly();

      // use a different transaction to do something useful
      EbeanServer server = Ebean.getDefaultServer();
      Transaction txn2 = server.createTransaction();
      try {
        server.save(c, txn2);
        txn2.commit();
      } finally {
        txn2.end();
      }
    }

    // asserts

    EbeanServer server = Ebean.getDefaultServer();
    Transaction txnForAssert = server.createTransaction();

    // success prior to commitAndContinue
    assertNotNull(server.find(MnyB.class, a.getId(), txnForAssert));

    // insert failed after commitAndContinue
    assertNull(server.find(MnyB.class, b.getId(), txnForAssert));

    // successful insert using txn2
    assertNotNull(server.find(MnyB.class, c.getId(), txnForAssert));
  }

  @Test
  public void basic() {

    assumeFalse("Skipping test because batching not yet supported for MS SQL Server.",
        isMsSqlServer());

    MnyB a = new MnyB("a");
    MnyB b = new MnyB("b");
    MnyB c = new MnyB("c");

    Transaction txn = Ebean.beginTransaction();
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
  @Transactional
  public void runTransactional() {
    assumeFalse("Skipping test because MS SQL Server will dead lock here.", isMsSqlServer());
    new MnyB("a100").save();
    new MnyB("a101").save();

    Ebean.currentTransaction().commitAndContinue();

    new MnyB("a200").save();
    new MnyB("a201").save();
  }

}
