package com.avaje.tests.transaction;

import com.avaje.ebean.*;
import com.avaje.tests.model.basic.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNestedBeginRequiredWithFailure extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestNestedBeginRequiredWithFailure.class);

  EbeanServer server = Ebean.getServer(null);

  @Test
  public void test() {

    someOuterMethod();
  }

  private void someOuterMethod() {

    Transaction txn = server.beginTransaction(TxScope.required());
    try {

      server.find(Country.class).findRowCount();

      try {
        someInnerMethodWithFailure();

        server.find(Product.class).findRowCount();
        txn.commit();

      } catch (RuntimeException e) {
        logger.info("Inner method failed with " + e);
      }

    } finally {
      // For REQUIRED ... the transaction can already been
      // rolled back (by inner method) so that case is expected
      // (and that is the case here - txn is already rolled back)
      txn.end();
    }

  }

  private void someInnerMethodWithFailure() {

    logger.debug("someInnerMethod() ...");
    Transaction txn = server.beginTransaction(TxScope.required());
    try {

      server.find(Customer.class).findRowCount();

      EBasic basic = new EBasic();
      basic.setName("ignore");
      server.save(basic);

      if (server != null) {
        throw new RuntimeException("barf");
      }

      txn.commit();

    } finally {
      txn.end();
      logger.debug("someInnerMethod() ... done");
    }
  }
}
