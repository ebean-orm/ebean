package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.TransactionCallbackAdapter;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestTransactionCallback extends BaseTestCase {

  int countPreCommit;
  int countPostCommit;
  int countPreRollback;
  int countPostRollback;

  @Test
  public void test_commitAndRollback() {
    try (Transaction txn = DB.beginTransaction()) {
      DB.register(new MyCallback());
      txn.getConnection(); // Ebean assumes writes have occurred
      txn.commit();
    }

    assertEquals(1, countPreCommit);
    assertEquals(1, countPostCommit);
    assertEquals(0, countPreRollback);
    assertEquals(0, countPostRollback);

    DB.beginTransaction();
    try {
      DB.register(new MyCallback());
    } finally {
      DB.rollbackTransaction();
    }

    assertEquals(1, countPreCommit);
    assertEquals(1, countPostCommit);
    assertEquals(1, countPreRollback);
    assertEquals(1, countPostRollback);
  }

  @Test
  public void test_commit_whenNoDbWrite() {
    try (Transaction txn = DB.beginTransaction()) {
      DB.register(new MyCallback());
      txn.commit();
    }

    assertEquals(1, countPreCommit);
    assertEquals(1, countPostCommit);
    assertEquals(0, countPreRollback);
    assertEquals(0, countPostRollback);
  }

  @Test
  public void test_rollback_whenNoDbWrite() {
    try (Transaction txn = DB.beginTransaction()) {
      DB.register(new MyCallback());
      txn.rollback();
    }

    assertEquals(0, countPreCommit);
    assertEquals(0, countPostCommit);
    assertEquals(1, countPreRollback);
    assertEquals(1, countPostRollback);
  }

  @Test
  public void test_noActiveTransaction() {
    assertThrows(PersistenceException.class, () -> DB.register(new MyCallback()));
  }

  @Test
  public void test_noActiveTransaction_withDatabase() {
    assertThrows(PersistenceException.class, () -> DB.getDefault().register(new MyCallback()));
  }

  class MyCallback extends TransactionCallbackAdapter {

    @Override
    public void preCommit() {
      countPreCommit++;
    }

    @Override
    public void postCommit() {
      countPostCommit++;
    }

    @Override
    public void preRollback() {
      countPreRollback++;
    }

    @Override
    public void postRollback() {
      countPostRollback++;
    }
  }
}
