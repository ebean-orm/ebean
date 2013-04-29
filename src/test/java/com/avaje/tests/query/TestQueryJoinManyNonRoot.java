package com.avaje.tests.query;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryJoinManyNonRoot extends BaseTestCase {

  @Test
  public void test_manyNonRoot() {

    ResetBasicData.reset();

    Query<Order> q = Ebean.find(Order.class).fetch("customer").fetch("customer.contacts").where()
        .gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();

    Assert.assertTrue(list.size() > 0);
    Assert.assertTrue(sql.contains("join o_customer t1 on t1.id "));
    Assert.assertTrue(sql.contains("left outer join contact t2 on"));

  }

  @Test
  public void test_manyRootQueryJoinOrderDetailsFirst() {

    ResetBasicData.reset();

    Query<Order> q = Ebean.find(Order.class).fetch("details").fetch("details.product")
        .fetch("customer").fetch("customer.contacts").where().gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();

    Assert.assertTrue(list.size() > 0);
    Assert.assertTrue(sql.contains("join o_customer t1 on t1.id "));
    Assert.assertTrue(sql.contains("left outer join o_order_detail "));
    Assert.assertTrue(sql.contains("left outer join o_product "));

    Assert.assertFalse(sql.contains("left outer join contact"));

  }

  @Test
  public void test_manyRootQueryJoinCustomerContactsFirst() {

    ResetBasicData.reset();

    Query<Order> q = Ebean.find(Order.class).fetch("customer").fetch("customer.contacts")
        .fetch("details").fetch("details.product").where().gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();

    Assert.assertTrue(list.size() > 0);
    Assert.assertTrue(sql.contains("join o_customer t1 on t1.id "));
    Assert.assertTrue(sql.contains("left outer join contact "));

    Assert.assertFalse(sql.contains("left outer join o_order_detail "));
    Assert.assertFalse(sql.contains("left outer join o_product "));

  }

}
