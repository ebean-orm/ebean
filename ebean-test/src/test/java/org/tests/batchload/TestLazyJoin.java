package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestLazyJoin extends BaseTestCase {

  @Test
  public void testLazyOnNonLoaded() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .select("status")
      .fetchLazy("customer", "name, status")
      .fetch("customer.contacts")
      .orderBy().asc("id");

    List<Order> list = query.findList();

    Order order = list.get(0);
    Customer customer = order.getCustomer();

    // this invokes lazy loading on a property that is
    // not one of the selected ones (name, status) ... and
    // therefore the lazy load query selects all properties
    // in the customer (not just name and status)
    Address billingAddress = customer.getBillingAddress();

    assertNotNull(billingAddress);
  }

}
