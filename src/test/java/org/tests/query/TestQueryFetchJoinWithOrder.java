package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestQueryFetchJoinWithOrder extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Order> list = Ebean.find(Order.class)
      .fetch("details", new FetchConfig().query())
      .order().asc("id")
      .order().desc("details.id").findList();

    Assert.assertNotNull(list);

    List<Order> list2 = Ebean.find(Order.class)
      .fetch("customer", new FetchConfig().query(5))
      .fetch("customer.contacts")
      .order().asc("id")
      .order().asc("customer.contacts.lastName")
      .findList();

    Assert.assertNotNull(list2);

    List<Customer> list3 = Ebean.find(Customer.class)
      .fetch("orders")
      .filterMany("orders").eq("status", Order.Status.NEW)
      .order().desc("orders.id")
      .findList();

    Assert.assertNotNull(list3);

  }
}
