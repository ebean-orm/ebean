package com.avaje.tests.transaction;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TransactionCallbackAdapter;
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


    Transaction txn = Ebean.beginTransaction();
    Ebean.register(new MyCallback());
    txn.getConnection();
    Ebean.commitTransaction();

    assertEquals(1, countPreCommit);
    assertEquals(1, countPostCommit);
    assertEquals(0, countPreRollback);
    assertEquals(0, countPostRollback);

    Ebean.beginTransaction();
    Ebean.register(new MyCallback());
    Ebean.rollbackTransaction();

    assertEquals(1, countPreCommit);
    assertEquals(1, countPostCommit);
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
