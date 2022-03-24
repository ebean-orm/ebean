package org.tests.query.other;

import io.ebean.xtest.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestFindPartialWithConstructorSetFields extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Order> list = DB.find(Order.class)
      .setLazyLoadBatchSize(100)
      .setUseCache(false)
      .select("shipDate")
      .findList();

    assertTrue(list.size() > 2);

    // assert first bean is partially loaded
    Order order0 = list.get(0);
    BeanState beanState = DB.beanState(order0);
    Set<String> loadedProps = beanState.loadedProps();
    assertTrue(loadedProps.contains("shipDate"));
    assertFalse(loadedProps.contains("status"));
    assertFalse(loadedProps.contains("orderDate"));

    // invoke lazy loading
    order0.getStatus();
    order0.getOrderDate();

    // assert that these beans are fully loaded
    for (int i = 1; i < list.size(); i++) {
      Order order1 = list.get(1);
      beanState = DB.beanState(order1);
      loadedProps = beanState.loadedProps();
      assertNull(loadedProps);
    }

  }

}
