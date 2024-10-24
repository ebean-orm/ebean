package org.tests.query;

import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRowCount extends BaseTestCase {

  @Test
  public void test() {
    ResetBasicData.reset();

    LoggedSql.start();
    Query<Order> query = DB.find(Order.class)
      .fetch("details")
      .where()
      .gt("id", 1)
      .gt("details.id", 1)
      .orderBy("id desc").query();

    int rc = query.findCount();
    List<Object> ids = query.findIds();
    List<Order> list = query.findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).doesNotContain("order by");
    assertThat(sql.get(1)).contains("order by");
    assertThat(sql.get(2)).contains("order by t0.id desc");
    for (Order order : list) {
      order.getStatus();
    }

    assertEquals(rc, ids.size());
    assertEquals(rc, list.size());
  }

  @Test
  public void find_count_distinct_singleProperty() {
    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .setDistinct(true)
      .select("anniversary")
      .where().eq("status", Customer.Status.NEW)
      .query();

    int count = query.findCount();

    assertThat(sqlOf(query)).contains("select count(distinct t0.anniversary) from o_customer t0 where t0.status = ?");
    assertThat(count).isGreaterThan(0);
  }

  @Test
  public void find_count_distinct_multipleProperties() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .setDistinct(true)
      .select("anniversary, status");

    int count = query.findCount();

    assertThat(sqlOf(query)).contains("select count(*) from ( select distinct t0.anniversary, t0.status from o_customer t0)");
    assertThat(count).isGreaterThan(0);
  }

}
