package org.tests.query.orderby;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    LoggedSql.start();

    Query<Order> query = DB.find(Order.class);

    // a query that ensures we are going to lazy load on the details
    List<Order> orders = query.findList();

    for (Order order : orders) {
      // invoke lazy loading
      List<OrderDetail> details = order.getDetails();
      details.size();
    }

    // first one is the main query and others are lazy loading queries
    List<String> loggedSql = LoggedSql.stop();
    assertTrue(loggedSql.size() > 1);

    String lazyLoadSql = loggedSql.get(1);
    // contains the foreign key back to the parent bean (t0.order_id)
    assertTrue(trimSql(lazyLoadSql, 2).contains("select t0.order_id, t0.id"));
    assertTrue(lazyLoadSql.contains("order by t0.order_id, t0.id, t0.order_qty, t0.cretime desc"));

  }

  private void checkWithBuiltInManyBasic() {

    Query<Order> query = DB.find(Order.class).fetch("details");
    query.findList();

    String sql = query.getGeneratedSql();

    assertThat(sql).contains("order by t0.id, t1.id asc, t1.order_qty asc, t1.cretime desc");
  }

  private void checkWithBuiltInMany() {

    Query<Order> query = DB.find(Order.class).fetch("details").orderBy().desc("customer.name");

    query.findList();

    String sql = query.getGeneratedSql();

    // t0.id inserted into the middle of the order by
    assertTrue(sql.contains("order by t1.name desc, t0.id, t2.id asc"));
    assertTrue(sql.contains("t2.id asc, t2.order_qty asc, t2.cretime desc"));
  }

  private void checkAppendId() {

    Query<Order> query = DB.find(Order.class).fetch("shipments").orderBy().desc("customer.name");

    query.findList();

    String sql = query.getGeneratedSql();

    // append the id to ensure ordering of root level objects
    assertTrue(sql.contains("order by t1.name desc, t0.id"));
  }

  private void checkNone() {

    Query<Order> query = DB.find(Order.class).orderBy().desc("customer.name");

    query.findList();

    String sql = query.getGeneratedSql();

    // no need to append id to order by as there is no 'many' included in the
    // query
    assertTrue(sql.contains("order by t1.name desc"));
    assertTrue(!sql.contains("order by t1.name desc,"));
  }

  private void checkBoth() {

    Query<Order> query = DB.find(Order.class).fetch("shipments").orderBy()
      .desc("customer.name, shipments.shipTime");

    query.findList();

    String sql = query.getGeneratedSql();
    // insert id into the middle of the order by
    assertTrue(sql.contains("order by t1.name, t0.id, t2.ship_time desc"));
  }

  private void checkPrepend() {

    Query<Order> query = DB.find(Order.class).fetch("shipments").orderBy()
      .desc("shipments.shipTime");

    query.findList();

    String sql = query.getGeneratedSql();
    // prepend id in order by
    assertTrue(sql.contains("order by t0.id, t1.ship_time desc"));
  }

  private void checkAlreadyIncluded() {

    Query<Order> query = DB.find(Order.class).fetch("shipments").orderBy()
      .desc("id, shipments.shipTime");

    query.findList();

    String sql = query.getGeneratedSql();
    // prepend id in order by
    assertTrue(sql.contains("order by t0.id, t1.ship_time desc"));
  }

  private void checkAlreadyIncluded2() {

    Query<Order> query = DB.find(Order.class).fetch("shipments").orderBy()
      .desc("orderDate, id, shipments.shipTime");

    query.findList();

    String sql = query.getGeneratedSql();
    // prepend id in order by
    assertThat(sql).contains("order by t0.order_date, t0.id, t1.ship_time desc");
  }
}
