package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.m2m.Role;
import org.tests.model.m2m.Tenant;
import org.tests.model.map.MpRole;
import org.tests.model.map.MpUser;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryJoinToAssocOne extends BaseTestCase {

  @Test
  public void testQueryJoinOnFullyPopulatedParent() {

    ResetBasicData.reset();

    LoggedSql.start();

    // This will use 2 SQL queries to build this object graph
    List<Order> l0 = DB.find(Order.class)
      .fetchQuery("details", "orderQty, unitPrice")
      .fetch("details.product", "sku, name")
      .findList();

    assertThat(l0).isNotEmpty();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(2);

    String secondaryQuery = trimSql(loggedSql.get(1), 1);
    assertThat(secondaryQuery).contains("select t0.order_id, t0.id,");
    assertThat(secondaryQuery).contains(" from o_order_detail t0 left join o_product t1");
    platformAssertIn(secondaryQuery, " (t0.order_id)");
    assertThat(secondaryQuery).contains(" order by t0.order_id, t0.id");
  }


  @Test
  public void testQueryJoinOnPartiallyPopulatedParent() {

    ResetBasicData.reset();

    LoggedSql.start();

    // This will use 2 SQL queries to build this object graph
    List<Order> l0 = DB.find(Order.class)
      .select("status, shipDate")
      .fetchQuery("details", "orderQty, unitPrice")
      .fetch("details.product", "sku, name")
      .findList();

    assertThat(l0).isNotEmpty();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(2);

    String secondaryQuery = trimSql(loggedSql.get(1), 1);
    assertThat(secondaryQuery).contains("select t0.order_id, t0.id,");
    assertThat(secondaryQuery).contains(" from o_order_detail t0 left join o_product t1");
    platformAssertIn(secondaryQuery, " (t0.order_id)");
    assertThat(secondaryQuery).contains(" order by t0.order_id, t0.id");
  }

  @Test
  public void testQueryJoinOnPartiallyPopulatedParent_withLazyLoadingDisabled() {

    ResetBasicData.reset();

    LoggedSql.start();

    // This will use 2 SQL queries to build this object graph
    List<Order> l0 = DB.find(Order.class)
      .setDisableLazyLoading(true)
      .select("status, shipDate")
      .fetchQuery("details", "orderQty, unitPrice")
      .fetch("details.product", "sku, name")
      .orderBy().asc("id")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);
    // normally invokes lazy loading
    order.getOrderDate();

    List<OrderDetail> details = order.getDetails();
    OrderDetail orderDetail = details.get(0);
    // normally invokes lazy loading
    orderDetail.getShipQty();

    // normally invokes lazy loading
    order.getShipments().size();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(2);

    String secondaryQuery = trimSql(loggedSql.get(1), 1);
    assertThat(secondaryQuery).contains("select t0.order_id, t0.id,");
    assertThat(secondaryQuery).contains(" from o_order_detail t0 left join o_product t1");
    platformAssertIn(secondaryQuery, " (t0.order_id)");
    assertThat(secondaryQuery).contains(" order by t0.order_id, t0.id");
  }

  @Test
  public void disableLazyLoading_when_oneToManyList() {

    ResetBasicData.reset();

    LoggedSql.start();

    // This will use 2 SQL queries to build this object graph
    List<Order> l0 = DB.find(Order.class)
      .setDisableLazyLoading(true)
      .select("status, shipDate")
      .orderBy().asc("id")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);
    // try to invoke lazy loading on the bean
    assertThat(order.getCustomer()).isNull();
    assertThat(order.getCretime()).isNull();

    // try to invoke lazy loading on the OneToMany ...
    List<OrderDetail> details = order.getDetails();
    assertThat(details).isEmpty();
    assertThat(details.size()).isEqualTo(0);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
  }

  @Test
  public void disableLazyLoad_when_oneToManySet() {

    Role role0 = new Role("r0");
    Role role1 = new Role("r1");
    DB.save(role0);
    DB.save(role1);

    Tenant tenant = new Tenant("t0");
    tenant.getRoles().add(role0);
    tenant.getRoles().add(role1);

    DB.save(tenant);

    LoggedSql.start();

    Tenant found = DB.find(Tenant.class)
      .setDisableLazyLoading(true)
      .select("name")
      .setId(tenant.getId())
      .findOne();

    assertThat(found.getRoles().size()).isEqualTo(0);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
  }

  @Test
  public void disableLazyLoading_when_oneToManyMap() {

    MpRole r0 = new MpRole();
    r0.setCode("disableLazyLoading");

    MpUser u = new MpUser();
    u.setName("disableLazy");
    u.getRoles().put("disableLazyLoading", r0);
    DB.save(u);

    LoggedSql.start();

    MpUser found = DB.find(MpUser.class)
      .setDisableLazyLoading(true)
      .select("name")
      .setId(u.getId())
      .findOne();

    // normally invokes lazy loading
    assertThat(found.getRoles().size()).isEqualTo(0);

    // only 1 query ... no lazy loading query
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);

    DB.deleteAll(Arrays.asList(found, r0));
  }

  @Test
  public void testJoinOnPartiallyPopulatedParent_withLazyLoadingDisabled() {

    ResetBasicData.reset();

    LoggedSql.start();

    // This will use 2 SQL queries to build this object graph
    List<Order> l0 = DB.find(Order.class)
      .setDisableLazyLoading(true)
      .select("status, shipDate")
      .fetch("details", "orderQty, unitPrice")//, new FetchConfig().query())
      .fetch("details.product", "sku, name")
      .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);
    // normally invokes lazy loading
    order.getOrderDate();

    List<OrderDetail> details = order.getDetails();
    OrderDetail orderDetail = details.get(0);
    // normally invokes lazy loading
    orderDetail.getShipQty();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);

    String originQuery = trimSql(loggedSql.get(0), 5);
    assertThat(originQuery).contains("select t0.id, t0.status, t0.ship_date, t1.id, t1.order_qty, t1.unit_price");
    assertThat(originQuery).contains(" from o_order t0 left join o_order_detail t1 ");
    assertThat(originQuery).contains(" order by t0.id, t1.id asc, t1.order_qty asc, t1.cretime desc;");
  }
}
