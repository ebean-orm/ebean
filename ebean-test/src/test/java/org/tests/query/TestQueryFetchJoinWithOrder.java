package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
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

    List<Order> list = DB.find(Order.class)
      .fetchQuery("details")
      .orderBy().asc("id")
      .orderBy().desc("details.id").findList();

    assertNotNull(list);

    List<Order> list2 = DB.find(Order.class)
      .fetchQuery("customer")
      .fetch("customer.contacts")
      .orderBy().asc("id")
      .orderBy().asc("customer.contacts.lastName")
      .findList();

    assertNotNull(list2);

    List<Customer> list3 = DB.find(Customer.class)
      .fetch("orders")
      .filterMany("orders").eq("status", Order.Status.NEW)
      .orderBy().desc("orders.id")
      .findList();

    assertNotNull(list3);

  }
}
