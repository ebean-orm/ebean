package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

public class TestOrderTotalAmountFormula extends BaseTestCase {

  @Test
  public void testAsJoin() {

    ResetBasicData.reset();

    List<Customer> l0 = Ebean.find(Customer.class)
      .select("id, name")
      .fetch("orders", "status, totalAmount")
      .where()
      .eq("orders.details.product.name", "Desk")
      .like("contacts.firstName", "Ji%")
      .findList();

    for (Customer c0 : l0) {
      c0.getId();
      List<Order> orders = c0.getOrders();
      for (Order order : orders) {
        order.getId();
      }
    }

  }


}
