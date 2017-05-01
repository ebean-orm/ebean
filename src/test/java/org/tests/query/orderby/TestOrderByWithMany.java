package org.tests.query.orderby;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestOrderByWithMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    checkWithLazyLoadingOnBuiltInMany();
    checkWithBuiltInManyBasic();
    checkWithBuiltInMany();
    checkAppendId();
    checkNone();
    checkBoth();
    checkPrepend();
    checkAlreadyIncluded();
    checkAlreadyIncluded2();
  }

  private void checkWithLazyLoadingOnBuiltInMany() {

    LoggedSqlCollector.start();

    Query<Order> query = Ebean.find(Order.class);

    // a query that ensures we are going to lazy load on the details
    List<Order> orders = query.findList();

    for (Order order : orders) {
      // invoke lazy loading
      List<OrderDetail> details = order.getDetails();
      details.size();
    }

    // first one is the main query and others are lazy loading queries
    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertTrue(loggedSql.size() > 1);

    String lazyLoadSql = loggedSql.get(1);
    // contains the foreign key back to the parent bean (t0.order_id)
    Assert.assertTrue(lazyLoadSql, trimSql(lazyLoadSql, 2).contains("select t0.order_id, t0.id"));
    Assert.assertTrue(lazyLoadSql.contains("order by t0.order_id, t0.id, t0.order_qty, t0.cretime desc"));

  }

  private void checkWithBuiltInManyBasic() {

    Query<Order> query = Ebean.find(Order.class).fetch("details");
    query.findList();

    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql.contains("order by t0.id, t1.id asc, t1.order_qty asc, t1.cretime desc"));
  }

  private void checkWithBuiltInMany() {

    Query<Order> query = Ebean.find(Order.class).fetch("details").order().desc("customer.name");

    query.findList();

    String sql = query.getGeneratedSql();

    // t0.id inserted into the middle of the order by
    Assert.assertTrue(sql.contains("order by t1.name desc, t0.id, t2.id asc"));
    Assert.assertTrue(sql.contains("t2.id asc, t2.order_qty asc, t2.cretime desc"));
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
