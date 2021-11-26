package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestOneToManyCorrectGrouping extends BaseTestCase {

  static final int EXPECTED_ITERATIONS = 4;

  @Test
  void test() {
    ResetBasicData.reset();
    Query<Customer> customerQuery = DB.find(Customer.class)
      .fetch("orders")
      .where().le("id", 4)
      .query();

    final AtomicInteger count = new AtomicInteger();

    customerQuery.findEach(customer -> {
      for (Order order : customer.getOrders()) {
        order.getId();
      }
      count.incrementAndGet();
    });
    assertEquals(EXPECTED_ITERATIONS, count.get());
  }
}
