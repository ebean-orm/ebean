package org.tests.transaction;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Address;
import org.tests.model.basic.Country;
import org.tests.model.basic.Product;

class TestNestedBeginRequiresNew extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestNestedBeginRequiresNew.class);

  Database server = DB.getDefault();

  @Test
  void test() {
    someOuterMethod();
  }

  private void someOuterMethod() {
    Transaction txn = server.beginTransaction(TxScope.requiresNew());
    try {
      server.find(Product.class).findCount();
      someInnerMethod();
      server.find(Address.class).findCount();
      txn.commit();
    } finally {
      txn.end();
    }
  }

  private void someInnerMethod() {
    logger.debug("someInnerMethod() ...");
    Transaction txn = server.beginTransaction(TxScope.requiresNew());
    try {
      server.find(Country.class).findCount();
      txn.commit();
    } finally {
      txn.end();
    }
    logger.debug("someInnerMethod() ... done");
  }
}
