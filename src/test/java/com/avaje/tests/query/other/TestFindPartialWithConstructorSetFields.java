package com.avaje.tests.query.other;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.Order.Status;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestFindPartialWithConstructorSetFields extends BaseTestCase {

  @Test
  public void test() {
    
    ResetBasicData.reset();

    List<Order> list = Ebean.find(Order.class)
      .setLazyLoadBatchSize(100)
      .setUseCache(false)
      .select("shipDate")
      .findList();

    assertTrue(list.size() > 2);

    // assert first bean is partially loaded
    Order order0 = list.get(0);
    BeanState beanState = Ebean.getBeanState(order0);
    Set<String> loadedProps = beanState.getLoadedProps();
    assertTrue(loadedProps.contains("shipDate"));
    assertTrue(!loadedProps.contains("status"));
    assertTrue(!loadedProps.contains("orderDate"));

    // invoke lazy loading
    order0.getStatus();
    order0.getOrderDate();

    // assert that these beans are fully loaded
    for (int i = 1; i < list.size(); i++) {
      Order order1 = list.get(1);
      beanState = Ebean.getBeanState(order1);
      loadedProps = beanState.getLoadedProps();
      assertNull(loadedProps);
    }

  }
  
}
