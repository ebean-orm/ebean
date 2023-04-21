package org.tests.autofetch;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

import java.util.List;

public class MainAutoQueryTune1 {

  public static void main(String[] args) {
    MainAutoQueryTune1 me = new MainAutoQueryTune1();
    me.tuneJoin();
  }

  @Test
  void tuneJoin() {
    TestData.load();

    List<Order> list = DB.find(Order.class)
      .setAutoTune(true)
      //.fetch("customer")
      .where()
      .eq("status", Order.Status.NEW)
      .eq("customer.name", "Rob")
      .order().asc("id")
      .findList();

    for (Order order : list) {
      order.getId();
      order.getOrderDate();
      order.getCustomer().getName();
      order.getCustomer().getNote();
    }
  }

}
