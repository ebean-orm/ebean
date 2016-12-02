package com.avaje.tests.rawsql;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;

public class TestRawSqlOrmQuery extends BaseTestCase {

  @Test
  public void testNamed() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.createNamedQuery(Order.class, "myRawTest");
    query.setParameter("orderStatus", Order.Status.NEW);
    query.setMaxRows(10);
    List<Order> list = query.findList();
    for (Order order : list) {
      order.getCretime();
    }

    String sql = query.getGeneratedSql();
    if (isMsSqlServer()) {
      assertThat(sql).contains("select  top 10 o.id,");
    } else {
      assertThat(sql).contains("select o.id,");
    }
    assertThat(sql).contains("o.id, o.status, o.ship_date, c.id, c.name, a.id, a.line_1, a.line_2, a.city from o_order o");
    assertThat(sql).contains("join o_customer c on o.kcustomer_id = c.id ");
    assertThat(sql).contains("where o.status = ?  order by c.name, c.id");
  }

  @Test
  public void test() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select r.id, r.name from o_customer r ")
      .columnMapping("r.id", "id")
      .columnMapping("r.name", "name").create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    query.fetch("contacts", new FetchConfig().query());
    query.filterMany("contacts").gt("lastName", "b");

    List<Customer> list = query.findList();
    Assert.assertNotNull(list);
  }

  @Test
  public void testFirstRowsMaxRows() throws InterruptedException, ExecutionException {

    if (isMsSqlServer()) return;

    ResetBasicData.reset();

    RawSql rawSql =
      RawSqlBuilder
        .parse("select r.id, r.name from o_customer r ")
        .columnMapping("r.id", "id")
        .columnMapping("r.name", "name")
        .create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);

    int initialRowCount = query.findCount();

    query.setFirstRow(1);
    query.setMaxRows(2);
    List<Customer> list = query.findList();

    int rowCount = query.findCount();
    FutureRowCount<Customer> futureRowCount = query.findFutureCount();

    Assert.assertEquals(initialRowCount, rowCount);
    Assert.assertEquals(initialRowCount, futureRowCount.get().intValue());

    // check that lazy loading still executes
    for (Customer customer : list) {
      customer.getCretime();
    }

  }

  @Test
  public void testPaging() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select r.id, r.name from o_customer r ")
      .columnMapping("r.id", "id")
      .columnMapping("r.name", "name")
      .create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);

    int initialRowCount = query.findCount();

    PagedList<Customer> page = query.setMaxRows(2).findPagedList();

    List<Customer> list = page.getList();
    int rowCount = page.getTotalCount();

    Assert.assertEquals(2, list.size());
    Assert.assertEquals(initialRowCount, rowCount);

    // check that lazy loading executes
    for (Customer customer : list) {
      customer.getCretime();
    }

  }

  @Test
  public void testPaging_with_existingRawSqlOrderBy_expect_id_appendToOrderBy() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc")
      .columnMapping("o.id", "id")
      .columnMapping("o.order_date", "orderDate")
      .columnMapping("o.ship_date", "shipDate")
      .create();

    Query<Order> query = Ebean.find(Order.class);
    query.setRawSql(rawSql);

    query.setMaxRows(100);
    query.findList();

    if (isMsSqlServer()) {
      assertThat(query.getGeneratedSql()).contains("top 100 ");
      assertThat(query.getGeneratedSql()).contains("order by o.ship_date desc, o.id");
    } else {
      assertThat(query.getGeneratedSql()).contains("order by o.ship_date desc, o.id limit 100");
    }
  }

  @Test
  public void testPaging_when_setOrderBy_expect_id_appendToOrderBy() {
    assumeFalse("sqlserver fails: 'now' is not a recognized built-in function name", isMsSqlServer());
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc nulls last")
      .columnMapping("o.id", "id")
      .columnMapping("o.order_date", "orderDate")
      .columnMapping("o.ship_date", "shipDate")
      .create();

    Query<Order> query = Ebean.find(Order.class);
    query.setRawSql(rawSql);

    query.setMaxRows(100);
    query.order("coalesce(shipDate, now()) desc");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("order by coalesce(o.ship_date, now()) desc, o.id limit 100");
  }

  @Test
  public void testPaging_when_setOrderBy_containsId_expect_leaveAsIs() {

    assumeFalse("sqlserver fails: The ORDER BY clause is invalid in views", isMsSqlServer());
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc")
      .columnMapping("o.id", "id")
      .columnMapping("o.order_date", "orderDate")
      .columnMapping("o.ship_date", "shipDate")
      .create();

    Query<Order> query = Ebean.find(Order.class);
    query.setRawSql(rawSql);

    query.setMaxRows(100);
    query.order("id desc");
    PagedList<Order> pagedList = query.findPagedList();
    pagedList.getList();
    pagedList.getTotalCount();

    assertThat(query.getGeneratedSql()).contains("order by o.id desc limit 100");
  }
}
