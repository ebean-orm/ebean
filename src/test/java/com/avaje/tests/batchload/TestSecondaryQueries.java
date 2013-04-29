package com.avaje.tests.batchload;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestSecondaryQueries extends BaseTestCase {

  @Test
  public void testQueries() {

    ResetBasicData.reset();

    Order testOrder = ResetBasicData.createOrderCustAndOrder("testSecQry10");
    Integer custId = testOrder.getCustomer().getId();

    Customer cust = Ebean.find(Customer.class).select("name").fetch("contacts", "+query")
        .setId(custId).findUnique();

    Assert.assertNotNull(cust);

    List<Order> list = Ebean.find(Order.class).select("status").fetch("details", "+query(10)")
        .fetch("customer", "+query name, status").fetch("customer.contacts").where()
        .eq("status", Order.Status.NEW).findList();

    Assert.assertTrue(list.size() > 0);
  }

}
