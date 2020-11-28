package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestLazyJoin extends BaseTestCase {

  @Test
  public void testLazyOnNonLoaded() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .select("status")
      .fetch("customer", "+lazy(10) name, status")
      .fetch("customer.contacts")
      .order().asc("id");

    List<Order> list = query.findList();

    Order order = list.get(0);
    Customer customer = order.getCustomer();

    // this invokes lazy loading on a property that is
    // not one of the selected ones (name, status) ... and
    // therefore the lazy load query selects all properties
    // in the customer (not just name and status)
    Address billingAddress = customer.getBillingAddress();

    Assert.assertNotNull(billingAddress);
  }

}
