package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.time.LocalDate;
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
  public void testDisjunction() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Customer.class)
      .filterMany("orders")
        .or()
          .eq("status", Order.Status.NEW)
          .eq("orderDate", LocalDate.now())
      .findList();

    List<String> sql = LoggedSqlCollector.stop();
    assertEquals(2, sql.size());
    assertThat(sql.get(1)).contains("and (t0.status = ?  or t0.order_date = ?");
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
    platformAssertIn(sql.get(1), " from contact t0 where (t0.customer_id)");
    assertThat(sql.get(1)).contains(" and t0.first_name is not null");
    platformAssertIn(sql.get(2), " from contact_note t0 where (t0.contact_id)");
    assertThat(sql.get(2)).contains(" and lower(t0.title) like");
  }
}
