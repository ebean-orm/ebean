package org.tests.query.joins;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.family.ChildPerson;
import org.tests.model.family.ParentPerson;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQueryJoinOnFormula extends BaseTestCase {

  @BeforeEach
  public void init() {
    ResetBasicData.reset();
  }

  @Test
  public void test_OrderFindIds() {

    LoggedSql.start();

    List<Integer> orderIds = DB.find(Order.class)
        .where().eq("totalItems", 3)
        .findIds();
    assertThat(orderIds).hasSize(2);

    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains(" left join (select order_id, count(*) as total_items,");
  }

  @Test
  public void test_OrderFindList() {

    LoggedSql.start();

    List<Order> orders = DB.find(Order.class)
        .where().eq("totalItems", 3)
        .findList();
    assertThat(orders).hasSize(2);

    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains(" left join (select order_id, count(*) as total_items,");
  }

  @Test
  public void test_OrderFindCount() {

    LoggedSql.start();

    int orders = DB.find(Order.class)
        .where().eq("totalItems", 3)
        .findCount();
    assertThat(orders).isEqualTo(2);

    List<String> sql = LoggedSql.stop();
    assertEquals(1, sql.size());
    assertSql(sql.get(0)).contains("join (select order_id, count(*) as total_items,");
    assertSql(sql.get(0)).contains("select count(*) from ( select t0.id from o_order t0  left join (select order_id,");
  }

  @Test
  public void test_OrderFindCount_multiFormula() {

    LoggedSql.start();

    int orders = DB.find(Order.class)
      .where()
      .eq("totalItems", 3)
      .gt("totalAmount", 10)
      .findCount();

    assertThat(orders).isEqualTo(2);

    List<String> sql = LoggedSql.stop();
    assertEquals(1, sql.size());
    assertSql(sql.get(0)).contains("select count(*) from ( select t0.id from o_order t0  left join (select order_id,");
  }

  @Test
  public void test_OrderFindSingleAttributeList() {

    LoggedSql.start();

    List<Date> orderDates = DB.find(Order.class)
        .select("orderDate")
        .where().eq("totalItems", 3)
        .findSingleAttributeList();
    assertThat(orderDates).hasSize(2);

    List<String> sql = LoggedSql.stop();
    assertEquals(1, sql.size());
    assertSql(sql.get(0)).contains(" left join (select order_id, count(*) as total_items,");
    assertSql(sql.get(0)).contains("select t0.order_date from o_order t0");
  }

  @Test
  public void test_OrderFindOne() {

    LoggedSql.start();

    Order order = DB.find(Order.class)
        .select("totalItems")
        .where().eq("totalItems", 3)
        .setMaxRows(1)
        .orderById(true)
        .findOne();

    assertThat(order.getTotalItems()).isEqualTo(3);

    List<String> sql = LoggedSql.stop();
    assertEquals(1, sql.size());
    assertSql(sql.get(0)).contains("join (select order_id, count(*) as total_items,");
    if (isSqlServer()) {
      assertSql(sql.get(0)).contains("select top 1 t0.id, z_bt0.total_items from o_order t0 join (select");
    } else {
      assertSql(sql.get(0)).contains("select t0.id, z_bt0.total_items from o_order t0 join (select order_id");
    }
  }

  @Test
  public void test_ParentPersonFindIds() {

    LoggedSql.start();

    List<ParentPerson> orderIds = DB.find(ParentPerson.class)
        .where().eq("totalAge", 3)
        .findIds();

    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ?; --bind(3)");
  }

  @Test
  public void test_ParentPersonFindList() {

    LoggedSql.start();

    DB.find(ParentPerson.class)
        .select("identifier")
        //.where().eq("totalAge", 3)
        .where().eq("familyName", "foo")
        .findList();

    List<String> sql = LoggedSql.stop();
    assertEquals(1, sql.size());
    assertSql(sql.get(0)).contains("select t0.identifier from parent_person t0 where t0.family_name = ?");
  }

  @Test
  public void test_ParentPersonFindCount() {

    LoggedSql.start();

    DB.find(ParentPerson.class)
      .where().eq("totalAge", 3)
      .findCount();

    List<String> sql = LoggedSql.stop();
    assertEquals(1, sql.size());
    assertSql(sql.get(0)).contains("where coalesce(f2.child_age, 0) = ?)");
    assertSql(sql.get(0)).contains("select count(*) from ( select t0.identifier from parent_person t0 left join (select i2.parent_identifier, count(*) as child_count");
  }

  @Test
  public void test_ParentPersonFindSingleAttributeList() {

    LoggedSql.start();

    DB.find(ParentPerson.class)
      .select("address")
      .where().eq("totalAge", 3)
      .findSingleAttributeList();

    List<String> sql = LoggedSql.stop();
    assertEquals(1, sql.size());
    assertSql(sql.get(0)).contains("select t0.address from parent_person t0 left join (select i2.parent_identifier");
    assertSql(sql.get(0)).contains("where coalesce(f2.child_age, 0) = ?; --bind(3)");
  }

  @Test
  public void test_ParentPersonFindOne() {

    LoggedSql.start();

    DB.find(ParentPerson.class)
      .where().eq("totalAge", 3)
      .setMaxRows(1)
      .orderById(true)
      .findOne();

    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ? order by t0.identifier");
  }

  @Test
  public void test_ChildPersonParentFindIds() {

    LoggedSql.start();

    DB.find(ChildPerson.class)
        .where().eq("parent.totalAge", 3)
        .findIds();

    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("select t0.identifier from child_person t0 left join (select i2.parent_identifier");
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ?");
  }

  @Test
  public void test_ChildPersonParentFindCount() {

    LoggedSql.start();

    DB.find(ChildPerson.class)
        .where().eq("parent.totalAge", 3)
        .findCount();

    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("select count(*) from ( select t0.identifier");
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ?");
  }
}
