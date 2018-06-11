package org.tests.aggregateformula;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAggregateFormula extends BaseTestCase {

  @Test
  public void sum_withoutAlias() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Order> orders = Ebean.find(Order.class)
      .select("status, sum(id)")
      .findList();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.status, sum(t0.id) from o_order t0 group by t0.status");

    assertThat(orders).isNotEmpty();
    for (Order order : orders) {
      assertThat(order.getStatus()).isNotNull();
      assertThat(order.getId()).isNotNull();
    }
  }

  @Test
  public void sum_withAlias() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Order> orders = Ebean.find(Order.class)
      .select("status, sum(id) as id")
      .findList();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.status, sum(t0.id) id from o_order t0 group by t0.status");

    assertThat(orders).isNotEmpty();
    for (Order order : orders) {
      assertThat(order.getStatus()).isNotNull();
      assertThat(order.getId()).isNotNull();
    }
  }

  @Test
  public void sumWithManyToOne_min() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Contact> contacts = Ebean.find(Contact.class)
      .select("customer, min(cretime)")
      .findList();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.customer_id, min(t0.cretime) from contact t0 group by t0.customer_id");

    assertThat(contacts).isNotEmpty();
  }

  @Test
  public void sumWithManyToOne_minMax() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Contact> contacts = Ebean.find(Contact.class)
      .select("customer, min(cretime), max(updtime)")
      .findList();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.customer_id, min(t0.cretime), max(t0.updtime) from contact t0 group by t0.customer_id");

    assertThat(contacts).isNotEmpty();
  }


}
