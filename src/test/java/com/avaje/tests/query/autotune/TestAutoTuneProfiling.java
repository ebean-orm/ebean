package com.avaje.tests.query.autotune;

import com.avaje.ebean.AdminAutofetch;
import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
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
  }


  private Order findById(long id) {
    return Ebean.find(Order.class)
        .setAutofetch(true)
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

  private static void collectUsage() {

    AdminAutofetch adminAutofetch = Ebean.getServer(null).getAdminAutofetch();
    adminAutofetch.collectUsageViaGC();

  }

}
