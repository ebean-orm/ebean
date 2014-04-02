package com.avaje.tests.batchload;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

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

    Assert.assertTrue(list.size() > 0);

  }
}
