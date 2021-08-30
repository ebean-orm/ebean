package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.ExpressionList;
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

    Customer customer = DB.find(Customer.class)
      .fetchLazy("orders")
      .filterMany("orders").eq("status", Order.Status.NEW)
      .where().ieq("name", "Rob")
      .order().asc("id").setMaxRows(1)
      .findList().get(0);

    final int size = customer.getOrders().size();
    assertThat(size).isGreaterThan(0);

    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(1)).contains("status = ?");

    // Currently this does not include the query filter
    DB.refreshMany(customer, "orders");

  }

  @Test
  public void filterMany_firstMaxRows_fluidStyle() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    final Query<Customer> query = DB.find(Customer.class)
      .where().ieq("name", "Rob")
      // fluid style adding maxRows/firstRow to filterMany
      .filterMany("orders").eq("status", Order.Status.NEW).order("id desc").setMaxRows(100).setFirstRow(3)
      .order().asc("id").setMaxRows(5);

    final List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();
    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(0)).contains("lower(t0.name) = ?");
    assertThat(sqlList.get(1)).contains("status = ?");

    if (isH2() || isPostgres()) {
      assertThat(sqlList.get(0)).doesNotContain("offset");
      assertThat(sqlList.get(0)).contains(" limit 5");
      assertThat(sqlList.get(1)).contains(" offset 3");
      assertThat(sqlList.get(1)).contains(" limit 100");
    }
  }

  @Test
  public void test_firstMaxRows() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    final Query<Customer> query = DB.find(Customer.class)
      .where().ieq("name", "Rob")
      .order().asc("id").setMaxRows(5);

    // non-fluid style adding maxRows/firstRow
    final ExpressionList<Customer> filterMany = query.filterMany("orders").order("id desc").eq("status", Order.Status.NEW);
    filterMany.setMaxRows(100);
    filterMany.setFirstRow(3);

    final List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();
    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(2, sqlList.size());

    assertThat(sqlList.get(0)).contains("lower(t0.name) = ?");
    assertThat(sqlList.get(1)).contains("status = ?");

    if (isH2() || isPostgres()) {
      assertThat(sqlList.get(0)).doesNotContain("offset");
      assertThat(sqlList.get(0)).contains(" limit 5");
      assertThat(sqlList.get(1)).contains(" offset 3");
      assertThat(sqlList.get(1)).contains(" limit 100");
    }
  }

  @Test
  public void filterMany_firstMaxRows_expressionFluidStyle() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    final Query<Customer> query = DB.find(Customer.class)
      .where().ieq("name", "Rob")
      // use expression + fluid style adding maxRows/firstRow to filterMany
      .filterMany("orders", "status = ?", Order.Status.NEW)
        .setMaxRows(100).setFirstRow(3).order("orderDate desc, id")
      .order().asc("id").setMaxRows(5);

    final List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();
    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(0)).contains("lower(t0.name) = ?");
    assertThat(sqlList.get(1)).contains("status = ?");

    if (isH2() || isPostgres()) {
      assertThat(sqlList.get(0)).doesNotContain("offset");
      assertThat(sqlList.get(0)).contains(" limit 5");
      assertThat(sqlList.get(1)).contains(" order by t0.order_date desc, t0.id limit 100 offset 3");
    }
  }

  @Test
  public void test_with_findOne() {

    ResetBasicData.reset();

    Customer customer = DB.find(Customer.class)
      .setMaxRows(1)
      .order().asc("id")
      .fetch("orders")
      .filterMany("orders").raw("1 = 0")
      .findOne();

    assertThat(customer).isNotNull();
  }

  @Test
  public void test_with_findOneOrEmpty() {

    ResetBasicData.reset();

    Optional<Customer> customer = DB.find(Customer.class)
      .setMaxRows(1)
      .order().asc("id")
      .fetch("orders")
      .filterMany("orders").raw("1 = 0")
      .findOneOrEmpty();

    assertThat(customer).isPresent();
  }

  @Test
  public void test_filterMany_with_isNotEmpty() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Query<Customer> query = DB.find(Customer.class)
      .filterMany("orders").raw("1=0")
      .where().isNotEmpty("orders")
      .query();

    List<Customer> list = query.findList();
    for (Customer customer : list) {
      assertThat(customer.getOrders()).isEmpty();
    }

    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(1, sqlList.size());
    assertThat(sqlList.get(0)).contains("from o_customer t0 left join o_order t1 on t1.kcustomer_id = t0.id and t1.order_date is not null left join o_customer t2 on t2.id = t1.kcustomer_id where exists (select 1 from o_order x where x.kcustomer_id = t0.id and x.order_date is not null) and 1=0 order by t0.id");
  }

  @Test
  public void test_filterMany_in_findCount() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Query<Customer> query = DB.find(Customer.class)
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

    Query<Customer> query = DB.find(Customer.class)
      .fetch("orders")
      .filterMany("orders").in("status", Order.Status.NEW)
      .order().asc("id");

    query.copy().findList();

    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(1, sqlList.size());
    assertThat(sqlList.get(0)).contains("from o_customer t0 left join o_order t1 on t1.kcustomer_id = t0.id and t1.order_date is not null left join o_customer t2 on t2.id = t1.kcustomer_id where t1.status ");
    if (isPostgres()) {
      assertThat(sqlList.get(0)).contains("where t1.status = any(?) order by t0.id");
    } else {
      assertThat(sqlList.get(0)).contains("where t1.status in (?) order by t0.id");
    }
  }

  @Test
  public void test_filterMany_fetchQuery() {

    ResetBasicData.reset();
    LoggedSqlCollector.start();

    Query<Customer> query = DB.find(Customer.class)
      .fetchQuery("orders") // explicitly fetch orders separately
      .filterMany("orders").in("status", Order.Status.NEW)
      .order().asc("id");

    query.findList();

    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(0)).contains("from o_customer t0");
    if (isPostgres()) {
      assertThat(sqlList.get(1)).contains("from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id where t0.order_date is not null and (t0.kcustomer_id) = any(?)");
      assertThat(sqlList.get(1)).contains(" and t0.status = any(?)");
    } else {
      assertThat(sqlList.get(1)).contains("from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id where t0.order_date is not null and (t0.kcustomer_id) in ");
      assertThat(sqlList.get(1)).contains(" and t0.status in ");
    }
  }

  @Test
  public void testDisjunction() {

    ResetBasicData.reset();
    LoggedSqlCollector.start();

    DB.find(Customer.class)
      .filterMany("orders")
      .or()
      .eq("status", Order.Status.NEW)
      .eq("orderDate", LocalDate.now())
      .findList();

    List<String> sql = LoggedSqlCollector.stop();
    assertEquals(1, sql.size());
    assertSql(sql.get(0)).contains(" from o_customer t0 left join o_order t1 on t1.kcustomer_id = t0.id and t1.order_date is not null left join o_customer t2 on t2.id = t1.kcustomer_id where (t1.status = ? or t1.order_date = ?) order by t0.id");
  }

  @Test
  public void testNestedFilterMany() {

    ResetBasicData.reset();
    LoggedSqlCollector.start();
    DB.find(Customer.class)
      .filterMany("contacts").isNotNull("firstName")
      .filterMany("contacts.notes").istartsWith("title", "foo")
      .findList();

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains(" from o_customer t0 left join contact t1 on t1.customer_id = t0.id where t1.first_name is not null order by t0.id; --bind()");
    platformAssertIn(sql.get(1), " from contact_note t0 where (t0.contact_id)");
    assertSql(sql.get(1)).contains(" and lower(t0.title) like");
  }

  @Test
  public void testFilterManyUsingExpression() {

    ResetBasicData.reset();
    LoggedSqlCollector.start();

    DB.find(Customer.class)
      .where()
      .filterMany("contacts", "firstName isNotNull and email istartsWith ?", "rob")
      .findList();

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains(" from o_customer t0 left join contact t1 on t1.customer_id = t0.id where (t1.first_name is not null and lower(t1.email) like ? escape'|' ) order by t0.id; --bind(rob%)");
  }
}
