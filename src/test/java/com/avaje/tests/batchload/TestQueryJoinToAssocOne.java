package com.avaje.tests.batchload;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryJoinToAssocOne extends BaseTestCase {

  @Test
  public void testQueryJoinOnFullyPopulatedParent() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    // This will use 2 SQL queries to build this object graph
    List<Order> l0 = Ebean.find(Order.class)
        .fetch("details", "orderQty, unitPrice", new FetchConfig().query())
        .fetch("details.product", "sku, name")
        .findList();

    assertThat(l0).isNotEmpty();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(2);

    String secondaryQuery = loggedSql.get(1);
    assertThat(secondaryQuery).contains("select t0.order_id c0, t0.id c1,");
    assertThat(secondaryQuery).contains(" from o_order_detail t0 left outer join o_product t1");
    assertThat(secondaryQuery).contains(" (t0.order_id) in (?");
    assertThat(secondaryQuery).contains(" order by t0.order_id, t0.id");
  }


  @Test
  public void testQueryJoinOnPartiallyPopulatedParent() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    // This will use 2 SQL queries to build this object graph
    List<Order> l0 = Ebean.find(Order.class)
        .select("status, shipDate")
        .fetch("details", "orderQty, unitPrice", new FetchConfig().query())
        .fetch("details.product", "sku, name")
        .findList();

    assertThat(l0).isNotEmpty();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(2);

    String secondaryQuery = loggedSql.get(1);
    assertThat(secondaryQuery).contains("select t0.order_id c0, t0.id c1,");
    assertThat(secondaryQuery).contains(" from o_order_detail t0 left outer join o_product t1");
    assertThat(secondaryQuery).contains(" (t0.order_id) in (?");
    assertThat(secondaryQuery).contains(" order by t0.order_id, t0.id");
  }

  @Test
  public void testQueryJoinOnPartiallyPopulatedParent_withLazyLoadingDisabled() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    // This will use 2 SQL queries to build this object graph
    List<Order> l0 = Ebean.find(Order.class)
        .setDisableLazyLoading(true)
        .select("status, shipDate")
        .fetch("details", "orderQty, unitPrice", new FetchConfig().query())
        .fetch("details.product", "sku, name")
        .order().asc("id")
        .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);
    // normally invokes lazy loading
    order.getOrderDate();

    List<OrderDetail> details = order.getDetails();
    OrderDetail orderDetail = details.get(0);
    // normally invokes lazy loading
    orderDetail.getShipQty();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(2);

    String secondaryQuery = loggedSql.get(1);
    assertThat(secondaryQuery).contains("select t0.order_id c0, t0.id c1,");
    assertThat(secondaryQuery).contains(" from o_order_detail t0 left outer join o_product t1");
    assertThat(secondaryQuery).contains(" (t0.order_id) in (?");
    assertThat(secondaryQuery).contains(" order by t0.order_id, t0.id");
  }

  @Test
  public void testJoinOnPartiallyPopulatedParent_withLazyLoadingDisabled() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    // This will use 2 SQL queries to build this object graph
    List<Order> l0 = Ebean.find(Order.class)
        .setDisableLazyLoading(true)
        .select("status, shipDate")
        .fetch("details", "orderQty, unitPrice")//, new FetchConfig().query())
        .fetch("details.product", "sku, name")
        .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);
    // normally invokes lazy loading
    order.getOrderDate();

    List<OrderDetail> details = order.getDetails();
    OrderDetail orderDetail = details.get(0);
    // normally invokes lazy loading
    orderDetail.getShipQty();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);

    String originQuery = loggedSql.get(0);
    assertThat(originQuery).contains("select t0.id c0, t0.status c1, t0.ship_date c2, t1.id c3, t1.order_qty c4, t1.unit_price c5");
    assertThat(originQuery).contains(" from o_order t0 left outer join o_order_detail t1 ");
  }
}
