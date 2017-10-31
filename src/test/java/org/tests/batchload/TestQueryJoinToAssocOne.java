package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.ebeantest.LoggedSqlCollector;
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

    String secondaryQuery = trimSql(loggedSql.get(1), 1);
    assertThat(secondaryQuery).contains("select t0.order_id, t0.id,");
    assertThat(secondaryQuery).contains(" from o_order_detail t0 left join o_product t1");
    platformAssertIn(secondaryQuery, " (t0.order_id)");
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

    String secondaryQuery = trimSql(loggedSql.get(1), 1);
    assertThat(secondaryQuery).contains("select t0.order_id, t0.id,");
    assertThat(secondaryQuery).contains(" from o_order_detail t0 left join o_product t1");
    platformAssertIn(secondaryQuery, " (t0.order_id)");
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

    // normally invokes lazy loading
    order.getShipments().size();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(2);

    String secondaryQuery = trimSql(loggedSql.get(1), 1);
    assertThat(secondaryQuery).contains("select t0.order_id, t0.id,");
    assertThat(secondaryQuery).contains(" from o_order_detail t0 left join o_product t1");
    platformAssertIn(secondaryQuery, " (t0.order_id)");
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

    String originQuery = trimSql(loggedSql.get(0), 5);
    assertThat(originQuery).contains("select t0.id, t0.status, t0.ship_date, t1.id, t1.order_qty, t1.unit_price");
    assertThat(originQuery).contains(" from o_order t0 left join o_order_detail t1 ");
  }
}
