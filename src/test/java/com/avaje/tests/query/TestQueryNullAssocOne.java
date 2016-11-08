package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestQueryNullAssocOne extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> q0 = Ebean.find(Order.class).where().eq("customer", null).query();

    List<Order> orders = q0.findList();
    Assert.assertNotNull(orders);
    Assert.assertTrue(q0.getGeneratedSql().contains("where t0.kcustomer_id is null"));

    Query<Order> q1 = Ebean.find(Order.class).where().eq("customer.id", null).query();

    List<Order> o1 = q1.findList();
    Assert.assertTrue(o1.size() == orders.size());
    Assert.assertTrue(q1.getGeneratedSql().contains("where t0.kcustomer_id is null"));

    Query<Order> q2 = Ebean.find(Order.class).where().isNull("customer").query();

    List<Order> o2 = q2.findList();
    Assert.assertTrue(o2.size() == orders.size());
    Assert.assertTrue(q2.getGeneratedSql().contains("where t0.kcustomer_id is null"));

    Query<Order> q3 = Ebean.find(Order.class).where().isNull("customer").query();

    List<Order> o3 = q3.findList();
    Assert.assertTrue(o3.size() == orders.size());
    Assert.assertTrue(q3.getGeneratedSql().contains("where t0.kcustomer_id is null"));

  }
}
