package com.avaje.tests.basic;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestWhereAnnotation extends BaseTestCase {

  @Test
  public void testWhere() {

    ResetBasicData.reset();
    Customer custTest = ResetBasicData.createCustAndOrder("testWhereAnn");

    Customer customer = Ebean.find(Customer.class, custTest.getId());
    List<Order> orders = customer.getOrders();

    Assert.assertTrue(orders.size() > 0);

    Query<Customer> q1 = Ebean.find(Customer.class).setUseCache(false).fetch("orders").where()
        .idEq(1).query();

    q1.findUnique();
    String s1 = q1.getGeneratedSql();
    Assert.assertTrue(s1.contains("t1.order_date is not null"));
  }
}
