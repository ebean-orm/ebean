package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestQueryFetchJoinWithOrder extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Order> list = Ebean.find(Order.class)
      .fetchQuery("details")
      .order().asc("id")
      .order().desc("details.id").findList();

    assertNotNull(list);

    List<Order> list2 = Ebean.find(Order.class)
      .fetchQuery("customer")
      .fetch("customer.contacts")
      .order().asc("id")
      .order().asc("customer.contacts.lastName")
      .findList();

    assertNotNull(list2);

    List<Customer> list3 = Ebean.find(Customer.class)
      .fetch("orders")
      .filterMany("orders").eq("status", Order.Status.NEW)
      .order().desc("orders.id")
      .findList();

    assertNotNull(list3);

  }
}
