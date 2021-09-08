package io.ebean;

import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestFilterWithEnum extends BaseTestCase {

  @Test
  public void test() throws InterruptedException {

    ResetBasicData.reset();

    List<Order> allOrders = DB.find(Order.class).findList();

    Filter<Order> filter = DB.filter(Order.class);
    List<Order> newOrders = filter.eq("status", Order.Status.NEW).filter(allOrders);

    assertNotNull(newOrders);
  }

}
