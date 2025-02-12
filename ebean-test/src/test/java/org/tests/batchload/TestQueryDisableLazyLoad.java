package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestQueryDisableLazyLoad extends BaseTestCase {

  @Test
  public void onAssocMany() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Order> l0 = DB.find(Order.class)
      .setDisableLazyLoading(true)
      .orderBy().asc("id")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);

    List<OrderDetail> details = order.getDetails();
    assertEquals(details.size(), 0);

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);

    assertThat(trimSql(loggedSql.get(0), 2)).contains("select t0.id, t0.status, t0.order_date,");
    assertThat(loggedSql.get(0)).contains(" from o_order t0 ");
  }

  @Test
  public void onAssocOne() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Order> l0 = DB.find(Order.class)
      .setDisableLazyLoading(true)
      .orderBy().asc("id")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);

    // normally invokes lazy loading
    assertNull(order.getCustomer().getStatus());

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
  }

  @Test
  public void onAssocOne_when_partial() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Order> l0 = DB.find(Order.class)
      .setDisableLazyLoading(true)
      .fetch("customer", "smallnote")
      .orderBy().asc("id")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);

    // normally invokes lazy loading
    assertNull(order.getCustomer().getStatus());

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
  }
}
