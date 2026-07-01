package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.cache.ServerCache;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestQueryJoin extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    // Need to make sure the customer cache is cleared for the later
    // asserts on the lazy loading
    ServerCache custCache = DB.cacheManager().beanCache(Customer.class);
    custCache.clear();

    Query<Order> query = DB.find(Order.class).select("status")
      .fetchLazy("customer", "name, status")
      .fetch("customer.contacts").orderBy().asc("id");

    List<Order> list = query.findList();

    // list.get(0).getShipDate();

    Order order = list.get(0);
    BeanState beanStateOrder = DB.beanState(order);
    assertNotNull(beanStateOrder.loadedProps());
    // assertTrue(beanStateOrder.getLoadedProps().contains("id"));
    assertThat(beanStateOrder.loadedProps()).contains("status", "shipments", "customer");

    Customer customer = order.getCustomer();
    BeanState beanStateCustomer = DB.beanState(customer);
    assertTrue(beanStateCustomer.isReference());

    customer.getName();
    assertNotNull(beanStateCustomer.loadedProps());
    assertThat(beanStateCustomer.loadedProps()).contains("name", "status");
    assertThat(beanStateCustomer.loadedProps()).doesNotContain("billingAddress");

    customer.getName();

    Address billingAddress = customer.getBillingAddress();
    billingAddress.getLine1();

    assertFalse(list.isEmpty());
  }
}
