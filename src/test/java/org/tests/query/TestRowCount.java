package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestRowCount extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .fetch("details")
      .where()
      .gt("id", 1)
      .gt("details.id", 1)
      .order("id desc");

    int rc = query.findCount();

    List<Object> ids = query.findIds();

    List<Order> list = query.findList();
    System.out.println(list);
    for (Order order : list) {
      order.getStatus();
    }

    assertEquals("same rc to ids.size() ", rc, ids.size());
    assertEquals("same rc to list.size()", rc, list.size());
  }

  @Test
  public void find_count_distinct_singleProperty() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
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

    Query<Customer> query = Ebean.find(Customer.class)
      .setDistinct(true)
      .select("anniversary, status");

    int count = query.findCount();

    assertThat(sqlOf(query)).contains("select count(*) from ( select distinct t0.anniversary, t0.status from o_customer t0)");
    assertThat(count).isGreaterThan(0);
  }

}
