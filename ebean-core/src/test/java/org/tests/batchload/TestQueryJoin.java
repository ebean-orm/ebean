package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.cache.ServerCache;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestQueryJoin extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    // Need to make sure the customer cache is cleared for the later
    // asserts on the lazy loading
    ServerCache custCache = Ebean.getServerCacheManager().beanCache(Customer.class);
    custCache.clear();

    Query<Order> query = Ebean.find(Order.class).select("status")
      .fetchLazy("customer", "name, status")
      .fetch("customer.contacts").order().asc("id");

    List<Order> list = query.findList();

    // list.get(0).getShipDate();

    Order order = list.get(0);
    BeanState beanStateOrder = Ebean.getBeanState(order);
    assertNotNull(beanStateOrder.getLoadedProps());
    // assertTrue(beanStateOrder.getLoadedProps().contains("id"));
    assertTrue(beanStateOrder.getLoadedProps().contains("status"));
    assertTrue(beanStateOrder.getLoadedProps().contains("shipments"));
    assertTrue(beanStateOrder.getLoadedProps().contains("customer"));

    Customer customer = order.getCustomer();
    BeanState beanStateCustomer = Ebean.getBeanState(customer);
    assertTrue(beanStateCustomer.isReference());

    customer.getName();
    assertNotNull(beanStateCustomer.getLoadedProps());
    assertTrue(beanStateCustomer.getLoadedProps().contains("name"));
    assertTrue(beanStateCustomer.getLoadedProps().contains("status"));
    assertFalse(beanStateCustomer.getLoadedProps().contains("billingAddress"));

    customer.getName();

    Address billingAddress = customer.getBillingAddress();
    System.out.println(billingAddress);
    billingAddress.getLine1();

    assertTrue(!list.isEmpty());

  }
}
