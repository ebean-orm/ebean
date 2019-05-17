package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import io.ebean.Query;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

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
    assertThat(sqlList.get(1)).contains("status = ?");

    // Currently this does not include the query filter
    Ebean.refreshMany(customer, "orders");

  }

  @Test
  public void test_with_findOne() {

    ResetBasicData.reset();

    Customer customer = Ebean.find(Customer.class)
      .setMaxRows(1)
      .orderBy().asc("id")
      .fetch("orders")
      .filterMany("orders").raw("1 = 0")
      .findOne();

    assertThat(customer).isNotNull();
  }

  @Test
  public void test_with_findOneOrEmpty() {

    ResetBasicData.reset();

    Optional<Customer> customer = Ebean.find(Customer.class)
      .setMaxRows(1)
      .orderBy().asc("id")
      .fetch("orders")
      .filterMany("orders").raw("1 = 0")
      .findOneOrEmpty();

    assertThat(customer).isPresent();
  }

  @Test
  public void test_filterMany_with_isNotEmpty() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Query<Customer> query = Ebean.find(Customer.class)
      .fetch("orders")
      .filterMany("orders").raw("1=0")
      .where().isNotEmpty("orders")
      .query();

    List<Customer> list = query.findList();
    for (Customer customer : list) {
      assertThat(customer.getOrders()).isEmpty();
    }

    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(0)).contains("where exists (select 1 from o_order x where x.kcustomer_id = t0.id)");
    assertThat(sqlList.get(1)).contains("and 1=0");
  }


  @Test
  public void test_filterMany_in_findCount() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Query<Customer> query = Ebean.find(Customer.class)
      .fetch("orders")
      .filterMany("orders").in("status", Order.Status.NEW)
      .order().asc("id");

    query.findCount();

    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(1, sqlList.size());
    assertThat(sqlList.get(0)).contains("select count(*) from o_customer");
  }

  @Test
  public void test_filterMany_copy_findList() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Query<Customer> query = Ebean.find(Customer.class)
      .fetch("orders")
      .filterMany("orders").in("status", Order.Status.NEW)
      .order().asc("id");

    query.copy().findList();

    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(0)).contains("from o_customer t0");
    assertThat(sqlList.get(1)).contains("from o_order t0 join o_customer t1");
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
    assertThat(sql.get(1)).contains("and (t0.status = ? or t0.order_date = ?");
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
