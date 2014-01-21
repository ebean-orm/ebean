package com.avaje.tests.rawsql;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Page;
import com.avaje.ebean.PagingList;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestRawSqlOrmQuery extends BaseTestCase {

  @Test
    public void test() {

        ResetBasicData.reset();
        
        RawSql rawSql = 
            RawSqlBuilder
                .parse("select r.id, r.name from o_customer r ")
                .columnMapping("r.id", "id")
                .columnMapping("r.name", "name")
                .create();
                    
        Query<Customer> query = Ebean.find(Customer.class);
        query.setRawSql(rawSql);
        query.where().ilike("name", "r%");
        
        query.fetch("contacts", new FetchConfig().query());
        query.filterMany("contacts").gt("lastName", "b");
        
        List<Customer> list = query.findList();
        Assert.assertNotNull(list);
    }
    
  @Test
    public void testPaging() {
      
      ResetBasicData.reset();
      
      RawSql rawSql = 
          RawSqlBuilder
              .parse("select r.id, r.name from o_customer r ")
              .columnMapping("r.id", "id")
              .columnMapping("r.name", "name")
              .create();
                  
      Query<Customer> query = Ebean.find(Customer.class);
      query.setRawSql(rawSql);
      PagingList<Customer> pagingList = query.findPagingList(10);
      Page<Customer> page = pagingList.getPage(0);
      List<Customer> list = page.getList();
      
      System.out.println(page);
      System.out.println(list);
      
      for (Customer customer : list) {
        customer.getCretime();
      }
      
    }
}
