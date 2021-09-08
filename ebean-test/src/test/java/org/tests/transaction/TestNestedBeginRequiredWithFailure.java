package org.tests.transaction;

import io.ebean.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.Product;

public class TestNestedBeginRequiredWithFailure extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestNestedBeginRequiredWithFailure.class);

  Database server = DB.getDefault();

  @Test
  public void test() {

    someOuterMethod();
  }

  private void someOuterMethod() {

    Transaction txn = server.beginTransaction(TxScope.required());
    try {

      server.find(Country.class).findCount();

      try {
        someInnerMethodWithFailure();

        server.find(Product.class).findCount();
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

      server.find(Customer.class).findCount();

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
