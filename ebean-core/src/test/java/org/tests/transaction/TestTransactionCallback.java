package org.tests.transaction;

import io.ebean.*;
import org.junit.Test;

import javax.persistence.PersistenceException;

import static org.junit.Assert.assertEquals;

public class TestTransactionCallback extends BaseTestCase {

  int countPreCommit;
  int countPostCommit;
  int countPreRollback;
  int countPostRollback;

  @Test(expected = PersistenceException.class)
  public void test_noActiveTransaction() {

    Ebean.register(new MyCallback());
  }

  @Test
  public void test_commitAndRollback() {


    try (Transaction txn = Ebean.beginTransaction()) {
      Ebean.register(new MyCallback());
      txn.getConnection();
      Ebean.commitTransaction();
    }

    assertEquals(1, countPreCommit);
    assertEquals(1, countPostCommit);
    assertEquals(0, countPreRollback);
    assertEquals(0, countPostRollback);

    Ebean.beginTransaction();
    try {
      Ebean.register(new MyCallback());
    } finally {
      Ebean.rollbackTransaction();
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

  @Test(expected = PersistenceException.class)
  public void test_withEbeanserver() {

    EbeanServer server = Ebean.getServer(null);
    server.register(new MyCallback());
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
