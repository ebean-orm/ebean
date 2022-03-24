package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.cache.ServerCache;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;
import org.tests.model.basic.ResetBasicData;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestBeanReferenceRefresh extends BaseTestCase {

  @Test
  public void testMe() {

    ResetBasicData.reset();

    ServerCache beanCache = DB.cacheManager().beanCache(Order.class);
    beanCache.clear();

    Order order = DB.reference(Order.class, 1);

    assertTrue(DB.beanState(order).isReference());

    // invoke lazy loading
    Date orderDate = order.getOrderDate();
    assertNotNull(orderDate);

    Customer customer = order.getCustomer();
    assertNotNull(customer);

    assertFalse(DB.beanState(order).isReference());
    assertNotNull(order.getStatus());
    assertNotNull(order.getDetails());
    assertNull(DB.beanState(order).loadedProps());

    Status status = order.getStatus();
    assertNotSame(status, Status.SHIPPED);
    order.setStatus(Order.Status.SHIPPED);
    DB.refresh(order);

    Status statusRefresh = order.getStatus();
    assertEquals(status, statusRefresh);

  }


}
