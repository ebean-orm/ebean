package org.tests.transaction;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

public class TestTransactionPerformance extends BaseTestCase {


  private static final int ITERATIONS = 100000;

  /**
   * H2: 207468 Transactions per seconds
   * sqlserver19: 1863 Transactions per seconds
   * mariadb: 252525 Transactions per seconds
   * db2: 6553 Transactions per seconds
   *
   * with lazy transactions: 299401 Transactions per seconds
   */
  @Test
  public void beginTransaction() {
    // warmup
    for (int i = 0; i < 100; i++) {
      doWithCache();
    }

    long time = System.currentTimeMillis();
    for (int i = 0; i < ITERATIONS; i++) {
      doWithCache();
    }
    time = System.currentTimeMillis() - time;
    System.out.println(ITERATIONS * 1000 / time + " Transactions per seconds");
  }

  private static void doWithCache() {
    try (Transaction txn = DB.beginTransaction(TxScope.requiresNew())) {
      DB.find(Customer.class).setUseQueryCache(true).where().eq("name", "Rob").findOne();
      txn.commit();
    }
  }


}
