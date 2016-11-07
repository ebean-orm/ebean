package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.QueryEachConsumer;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
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
