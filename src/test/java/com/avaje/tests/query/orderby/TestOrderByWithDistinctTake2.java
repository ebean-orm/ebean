package com.avaje.tests.query.orderby;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;

public class TestOrderByWithDistinctTake2 extends BaseTestCase {

  @Test
  public void testRegex() {
    
    String test = "helloasc asc ASC desc DESC boodesc desc ASC";
    
    test = test.replaceAll("(?i)\\b asc\\b|\\b desc\\b", "");
    System.out.println(test);
    Assert.assertEquals("helloasc boodesc", test);
  }
  
  
  @Test
  public void test() {
    
    Query<Customer> query = Ebean.find(Customer.class)
      .select("id")
      .where().ilike("contacts.firstName", "R%")
      .order("name desc");
    
    query.findList();
    
    String generatedSql = query.getGeneratedSql();
    
    // select distinct t0.id c0, t0.name 
    // from o_customer t0 join contact u1 on u1.customer_id = t0.id  
    // where lower(u1.first_name) like ?  
    // order by t0.name; --bind(r%)
    
    Assert.assertTrue("t0.name added to the select clause", generatedSql.contains("select distinct t0.id c0, t0.name"));
    Assert.assertTrue(generatedSql.contains("order by t0.name desc"));
    Assert.assertTrue(generatedSql.contains("from o_customer t0 join contact u1 on u1.customer_id = t0.id"));
    Assert.assertTrue(generatedSql.contains("where lower(u1.first_name) like ?"));
  }
  
  @Test
  public void testWithAscAndDesc() {
    
    Query<Customer> query = Ebean.find(Customer.class)
      .select("id")
      .where().ilike("contacts.firstName", "R%")
      .order("name asc,id desc");
    
    query.findList();
    
    String generatedSql = query.getGeneratedSql();
    
    Assert.assertTrue("t0.name added to the select clause", generatedSql.contains("select distinct t0.id c0, t0.name, t0.id"));
    Assert.assertTrue(generatedSql.contains("order by t0.name, t0.id desc"));
    Assert.assertTrue(generatedSql.contains("from o_customer t0 join contact u1 on u1.customer_id = t0.id"));
    Assert.assertTrue(generatedSql.contains("where lower(u1.first_name) like ?"));
  }
  
}
