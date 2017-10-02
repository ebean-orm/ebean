package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import io.ebean.FutureRowCount;
import io.ebean.PagedList;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

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
    if (isSqlServer()) {
      assertThat(sql).contains("select top 10 o.id,");
    } else {
      assertThat(sql).contains("select o.id,");
      assertThat(sql).contains("limit 10");
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
  public void test_upperCaseSql() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select id, NAME from O_CUSTOMER ").create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    List<Customer> list = query.findList();
    Assert.assertNotNull(list);
  }

  @Test
  public void testFirstRowsMaxRows() throws InterruptedException, ExecutionException {

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

    if (isSqlServer()) {
      assertThat(query.getGeneratedSql()).contains("top 100 ");
      assertThat(query.getGeneratedSql()).contains("order by o.ship_date desc, o.id");
    } else if (isOracle()) {
      assertThat(sqlOf(query)).contains("order by o.ship_date desc, o.id");
      assertThat(sqlOf(query)).endsWith(") a  where rownum <= 100 ) ");
    } else {
      assertThat(query.getGeneratedSql()).contains("order by o.ship_date desc, o.id limit 100");
    }
  }

  @Test
  public void testPaging_when_setOrderBy_expect_id_appendToOrderBy() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc nulls last")
      .columnMapping("o.id", "id")
      .columnMapping("o.order_date", "orderDate")
      .columnMapping("o.ship_date", "shipDate")
      .create();

    Query<Order> query = Ebean.find(Order.class);
    query.setRawSql(rawSql);

    query.setMaxRows(100);
    
    if (isSqlServer()) {
      query.order("coalesce(shipDate, getdate()) desc");
      query.findList();

      assertThat(sqlOf(query)).contains("order by coalesce(o.ship_date, getdate()) desc, o.id");
      assertThat(sqlOf(query)).contains("select top 100");
      
    } else {
      query.order("coalesce(shipDate, now()) desc");
      query.findList();

      assertThat(query.getGeneratedSql()).contains("order by coalesce(o.ship_date, now()) desc, o.id limit 100");
    }
  }

  @Test
  public void testPaging_when_setOrderBy_containsId_expect_leaveAsIs() {

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

    if (isSqlServer()) {
      assertThat(sqlOf(query)).contains("select top 100 ");
      assertThat(sqlOf(query)).contains("order by o.id desc");
    } else if (isOracle()) {
      assertThat(sqlOf(query)).contains("order by o.id desc");
      assertThat(sqlOf(query)).endsWith(") a  where rownum <= 100 ) ");
    } else {
      assertThat(sqlOf(query)).contains("order by o.id desc limit 100");
    }
  }
}
