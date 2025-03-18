package org.tests.batchload;

import io.ebean.LazyInitialisationException;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

    assertThrows(LazyInitialisationException.class, order::getDetails);

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
    assertThrows(LazyInitialisationException.class, () -> order.getCustomer().getStatus());

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
    assertThrows(LazyInitialisationException.class, () -> order.getCustomer().getStatus());

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
  }

  @Test
  public void onSetter_expect_LazyInitialisationException() {
    ResetBasicData.reset();
    LoggedSql.start();

    List<Order> l0 = DB.find(Order.class)
      .setDisableLazyLoading(true)
      .select("status, orderDate")
      .orderBy().asc("id")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);

    // normally invokes lazy loading
    assertThrows(LazyInitialisationException.class, () -> order.setShipDate(new Date(System.currentTimeMillis())));

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
  }
}
