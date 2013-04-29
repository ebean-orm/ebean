package com.avaje.tests.query.orderby;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestOrderByWithMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    checkWithBuiltInMany();
    checkAppendId();
    checkNone();
    checkBoth();
    checkPrepend();
    checkAlreadyIncluded();
    checkAlreadyIncluded2();
  }

  private void checkWithBuiltInMany() {

    Query<Order> query = Ebean.find(Order.class).fetch("details").order().desc("customer.name");

    query.findList();

    String sql = query.getGeneratedSql();

    // t0.id inserted into the middle of the order by
    Assert.assertTrue(sql.contains("order by t1.name desc, t0.id, t2.id asc"));
  }

  private void checkAppendId() {

    Query<Order> query = Ebean.find(Order.class).fetch("shipments").order().desc("customer.name");

    query.findList();

    String sql = query.getGeneratedSql();

    // append the id to ensure ordering of root level objects
    Assert.assertTrue(sql.contains("order by t1.name desc, t0.id"));
  }

  private void checkNone() {

    Query<Order> query = Ebean.find(Order.class).order().desc("customer.name");

    query.findList();

    String sql = query.getGeneratedSql();

    // no need to append id to order by as there is no 'many' included in the
    // query
    Assert.assertTrue(sql.contains("order by t1.name desc"));
    Assert.assertTrue(!sql.contains("order by t1.name desc,"));
  }

  private void checkBoth() {

    Query<Order> query = Ebean.find(Order.class).fetch("shipments").order()
        .desc("customer.name, shipments.shipTime");

    query.findList();

    String sql = query.getGeneratedSql();
    // insert id into the middle of the order by
    Assert.assertTrue(sql.contains("order by t1.name, t0.id, t2.ship_time desc"));
  }

  private void checkPrepend() {

    Query<Order> query = Ebean.find(Order.class).fetch("shipments").order()
        .desc("shipments.shipTime");

    query.findList();

    String sql = query.getGeneratedSql();
    // prepend id in order by
    Assert.assertTrue(sql.contains("order by t0.id, t1.ship_time desc"));
  }

  private void checkAlreadyIncluded() {

    Query<Order> query = Ebean.find(Order.class).fetch("shipments").order()
        .desc("id, shipments.shipTime");

    query.findList();

    String sql = query.getGeneratedSql();
    // prepend id in order by
    Assert.assertTrue(sql.contains("order by t0.id, t1.ship_time desc"));
  }

  private void checkAlreadyIncluded2() {

    Query<Order> query = Ebean.find(Order.class).fetch("shipments").order()
        .desc("orderDate, id, shipments.shipTime");

    query.findList();

    String sql = query.getGeneratedSql();
    // prepend id in order by
    Assert.assertTrue(sql.contains("order by t0.order_date, t0.id, t1.ship_time desc"));
  }
}
