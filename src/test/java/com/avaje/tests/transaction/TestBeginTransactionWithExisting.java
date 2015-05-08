package com.avaje.tests.transaction;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxIsolation;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;

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

      Ebean.beginTransaction(TxIsolation.READ_COMMITED);

    } finally {
      txn.end();
    }
  }
}
