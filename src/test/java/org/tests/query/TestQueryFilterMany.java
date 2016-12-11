package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestQueryFilterMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Customer customer = Ebean.find(Customer.class)
      .fetch("orders", new FetchConfig().lazy())
      .filterMany("orders").eq("status", Order.Status.NEW)
      .where().ieq("name", "Rob")
      .order().asc("id").setMaxRows(1)
      .findList().get(0);

    customer.getOrders().size();

    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(2, sqlList.size());
    assertTrue(sqlList.get(1).contains("status = ?"));

    // Currently this does not include the query filter
    Ebean.refreshMany(customer, "orders");

  }

  @Test
  public void testNestedFilterMany() {

    ResetBasicData.reset();
    LoggedSqlCollector.start();
    Ebean.find(Customer.class)
      .filterMany("contacts").isNotNull("firstName")
      .filterMany("contacts.notes").istartsWith("title", "foo")
      .findList();

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains(" from o_customer t0; --bind()");
    assertThat(sql.get(1)).contains(" from contact t0 where (t0.customer_id) in");
    assertThat(sql.get(1)).contains(" and t0.first_name is not null");
    assertThat(sql.get(2)).contains(" from contact_note t0 where (t0.contact_id) in");
    assertThat(sql.get(2)).contains(" and lower(t0.title) like");
  }
}
