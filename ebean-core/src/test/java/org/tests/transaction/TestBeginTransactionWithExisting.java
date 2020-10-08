package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.TxIsolation;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestBeginTransactionWithExisting extends BaseTestCase {

  @Test
  public void testTransactionIsoLevels() {

    assertEquals(Transaction.READ_COMMITTED, Connection.TRANSACTION_READ_COMMITTED);
    assertEquals(Transaction.READ_UNCOMMITTED, Connection.TRANSACTION_READ_UNCOMMITTED);
    assertEquals(Transaction.REPEATABLE_READ, Connection.TRANSACTION_REPEATABLE_READ);
    assertEquals(Transaction.SERIALIZABLE, Connection.TRANSACTION_SERIALIZABLE);
  }

  @Test(expected = PersistenceException.class)
  public void test() {

    assertEquals(Transaction.READ_COMMITTED, Connection.TRANSACTION_READ_COMMITTED);

    Transaction txn = Ebean.beginTransaction();
    try {

      try (Transaction txn2 = Ebean.beginTransaction(TxIsolation.READ_COMMITED)) {
        fail("Expected persitenceException here");
      }

    } finally {
      txn.end();
    }
  }
}
