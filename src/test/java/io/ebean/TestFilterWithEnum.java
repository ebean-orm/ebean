package io.ebean;

import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestFilterWithEnum extends BaseTestCase {

  @Test
  public void test() throws InterruptedException {

    ResetBasicData.reset();

    List<Order> allOrders = Ebean.find(Order.class).findList();

    Filter<Order> filter = Ebean.filter(Order.class);
    List<Order> newOrders = filter.eq("status", Order.Status.NEW).filter(allOrders);

    Assert.assertNotNull(newOrders);

  }

}
