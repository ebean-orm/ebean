package com.avaje.tests.rawsql;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestRawSqlAsKeyword extends BaseTestCase {
  
  @Test
  public void test() {

    // Make this false to run this test ... as the pipe string concatenation syntax is DB specific
    boolean skipTestAsDBSpecficSQL = true;
    
    if (skipTestAsDBSpecficSQL) {
      
      return;
    }
    
      ResetBasicData.reset();
      
      
      // try valid query where spaces in the formula ...
      RawSql rawSql = 
          RawSqlBuilder
              .parse("select r.id, r.name || 'hello' as name from o_customer r ")
              .create();
                  
      Query<Customer> query = Ebean.find(Customer.class);
      query.setRawSql(rawSql);
      query.where().ilike("name", "r%");
      
      List<Customer> list = query.findList();
      Assert.assertNotNull(list);
      
      
      // try valid query with no spaces
      rawSql = 
          RawSqlBuilder
              .parse("select r.id, r.name||'hello' as name from o_customer r ")
              .create();
                  
      query = Ebean.find(Customer.class);
      query.setRawSql(rawSql);
      query.where().ilike("name", "r%");
      
      list = query.findList();
      Assert.assertNotNull(list);
      
      rawSql = 
          RawSqlBuilder
              .parse("select r.id, r.name||'hello' name from o_customer r ")
              .create();
      query = Ebean.find(Customer.class);
      query.setRawSql(rawSql);
      query.where().ilike("name", "r%");
      
      list = query.findList();
      Assert.assertNotNull(list);
      
      // this will barf - expecting the AS keyword now
      rawSql = 
          RawSqlBuilder
              .parse("select r.id, r.name || 'hello' name from o_customer r ")
              .create();
      query = Ebean.find(Customer.class);
      query.setRawSql(rawSql);
      query.where().ilike("name", "r%");
      
      list = query.findList();
      Assert.assertNotNull(list);
  }
}
