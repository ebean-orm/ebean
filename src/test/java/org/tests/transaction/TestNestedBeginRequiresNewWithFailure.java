package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import io.ebean.TxScope;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Product;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNestedBeginRequiresNewWithFailure extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestNestedBeginRequiresNewWithFailure.class);

  EbeanServer server = Ebean.getServer(null);

  @Test
  public void test() {

    someOuterMethod();
  }

  private void someOuterMethod() {

    Transaction txn = server.beginTransaction(TxScope.requiresNew());
    try {

      server.find(Country.class).findCount();

      try {
        someInnerMethodWithFailure();
      } catch (RuntimeException e) {
        logger.info("Inner method failed with " + e.getMessage());
      }
      server.find(Product.class).findCount();

      txn.commit();

    } finally {
      txn.end();
    }

  }

  private void someInnerMethodWithFailure() {

    logger.debug("someInnerMethod() ...");
    Transaction txn = server.beginTransaction(TxScope.requiresNew());
    try {

      server.find(Customer.class).findCount();

      if (server != null) {
        throw new RuntimeException("barf");
      }
      txn.commit();

    } finally {
      txn.end();
    }
    logger.debug("someInnerMethod() ... done");
  }
}
