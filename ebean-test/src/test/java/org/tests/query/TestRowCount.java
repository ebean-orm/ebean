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

class TestRowCount extends BaseTestCase {

  @Test
  void test() {
    ResetBasicData.reset();

    LoggedSql.start();
    Query<Order> query = DB.find(Order.class)
      .fetch("details")
      .where()
      .gt("id", 1)
      .gt("details.id", 1)
      .order("id desc").query();

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
  void find_count_distinct_singleProperty() {
    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .setDistinct(true)
      .select("anniversary")
      .where().eq("status", Customer.Status.NEW)
      .query();

    int count = query.findCount();

    // never see column alias used with count distinct on single column
    assertThat(query.getGeneratedSql()).contains("select count(distinct t0.anniversary) from o_customer t0 where t0.status = ?");
    assertThat(count).isGreaterThan(0);
  }

  @Test
  void find_count_distinct_multipleProperties() {
    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .setDistinct(true)
      .select("anniversary, status");

    int count = query.findCount();

    if (isSqlServer() || isH2()) {
      assertThat(query.getGeneratedSql()).contains("select count(*) from ( select distinct t0.anniversary c0, t0.status c1 from o_customer t0)");
    } else {
      assertThat(query.getGeneratedSql()).contains("select count(*) from ( select distinct t0.anniversary, t0.status from o_customer t0)");
    }
    assertThat(count).isGreaterThan(0);
  }

  @Test
  void find_count_distinct_multiplePropertiesSameColumn() {
    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .setDistinct(true)
      .select("id, anniversary, status")
      .fetch("billingAddress", "id, city")
      .fetch("shippingAddress", "id, city");

    int count = query.findCount();

    if (isSqlServer() || isH2()) {
      // must use column alias
      assertThat(query.getGeneratedSql()).contains("select count(*) from ( select distinct t0.anniversary c0, t0.status c1, t1.id c2, t1.city c3, t2.id c4, t2.city c5 from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id left join o_address t2 on t2.id = t0.shipping_address_id)");
    } else {
      assertThat(query.getGeneratedSql()).contains("select count(*) from ( select distinct t0.anniversary, t0.status, t1.id, t1.city, t2.id, t2.city from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id left join o_address t2 on t2.id = t0.shipping_address_id) as c");
    }
    assertThat(count).isGreaterThan(0);
  }

}
