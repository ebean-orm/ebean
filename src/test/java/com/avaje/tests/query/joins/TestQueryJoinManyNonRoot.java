package com.avaje.tests.query.joins;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryJoinManyNonRoot extends BaseTestCase {

  @Test
  public void test_manyNonRoot() {

    ResetBasicData.reset();

    Query<Order> q = Ebean.find(Order.class)
        .fetch("customer")
        .fetch("customer.contacts")
        .where().gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();

    Assert.assertTrue(list.size() > 0);
    Assert.assertTrue(sql.contains("join o_customer t1 on t1.id "));
    Assert.assertTrue(sql.contains("left outer join contact t2 on"));
    
    // select t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t1.name c4, t0.cretime c5, t0.updtime c6, 
    //        t1.id c7, t1.status c8, t1.name c9, t1.smallnote c10, t1.anniversary c11, t1.cretime c12, t1.updtime c13, t1.billing_address_id c14, t1.shipping_address_id c15, 
    //        t2.id c16, t2.first_name c17, t2.last_name c18, t2.phone c19, t2.mobile c20, t2.email c21, t2.cretime c22, t2.updtime c23, t2.customer_id c24, t2.group_id c25 
    // from o_order t0 
    // join o_customer t1 on t1.id = t0.kcustomer_id 
    // left outer join contact t2 on t2.customer_id = t1.id 
    // where t0.id > ? ; --bind(0)
    
  }

  @Test
  public void test_manyRootQueryJoinOrderDetailsFirst() {

    ResetBasicData.reset();

    Query<Order> q = Ebean.find(Order.class)
        .fetch("details")
        .fetch("details.product")
        .fetch("customer")
        .fetch("customer.contacts")
        .where().gt("id", 0).query();

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

    Query<Order> q = Ebean.find(Order.class)
        .fetch("customer")
        .fetch("customer.contacts")
        .fetch("details", new FetchConfig().query(10))
        .fetch("details.product")
        .where().gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();
    
    for (Order order : list) {
      order.getCustomer().getContacts().size();
    }

    Assert.assertTrue(list.size() > 0);
    Assert.assertTrue(sql.contains("join o_customer t1 on t1.id "));
    Assert.assertTrue(sql.contains("left outer join contact "));

    Assert.assertFalse(sql.contains("left outer join o_order_detail "));
    Assert.assertFalse(sql.contains("left outer join o_product "));

  }

}
