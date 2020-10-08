package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestQueryDisableLazyLoad extends BaseTestCase {

  @Test
  public void onAssocMany() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Order> l0 = Ebean.find(Order.class)
      .setDisableLazyLoading(true)
      .order().asc("id")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);

    List<OrderDetail> details = order.getDetails();
    assertEquals(details.size(), 0);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);

    assertThat(trimSql(loggedSql.get(0), 2)).contains("select t0.id, t0.status, t0.order_date,");
    assertThat(loggedSql.get(0)).contains(" from o_order t0 ");
  }

  @Test
  public void onAssocOne() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Order> l0 = Ebean.find(Order.class)
      .setDisableLazyLoading(true)
      .order().asc("id")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);

    // normally invokes lazy loading
    assertNull(order.getCustomer().getStatus());

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
  }

  @Test
  public void onAssocOne_when_partial() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Order> l0 = Ebean.find(Order.class)
      .setDisableLazyLoading(true)
      .fetch("customer", "smallnote")
      .order().asc("id")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);

    // normally invokes lazy loading
    assertNull(order.getCustomer().getStatus());

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
  }
}
