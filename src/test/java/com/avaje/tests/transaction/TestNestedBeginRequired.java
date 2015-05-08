package com.avaje.tests.transaction;

import com.avaje.ebean.*;
import com.avaje.tests.model.basic.Country;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Product;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNestedBeginRequired extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestNestedBeginRequired.class);

  EbeanServer server = Ebean.getServer(null);

  @Test
  public void test() {

    someOuterMethod();
  }

  private void someOuterMethod() {

    Transaction txn = server.beginTransaction(TxScope.required());
    try {

      server.find(Country.class).findRowCount();

      someInnerMethod();

      server.find(Product.class).findRowCount();

      txn.commit();

    } finally {
      txn.end();
    }

  }

  private void someInnerMethod() {

    logger.debug("someInnerMethod() ...");
    Transaction txn = server.beginTransaction(TxScope.required());
    try {

      server.find(Customer.class).findRowCount();

      txn.commit();

    } finally {
      txn.end();
    }
    logger.debug("someInnerMethod() ... done");

  }
}
