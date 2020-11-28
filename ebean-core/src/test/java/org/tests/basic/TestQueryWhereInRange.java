package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryWhereInRange extends BaseTestCase {

  @Test
  public void inRange() {

    ResetBasicData.reset();

    LocalDate today = LocalDate.now();

    Query<Order> query = DB.find(Order.class)
      .where().inRange("orderDate", today.minusDays(7), today)
      .isNotNull("id")
      .query();

    query.findList();

    assertThat(sqlOf(query)).contains("where (t0.order_date >= ? and t0.order_date < ?) and ");
  }

  @Test
  public void eqOrNull() {

    ResetBasicData.reset();

    LocalDate today = LocalDate.now();

    Query<Order> query = DB.find(Order.class)
      .where().eqOrNull("orderDate", today)
      .query();

    query.findList();

    assertSql(query).contains("where (t0.order_date = ? or t0.order_date is null)");
  }

  @Test
  public void gtOrNull() {

    ResetBasicData.reset();

    LocalDate today = LocalDate.now();

    Query<Order> query = DB.find(Order.class)
      .where().gtOrNull("orderDate", today)
      .query();

    query.findList();

    assertSql(query).contains("where (t0.order_date > ? or t0.order_date is null)");
  }

  @Test
  public void geOrNull() {

    ResetBasicData.reset();
    LocalDate today = LocalDate.now();

    Query<Order> query = DB.find(Order.class)
      .where().geOrNull("orderDate", today)
      .query();

    query.findList();

    assertSql(query).contains("where (t0.order_date >= ? or t0.order_date is null)");
  }

  @Test
  public void ltOrNull() {

    ResetBasicData.reset();

    LocalDate today = LocalDate.now();

    Query<Order> query = DB.find(Order.class)
      .where().ltOrNull("orderDate", today)
      .query();

    query.findList();
    assertSql(query).contains(" where (t0.order_date < ? or t0.order_date is null)");
  }

  @Test
  public void leOrNull() {

    ResetBasicData.reset();
    LocalDate today = LocalDate.now();

    Query<Order> query = DB.find(Order.class)
      .where().leOrNull("orderDate", today)
      .query();

    query.findList();
    assertSql(query).contains(" where (t0.order_date <= ? or t0.order_date is null)");
  }

  @Test
  public void inRangeWith() {

    ResetBasicData.reset();

    LocalDate today = LocalDate.now();

    Query<Order> query = DB.find(Order.class)
      .where().inRangeWith("orderDate", "shipDate", today)
      .query();

    query.findList();

    assertSql(query).contains("(t0.order_date <= ? and (t0.ship_date > ? or t0.ship_date is null))");
  }
}
