package org.tests.query;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import io.ebean.text.PathProperties;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFetchIdOnly extends BaseTestCase {

  @Test
  void test_withFetchPath() {
    PathProperties root = PathProperties.parse("status,customer(id)");
    LoggedSql.start();
    Query<Order> query = DB.find(Order.class).apply(root);
    query.findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0;");
  }

  @Test
  void test_withSelect() {
    LoggedSql.start();
    Query<Order> query = DB.find(Order.class).select("status, customer");
    query.findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0;");
  }

  @Test
  void test_withFetch() {
    LoggedSql.start();
    Query<Order> query = DB.find(Order.class).select("status").fetch("customer", "id");
    query.findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0;");
  }

  @Test
  void test_withChildFetch() {
    LoggedSql.start();
    Query<Order> query = DB.find(Order.class).select("status").fetch("customer.shippingAddress", "id");
    query.findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t1.id, t1.shipping_address_id from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id;");
  }
}
