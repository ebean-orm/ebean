package org.tests.query.joins;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestQueryJoinManyNonRoot extends BaseTestCase {

  @Test
  public void test_manyPredicate() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Order> orders = DB.find(Order.class)
      .select("id, status, orderDate")
      .where().gt("details.orderQty", 0)
      .findList();

    assertTrue(!orders.isEmpty());

    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertTrue(loggedSql.get(0).contains("select distinct "));
    assertTrue(loggedSql.get(0).contains(" from o_order t0 join o_order_detail u1 on u1.order_id = t0.id "));
  }

  @Test
  public void test_manyNonRoot() {

    ResetBasicData.reset();

    Query<Order> q = DB.find(Order.class)
      .fetch("customer")
      .fetch("customer.contacts")
      .where().gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();

    assertTrue(!list.isEmpty());
    assertTrue(sql.contains("join o_customer t1 on t1.id "));
    assertTrue(sql.contains("left join contact t2 on"));

    Map<Integer, Order> ordersById = DB.find(Order.class).setMapKey("id").where().gt("id", 0).query().findMap();
    for (Order o: list) {
      int withoutFetch = ordersById.get(o.getId()).getCustomer().getContacts().size();
      int withFetch = o.getCustomer().getContacts().size();
      assertEquals(withoutFetch, withFetch);
    }


    // select t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t1.name c4, t0.cretime c5, t0.updtime c6,
    //        t1.id c7, t1.status c8, t1.name c9, t1.smallnote c10, t1.anniversary c11, t1.cretime c12, t1.updtime c13, t1.billing_address_id c14, t1.shipping_address_id c15,
    //        t2.id c16, t2.first_name c17, t2.last_name c18, t2.phone c19, t2.mobile c20, t2.email c21, t2.cretime c22, t2.updtime c23, t2.customer_id c24, t2.group_id c25
    // from o_order t0
    // join o_customer t1 on t1.id = t0.kcustomer_id
    // left join contact t2 on t2.customer_id = t1.id
    // where t0.id > ? ; --bind(0)

  }

  @Test
  public void test_manyRootQueryJoinOrderDetailsFirst() {

    ResetBasicData.reset();

    Query<Order> q = DB.find(Order.class)
      .fetch("details")
      .fetch("details.product")
      .fetch("customer")
      .fetch("customer.contacts")
      .where().gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();

    assertTrue(!list.isEmpty());
    assertTrue(sql.contains("join o_customer t1 on t1.id "));
    assertTrue(sql.contains("left join o_order_detail "));
    assertTrue(sql.contains("left join o_product "));

    assertFalse(sql.contains("left join contact"));

  }

  @Test
  public void test_manyRootQueryJoinCustomerContactsFirst() {

    ResetBasicData.reset();

    Query<Order> q = DB.find(Order.class)
      .fetch("customer")
      .fetch("customer.contacts")
      .fetchQuery("details")
      .fetch("details.product")
      .where().gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();

    for (Order order : list) {
      order.getCustomer().getContacts().size();
    }

    assertTrue(!list.isEmpty());
    assertTrue(sql.contains("join o_customer t1 on t1.id "));
    assertTrue(sql.contains("left join contact "));

    assertFalse(sql.contains("left join o_order_detail "));
    assertFalse(sql.contains("left join o_product "));

  }

}
