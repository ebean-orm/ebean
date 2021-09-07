package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.cache.ServerCache;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;
import org.tests.model.basic.ResetBasicData;
import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestBeanReferenceRefresh extends BaseTestCase {

  @Test
  public void testMe() {

    ResetBasicData.reset();

    ServerCache beanCache = Ebean.getServerCacheManager().beanCache(Order.class);
    beanCache.clear();

    Order order = Ebean.getReference(Order.class, 1);

    assertTrue(Ebean.getBeanState(order).isReference());

    // invoke lazy loading
    Date orderDate = order.getOrderDate();
    assertNotNull(orderDate);

    Customer customer = order.getCustomer();
    assertNotNull(customer);

    assertFalse(Ebean.getBeanState(order).isReference());
    assertNotNull(order.getStatus());
    assertNotNull(order.getDetails());
    assertNull(Ebean.getBeanState(order).getLoadedProps());

    Status status = order.getStatus();
    assertTrue(status != Order.Status.SHIPPED);
    order.setStatus(Order.Status.SHIPPED);
    Ebean.refresh(order);

    Status statusRefresh = order.getStatus();
    assertEquals(status, statusRefresh);

  }


}
