package com.avaje.tests.rawsql;

import java.util.List;
import java.util.concurrent.ExecutionException;

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

public class TestRawSqlOrmQuery extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select r.id, r.name from o_customer r ")
        .columnMapping("r.id", "id")
        .columnMapping("r.name", "name").create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setUseCache(false).setUseQueryCache(false);
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
    query.setUseCache(false).setUseQueryCache(false);
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
    query.setUseCache(false).setUseQueryCache(false);
    query.setRawSql(rawSql);
    
    int initialRowCount = query.findRowCount();
    
    PagedList<Customer> page = query.findPagedList(0, 2);

    List<Customer> list = page.getList();
    int rowCount = page.getTotalRowCount();
    
    Assert.assertEquals(2, list.size());
    Assert.assertEquals(initialRowCount, rowCount);

    // check that lazy loading executes
    for (Customer customer : list) {
      customer.getCretime();
    }

  }
}
