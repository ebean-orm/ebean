package org.tests.query.joins;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.family.ChildPerson;
import org.tests.model.family.ParentPerson;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;

public class TestQueryJoinOnFormula extends BaseTestCase {


  @Before
  public void init() {
    ResetBasicData.reset();
  }

  @Test
  public void test_OrderFindIds() {

    LoggedSqlCollector.start();

    List<Integer> orderIds = Ebean.find(Order.class)
        .where().eq("totalItems", 3)
        .findIds();
    assertThat(orderIds).hasSize(2);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("join (select order_id, count(*) as total_items,");
  }

  @Test
  public void test_OrderFindList() {

    LoggedSqlCollector.start();

    List<Order> orders = Ebean.find(Order.class)
        .where().eq("totalItems", 3)
        .findList();
    assertThat(orders).hasSize(2);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("join (select order_id, count(*) as total_items,");
  }

  @Test
  public void test_OrderFindCount() {

    LoggedSqlCollector.start();

    int orders = Ebean.find(Order.class)
        .where().eq("totalItems", 3)
        .findCount();
    assertThat(orders).isEqualTo(2);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("join (select order_id, count(*) as total_items,");
  }

  @Test
  public void test_OrderFindSingleAttributeList() {

    LoggedSqlCollector.start();

    List<Date> orderDates = Ebean.find(Order.class)
        .select("orderDate")
        .where().eq("totalItems", 3)
        .findSingleAttributeList();
    assertThat(orderDates).hasSize(2);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("join (select order_id, count(*) as total_items,");
  }

  @Test
  public void test_OrderFindOne() {

    LoggedSqlCollector.start();

    Order order = Ebean.find(Order.class)
        .select("totalItems")
        .where().eq("totalItems", 3)
        .setMaxRows(1)
        .orderById(true)
        .findOne();

    assertThat(order.getTotalItems()).isEqualTo(3);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("join (select order_id, count(*) as total_items,");
  }

  @Test
  public void test_ParentPersonFindIds() {

    LoggedSqlCollector.start();

    List<ParentPerson> orderIds = Ebean.find(ParentPerson.class)
        .where().eq("totalAge", 3)
        .findIds();
    // TODO: There are no beans in database, so for now only the query must run.

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }

  @Test
  public void test_ParentPersonFindList() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
        .select("identifier")
        //.where().eq("totalAge", 3)
        .where().eq("familyName", "foo")
        .findList();
    // TODO: There are no beans in database, so for now only the query must run.

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }

  @Test
  public void test_ParentPersonFindCount() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
      .where().eq("totalAge", 3)
      .findCount();
    // TODO: There are no beans in database, so for now only the query must run.

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }

  @Test
  public void test_ParentPersonFindSingleAttributeList() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
      .select("address") // .select("address, totalAge") would work
      .where().eq("totalAge", 3)
      .findSingleAttributeList();
    // TODO: There are no beans in database, so for now only the query must run.

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }

  @Test
  public void test_ParentPersonFindOne() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
      .where().eq("totalAge", 3)
      .setMaxRows(1)
      .orderById(true)
      .findOne();
    // TODO: There are no beans in database, so for now only the query must run.

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }

  @Test
  public void test_ChildPersonParentFindIds() {

    LoggedSqlCollector.start();

    Ebean.find(ChildPerson.class)
        .where().eq("parent.totalAge", 3)
        .findIds();
    // TODO: There are no beans in database, so for now only the query must run.

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }

  @Test
  public void test_ChildPersonParentFindCount() {

    LoggedSqlCollector.start();

    Ebean.find(ChildPerson.class)
        .where().eq("parent.totalAge", 3)
        .findCount();
    // TODO: There are no beans in database, so for now only the query must run.

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
}
