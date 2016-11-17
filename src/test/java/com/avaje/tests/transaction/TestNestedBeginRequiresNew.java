package com.avaje.tests.transaction;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;
import com.avaje.tests.model.basic.Country;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Product;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNestedBeginRequiresNew extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestNestedBeginRequiresNew.class);

  EbeanServer server = Ebean.getServer(null);

  @Test
  public void test() {

    someOuterMethod();
  }

  private void someOuterMethod() {

    Transaction txn = server.beginTransaction(TxScope.requiresNew());
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
    Transaction txn = server.beginTransaction(TxScope.requiresNew());
    try {

      server.find(Customer.class).findCount();
      txn.commit();

    } finally {
      txn.end();
    }
    logger.debug("someInnerMethod() ... done");

  }
}
