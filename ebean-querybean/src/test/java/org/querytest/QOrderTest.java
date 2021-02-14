package org.querytest;

import io.ebean.DB;
import io.ebean.FetchGroup;
import io.ebean.test.LoggedSql;
import org.example.domain.Order;
import org.example.domain.query.QCustomer;
import org.example.domain.query.QOrder;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QOrderTest {

  private static final QCustomer cu = QCustomer.alias();

  private static final QOrder or = QOrder.alias();

  private static final FetchGroup<Order> fg = QOrder.forFetchGroup()
    .select(or.status, or.shipDate)
    .customer.fetchCache(cu.name, cu.status, cu.registered, cu.comments)
    .buildFetchGroup();

  private static final FetchGroup<Order> fg2 = QOrder.forFetchGroup()
    .select(or.status)
    .customer.fetch(cu.name)
    .buildFetchGroup();

  @Test
  public void fetchCache() {

    new QOrder()
      .status.eq(Order.Status.NEW)
      .customer.fetchCache(cu.name, cu.registered)
      .findList();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .customer.fetchCache()
      .findList();
  }

  @Test
  public void viaFetchGraph() {

    DB.getDefault();
    LoggedSql.start();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .select(fg)
      .findList();


    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.ship_date, t0.customer_id from o_order t0 where");
  }

  @Test
  public void viaFetchGraph_withJoin() {

    DB.getDefault();
    LoggedSql.start();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .select(fg2)
      .findList();


    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t1.id, t1.name from o_order t0 join be_customer t1 on t1.id = t0.customer_id where");
  }

  @Test
  public void select_partial() {

    DB.getDefault();
    LoggedSql.start();

    final QOrder o = QOrder.alias();

    new QOrder()
      .select(o.status, o.orderDate)
      .findList();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.order_date from o_order t0");
  }

  @Test
  public void fetch_partial() {

    DB.getDefault();
    LoggedSql.start();

    final QOrder o = QOrder.alias();
    final QCustomer c = QCustomer.alias();

    new QOrder()
      .select(o.status)
      .customer.fetch(c.email, c.name)
      .findList();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t1.id, t1.email, t1.name from o_order t0 join be_customer t1 on t1.id = t0.customer_id");

  }

}
