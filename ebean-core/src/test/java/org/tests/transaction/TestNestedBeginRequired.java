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
