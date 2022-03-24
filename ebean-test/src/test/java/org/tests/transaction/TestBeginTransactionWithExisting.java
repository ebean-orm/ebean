package org.tests.transaction;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.TxIsolation;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestBeginTransactionWithExisting extends BaseTestCase {

  @Test
  public void testTransactionIsoLevels() {
    assertEquals(Transaction.READ_COMMITTED, Connection.TRANSACTION_READ_COMMITTED);
    assertEquals(Transaction.READ_UNCOMMITTED, Connection.TRANSACTION_READ_UNCOMMITTED);
    assertEquals(Transaction.REPEATABLE_READ, Connection.TRANSACTION_REPEATABLE_READ);
    assertEquals(Transaction.SERIALIZABLE, Connection.TRANSACTION_SERIALIZABLE);
  }

  @Test
  public void test() {

    assertEquals(Transaction.READ_COMMITTED, Connection.TRANSACTION_READ_COMMITTED);
    assertThrows(PersistenceException.class, () -> {
      Transaction txn = DB.beginTransaction();
      try {
        try (Transaction txn2 = DB.beginTransaction(TxIsolation.READ_COMMITED)) {
          fail("Expected persitenceException here");
        }
      } finally {
        txn.end();
      }
    });
  }
}
