package org.tests.autofetch;

import io.ebean.DB;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;

import java.time.LocalDate;

public class TestData {

  static void load() {
    if (DB.find(Order.class).findCount() > 0) {
      return;
    }

    var customer = new Customer("Rob");
    customer.setBillingAddress(new Address("l0", "city0"));
    DB.save(customer);

    var order = new Order(customer);
    order.setOrderDate(LocalDate.now());
    DB.save(order);
  }
}
