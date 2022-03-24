package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

public class TestOrderTotalAmountFormula extends BaseTestCase {

  @Test
  public void testAsJoin() {

    ResetBasicData.reset();

    List<Customer> l0 = DB.find(Customer.class)
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
