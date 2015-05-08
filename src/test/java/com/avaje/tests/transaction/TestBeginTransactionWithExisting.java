package com.avaje.tests.transaction;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import org.junit.Test;

import javax.persistence.PersistenceException;

public class TestBeginTransactionWithExisting extends BaseTestCase {

  @Test(expected = PersistenceException.class)
  public void test() {

    Transaction txn = Ebean.beginTransaction();
    try {

      Ebean.beginTransaction();

    } finally {
      txn.end();
    }
  }
}
