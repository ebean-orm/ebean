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
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestQueryJoin extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    // Need to make sure the customer cache is cleared for the later
    // asserts on the lazy loading
    ServerCache custCache = Ebean.getServerCacheManager().getBeanCache(Customer.class);
    custCache.clear();

    Query<Order> query = Ebean.find(Order.class).select("status")
      // .join("details","+query(10)")
      .fetch("customer", "+lazy(10) name, status").fetch("customer.contacts").orderBy().asc("id");
    // .join("customer.billingAddress");

    List<Order> list = query.findList();

    // list.get(0).getShipDate();

    Order order = list.get(0);
    BeanState beanStateOrder = Ebean.getBeanState(order);
    Assert.assertNotNull(beanStateOrder.getLoadedProps());
    // Assert.assertTrue(beanStateOrder.getLoadedProps().contains("id"));
    Assert.assertTrue(beanStateOrder.getLoadedProps().contains("status"));
    Assert.assertTrue(beanStateOrder.getLoadedProps().contains("shipments"));
    Assert.assertTrue(beanStateOrder.getLoadedProps().contains("customer"));

    Customer customer = order.getCustomer();
    BeanState beanStateCustomer = Ebean.getBeanState(customer);
    Assert.assertTrue(beanStateCustomer.isReference());

    customer.getName();
    Assert.assertNotNull(beanStateCustomer.getLoadedProps());
    Assert.assertTrue(beanStateCustomer.getLoadedProps().contains("name"));
    Assert.assertTrue(beanStateCustomer.getLoadedProps().contains("status"));
    Assert.assertFalse(beanStateCustomer.getLoadedProps().contains("billingAddress"));

    customer.getName();

    Address billingAddress = customer.getBillingAddress();
    System.out.println(billingAddress);
    billingAddress.getLine1();

    Assert.assertTrue(!list.isEmpty());

  }
}
