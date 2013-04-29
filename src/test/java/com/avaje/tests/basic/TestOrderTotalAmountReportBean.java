package com.avaje.tests.basic;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.OrderAggregate;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestOrderTotalAmountReportBean extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<OrderAggregate> l0 = Ebean.find(OrderAggregate.class).findList();

    for (OrderAggregate r0 : l0) {
      System.out.println(r0);
    }

    List<OrderAggregate> list = Ebean.createNamedQuery(OrderAggregate.class, "total.amount")
        .where().gt("order.id", 0).having().gt("totalAmount", 50).findList();

    for (OrderAggregate r1 : list) {
      System.out.println(r1);
      Assert.assertTrue(r1.getTotalAmount() > 20.50);
      // partial object query without totalItems
      // ... no lazy loading invoked on this type of bean
      Assert.assertTrue(r1.getTotalItems() == null);
    }

    List<OrderAggregate> l2 = Ebean.createQuery(OrderAggregate.class).where().gt("order.id", 0)
        .having().lt("totalItems", 3).gt("totalAmount", 50).findList();

    for (OrderAggregate r2 : l2) {
      // System.out.println(r2);
      Assert.assertTrue(r2.getTotalItems() < 3);
    }

  }

}
