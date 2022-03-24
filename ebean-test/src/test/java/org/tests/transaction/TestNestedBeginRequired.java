package org.tests.transaction;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Product;

public class TestNestedBeginRequired extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestNestedBeginRequired.class);

  Database server = DB.getDefault();

  @Test
  public void test() {

    someOuterMethod();
  }

  private void someOuterMethod() {

    Transaction txn = server.beginTransaction(TxScope.required());
    try {

      server.find(Country.class).findCount();

      someInnerMethod();

      server.find(Product.class).findCount();

      txn.commit();

    } finally {
      txn.end();
    }

  }

  private void someInnerMethod() {

    logger.debug("someInnerMethod() ...");
    Transaction txn = server.beginTransaction(TxScope.required());
    try {

      server.find(Customer.class).findCount();

      txn.commit();

    } finally {
      txn.end();
    }
    logger.debug("someInnerMethod() ... done");

  }
}
