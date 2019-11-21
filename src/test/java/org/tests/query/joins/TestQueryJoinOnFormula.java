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
    assertThat(loggedSql.get(0)).contains(" left join (select order_id, count(*) as total_items,");
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
    assertThat(loggedSql.get(0)).contains(" left join (select order_id, count(*) as total_items,");
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
    assertThat(loggedSql.get(0)).contains("select count(*) from ( select t0.id from o_order t0  left join (select order_id,");
  }

  @Test
  public void test_OrderFindCount_multiFormula() {

    LoggedSqlCollector.start();

    int orders = Ebean.find(Order.class)
      .where()
      .eq("totalItems", 3)
      .gt("totalAmount", 10)
      .findCount();

    assertThat(orders).isEqualTo(2);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("select count(*) from ( select t0.id from o_order t0  left join (select order_id,");
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
    assertThat(loggedSql.get(0)).contains(" left join (select order_id, count(*) as total_items,");
    assertThat(loggedSql.get(0)).contains("select t0.order_date from o_order t0");
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
    if (isSqlServer()) {
      assertThat(loggedSql.get(0)).contains("select top 1 t0.id, z_bt0.total_items from o_order t0 join (select");
    } else {
      assertThat(loggedSql.get(0)).contains("select t0.id, z_bt0.total_items from o_order t0 join (select order_id");
    }
  }

  @Test
  public void test_ParentPersonFindIds() {

    LoggedSqlCollector.start();

    List<ParentPerson> orderIds = Ebean.find(ParentPerson.class)
        .where().eq("totalAge", 3)
        .findIds();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ?; --bind(3)");
  }

  @Test
  public void test_ParentPersonFindList() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
        .select("identifier")
        //.where().eq("totalAge", 3)
        .where().eq("familyName", "foo")
        .findList();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("select t0.identifier from parent_person t0 where t0.family_name = ?");
  }

  @Test
  public void test_ParentPersonFindCount() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
      .where().eq("totalAge", 3)
      .findCount();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ?)");
    assertThat(loggedSql.get(0)).contains("select count(*) from ( select t0.identifier from parent_person t0 left join (select i2.parent_identifier, count(*) as child_count");
  }

  @Test
  public void test_ParentPersonFindSingleAttributeList() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
      .select("address")
      .where().eq("totalAge", 3)
      .findSingleAttributeList();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("select t0.address from parent_person t0 left join (select i2.parent_identifier");
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ?; --bind(3)");
  }

  @Test
  public void test_ParentPersonFindOne() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
      .where().eq("totalAge", 3)
      .setMaxRows(1)
      .orderById(true)
      .findOne();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ? order by t0.identifier");
  }

  @Test
  public void test_ChildPersonParentFindIds() {

    LoggedSqlCollector.start();

    Ebean.find(ChildPerson.class)
        .where().eq("parent.totalAge", 3)
        .findIds();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("select t0.identifier from child_person t0 left join (select i2.parent_identifier");
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ?");
  }

  @Test
  public void test_ChildPersonParentFindCount() {

    LoggedSqlCollector.start();

    Ebean.find(ChildPerson.class)
        .where().eq("parent.totalAge", 3)
        .findCount();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("select count(*) from ( select t0.identifier");
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ?");
  }
}
