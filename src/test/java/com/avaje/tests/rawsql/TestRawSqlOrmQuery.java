package com.avaje.tests.rawsql;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.avaje.tests.model.basic.Order;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRawSqlOrmQuery extends BaseTestCase {

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

    int initialRowCount = query.findRowCount();

    query.setFirstRow(1);
    query.setMaxRows(2);
    List<Customer> list = query.findList();
    
    int rowCount = query.findRowCount();
    FutureRowCount<Customer> futureRowCount = query.findFutureRowCount();

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
    
    int initialRowCount = query.findRowCount();
    
    PagedList<Customer> page = query.setMaxRows(2).findPagedList();

    List<Customer> list = page.getList();
    int rowCount = page.getTotalRowCount();
    
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

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc nulls last")
        .columnMapping("o.id", "id")
        .columnMapping("o.order_date", "orderDate")
        .columnMapping("o.ship_date", "shipDate")
        .create();

    Query<Order> query = Ebean.find(Order.class);
    query.setRawSql(rawSql);

    query.setMaxRows(100);
    query.findList();

    assertThat(query.getGeneratedSql()).contains("order by o.ship_date desc nulls last, o.id limit 100");
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
    query.order("coalesce(shipDate, now()) desc");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("order by coalesce(o.ship_date, now()) desc, o.id limit 100");
  }

  @Test
  public void testPaging_when_setOrderBy_containsId_expect_leaveAsIs() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc nulls last")
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
    pagedList.getTotalRowCount();

    assertThat(query.getGeneratedSql()).contains("order by o.id desc limit 100");
  }
}
