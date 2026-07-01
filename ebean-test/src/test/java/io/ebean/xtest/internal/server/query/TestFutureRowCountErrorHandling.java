package io.ebean.xtest.internal.server.query;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class TestFutureRowCountErrorHandling extends BaseTestCase {

  @Test
  void testFutureRowCount() {
    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();

    FutureRowCount<Customer> futureRowCount = query.findFutureCount();

    assertThrows(ExecutionException.class, futureRowCount::get);
  }

  @Test
  void testFutureIds() {
    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();

    FutureIds<Customer> futureIds = query.findFutureIds();
    assertThrows(ExecutionException.class, futureIds::get);
  }

  @Test
  void testFutureList() {
    ResetBasicData.reset();

    Query<Customer> query = DB.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();

    FutureList<Customer> futureList = query.findFutureList();
    assertThrows(ExecutionException.class, futureList::get);
  }
}
