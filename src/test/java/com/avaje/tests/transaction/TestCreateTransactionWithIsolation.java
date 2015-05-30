package com.avaje.tests.transaction;

import com.avaje.ebean.*;
import org.junit.Test;

public class TestCreateTransactionWithIsolation extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);
    Transaction txn = server.createTransaction(TxIsolation.SERIALIZABLE);
    txn.end();

  }

}
