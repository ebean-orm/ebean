package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTransactionSkipCache extends BaseTestCase {

  private void findUseQueryCache() {

    Ebean.find(Customer.class)
      .setUseQueryCache(true)
      .setBeanCacheMode(CacheMode.PUT)
      .where().startsWith("name", "Rob")
      .findList();
  }

  private void setup() {
    ResetBasicData.reset();
    // load into L2 query cache
    findUseQueryCache();
  }

  @Test
  public void skipL2QueryCache() {

    setup();

    Transaction transaction = Ebean.beginTransaction();
    try {
      transaction.setSkipCache(true);

      LoggedSqlCollector.start();
      findUseQueryCache();

      List<String> sql = LoggedSqlCollector.stop();
      assertThat(sql).as("We didn't use the L2 query cache").hasSize(1);

    } finally {
      transaction.end();
    }
  }

}
