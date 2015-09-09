package com.avaje.tests.query.autotune;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

public class TestAutoTuneProfiling extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    for (int i = 0; i < 1; i++) {
      execute();
    }


    collectUsage();
  }


  private void execute() {
    useOrderDate();
    useOrderDateCustomerName();
    useLots();
    useLotUntuned();
  }

  private Order findById(long id) {
    return Ebean.find(Order.class)
        .select("status, orderDate, shipDate")
        .setId(id)
        .findUnique();
  }

  private void useOrderDate() {
    Order order = findById(3);
    order.getStatus();
    order.getShipDate();
  }

  private void useOrderDateCustomerName() {
    Order order = findById(3);
    order.getOrderDate();
    order.getCustomer().getName();
  }

  private void useLots() {

    Order order = findById(3);
    order.getOrderDate();
    order.getShipDate();
    // order.setShipDate(new Date(System.currentTimeMillis()));
    Customer customer = order.getCustomer();
    customer.getName();
    Address shippingAddress = customer.getShippingAddress();
    if (shippingAddress != null) {
      shippingAddress.getLine1();
      shippingAddress.getCity();
    }
  }

  private void useLotUntuned() {
    Order order = findById(3);
    List<OrderDetail> details = order.getDetails();
    for (OrderDetail detail : details) {
      detail.getProduct().getName();
      detail.getOrderQty();
      detail.getShipQty();
      detail.getUnitPrice();
    }

    Customer customer = order.getCustomer();
    customer.getName();
    Address billingAddress = customer.getBillingAddress();
    if (billingAddress != null) {
      billingAddress.getCity();
      billingAddress.getLine1();
      billingAddress.getLine2();
    }
  }

  private static void collectUsage() {

    Ebean.getDefaultServer().getAutoTune().collectProfiling();
  }

}
