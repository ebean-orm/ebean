package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestWhereAnnotation extends BaseTestCase {

  @Test
  public void testWhere() {

    ResetBasicData.reset();
    Customer custTest = ResetBasicData.createCustAndOrder("testWhereAnn");

    Customer customer = Ebean.find(Customer.class, custTest.getId());
    List<Order> orders = customer.getOrders();

    Assert.assertTrue(!orders.isEmpty());

    Query<Customer> q1 = Ebean.find(Customer.class).setUseCache(false).fetch("orders").where()
      .idEq(1).query();

    q1.findOne();
    String s1 = q1.getGeneratedSql();
    Assert.assertTrue(s1.contains("t1.order_date is not null"));
  }
}
