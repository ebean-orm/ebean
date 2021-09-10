package org.tests.transaction;

import io.ebean.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Product;

public class TestNestedBeginRequiresNewWithFailure extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestNestedBeginRequiresNewWithFailure.class);

  Database server = DB.getDefault();

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
