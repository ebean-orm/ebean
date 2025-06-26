package org.tests.query;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import io.ebean.text.PathProperties;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFetchIdOnly extends BaseTestCase {

  @Test
  void test_withFetchPath() {
    ResetBasicData.reset();

    PathProperties root = PathProperties.parse("status,customer(id)");
    LoggedSql.start();
    Query<Order> query = DB.find(Order.class).apply(root);
    query.findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0");
  }

  @Test
  void test_withSelect() {
    ResetBasicData.reset();

    LoggedSql.start();
    Query<Order> query = DB.find(Order.class).select("status, customer");
    query.findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0");
  }

  @Test
  void test_withFetch() {
    ResetBasicData.reset();

    LoggedSql.start();
    Query<Order> query = DB.find(Order.class).select("status").fetch("customer", "id");
    query.findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0");
  }

}
