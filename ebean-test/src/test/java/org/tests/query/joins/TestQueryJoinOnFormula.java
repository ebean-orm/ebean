package org.tests.query.joins;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderShipment;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.TreeNode;
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
    assertSql(sql.get(0)).contains("select count(*) from ( select t0.id from o_order t0 left join (select order_id,");
  }

  @Test
  public void testOrderOnChainedFormulaProperty() {
    // test that join to order.details is not included

    // Tests if SqlTreeBuilder.IncludesDistiller.createExtraJoin appends formulaJoinProperties
    Query<OrderShipment> shipQuery = DB.find(OrderShipment.class)
      .select("id")
      .orderBy().asc("order.totalAmount");

    shipQuery.findList();
    assertSql(shipQuery.getGeneratedSql()).isEqualTo("select t0.id "
      + "from or_order_ship t0 "
      + "left join o_order t1 on t1.id = t0.order_id "
      + "left join (select order_id, count(*) as total_items, sum(order_qty*unit_price) as total_amount from o_order_detail group by order_id) z_bt1 on z_bt1.order_id = t1.id "
      + "order by z_bt1.total_amount");
  }

  @Test
  public void testWhereOnChainedFormulaProperty() {
    // test that join to order.details is not included

    // Tests if SqlTreeBuilder.IncludesDistiller.createExtraJoin appends formulaJoinProperties
    Query<OrderShipment> shipQuery = DB.find(OrderShipment.class)
      .select("id")
      .where().isNotNull("order.totalAmount").query();

    shipQuery.findList();
    assertSql(shipQuery.getGeneratedSql()).isEqualTo("select t0.id "
      + "from or_order_ship t0 "
      + "left join o_order t1 on t1.id = t0.order_id "
      + "left join (select order_id, count(*) as total_items, sum(order_qty*unit_price) as total_amount from o_order_detail group by order_id) z_bt1 on z_bt1.order_id = t1.id "
      + "where z_bt1.total_amount is not null");
  }

  @Test
  public void testWhereOnChainedFormulaManyWhere() {
    // test that join to order.details is not included

    // Tests if SqlTreeBuilder.IncludesDistiller.createExtraJoin appends formulaJoinProperties
    Query<OrderShipment> shipQuery = DB.find(OrderShipment.class)
      .select("id")
      .where().isNotNull("order.shipments.order.totalAmount").query();

    shipQuery.findList();
    if (platformDistinctOn()) {
      assertThat(shipQuery.getGeneratedSql()).contains("select distinct on (t0.id) t0.id from or_order_ship t0");
    } else {
      assertSql(shipQuery.getGeneratedSql()).contains("select distinct t0.id from or_order_ship t0 ");
    }
    assertThat(shipQuery.getGeneratedSql()).contains(
      "from or_order_ship t0 " +
        "join o_order u1 on u1.id = t0.order_id " +
        "join or_order_ship u2 on u2.order_id = u1.id " +
        "join o_order u3 on u3.id = u2.order_id " +
        "left join (select order_id, count(*) as total_items, sum(order_qty*unit_price) as total_amount from o_order_detail group by order_id) z_bu3 on z_bu3.order_id = u3.id " +
        "where z_bu3.total_amount is not null");
  }

  @Test
  public void testOrderOnChainedFormulaPropertyWithFetch() {

    // Tests if SqlTreeBuilder.buildSelectChain appends formulaJoinProperties
    Query<OrderShipment> shipQuery = DB.find(OrderShipment.class)
      .select("id")
      .fetch("order", "totalAmount")
      .orderBy().asc("order.totalAmount");

    shipQuery.findList();
    assertSql(shipQuery.getGeneratedSql()).isEqualTo("select t0.id, t1.id, z_bt1.total_amount "
      + "from or_order_ship t0 "
      + "left join o_order t1 on t1.id = t0.order_id "
      + "left join (select order_id, count(*) as total_items, sum(order_qty*unit_price) as total_amount from o_order_detail group by order_id) z_bt1 on z_bt1.order_id = t1.id "
      + "order by z_bt1.total_amount");

  }

  @Test
  public void testWhereOnChainedFormulaPropertyWithFetch() {
    // Tests if SqlTreeBuilder.buildSelectChain appends formulaJoinProperties
    Query<OrderShipment> shipQuery = DB.find(OrderShipment.class)
      .select("id")
      .fetch("order", "totalAmount")
      .where().isNotNull("order.totalAmount").query();

    shipQuery.findList();
    assertSql(shipQuery.getGeneratedSql()).isEqualTo("select t0.id, t1.id, z_bt1.total_amount "
      + "from or_order_ship t0 "
      + "left join o_order t1 on t1.id = t0.order_id "
      + "left join (select order_id, count(*) as total_items, sum(order_qty*unit_price) as total_amount from o_order_detail group by order_id) z_bt1 on z_bt1.order_id = t1.id "
      + "where z_bt1.total_amount is not null");

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
    assertSql(sql.get(0)).contains("select count(*) from ( select t0.id from o_order t0 left join (select order_id,");
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
    assertThat(loggedSql.get(0))
      .contains("select t0.identifier from child_person t0")
      .contains("left join (select i2.parent_identifier")
      .contains("where coalesce(f2.child_age, 0) = ?");
  }

  @Test
  public void test_ChildPersonParentFindCount() {

    LoggedSql.start();

    DB.find(ChildPerson.class)
      .where().eq("parent.totalAge", 3)
      .findCount();

    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("select count(*) from child_person t0 left join parent_person t1 on t1.identifier = t0.parent_identifier");
    assertThat(loggedSql.get(0)).contains("where coalesce(f2.child_age, 0) = ?");
  }

  @Test
  public void test_fetch_only() {

    LoggedSql.start();

    DB.find(ChildPerson.class).select("name").fetch("parent.effectiveBean").findList();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql.get(0)).contains("from child_person t0 "
      + "left join parent_person t1 on t1.identifier = t0.parent_identifier "
      + "left join grand_parent_person j1 on j1.identifier = t1.parent_identifier "
      + "left join e_basic t2 on t2.id = coalesce(t1.some_bean_id, j1.some_bean_id)");
  }

  @Test
  public void test_softRef() {

    LoggedSql.start();

    DB.find(TreeNode.class)
      .where().isNotNull("ref.id")
      .findList();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql.get(0)).contains("select t0.id, t0.soft_ref, t0.parent_id, t0_ref.id "
      + "from tree_node t0 "
      + "left join e_basic t0_ref on t0_ref.id = t0.soft_ref "
      + "left join e_basic t1 on t1.id = t0_ref.id where t1.id is not null");
  }

  @Test
  public void test_softRefChildren() {

    LoggedSql.start();

    DB.find(TreeNode.class).select("id")
      .where().isNotNull("children.ref.id")
      .findList();

    List<String> loggedSql = LoggedSql.stop();
    if (isPostgresCompatible()) {
      // TBD
    } else {
      assertThat(loggedSql.get(0)).contains("select distinct t0.id "
        + "from tree_node t0 "
        + "join tree_node u1 on u1.parent_id = t0.id "
        + "join e_basic u1_ref on u1_ref.id = u1.soft_ref "
        + "join e_basic u2 on u2.id = u1_ref.id where u2.id is not null");
    }
    LoggedSql.start();
    DB.find(TreeNode.class).select("id")
      .where().isNull("children.ref.id")
      .findList();

    loggedSql = LoggedSql.stop();
    if (isPostgresCompatible()) {
      // TBD
    } else {
      assertThat(loggedSql.get(0)).contains("select distinct t0.id "
        + "from tree_node t0 "
        + "left join tree_node u1 on u1.parent_id = t0.id "
        + "left join e_basic u1_ref on u1_ref.id = u1.soft_ref "
        + "left join e_basic u2 on u2.id = u1_ref.id where u2.id is null");
    }
  }
}
