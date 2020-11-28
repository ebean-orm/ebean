package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class TestOneToManyCorrectGrouping extends BaseTestCase {

  public static final int EXPECTED_ITERATIONS = 2;

  @Test
  public void test() {

    ResetBasicData.reset();
    Query<Customer> customerQuery = Ebean.find(Customer.class)
      .fetch("orders")
      .where().le("id", 2)
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
