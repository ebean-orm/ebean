package com.avaje.tests.query.other;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryDistinct extends BaseTestCase {

  @Test
  public void test() {
    
    ResetBasicData.reset();
    
    Query<Customer> query = Ebean.find(Customer.class)
      .setUseCache(false)
      .setDistinct(true)
      .select("name");
    
    List<Customer> customers = query.findList();
    
    String generatedSql = query.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains("select distinct t0.name c0 from o_customer t0"));
    
    for (Customer customer : customers) {
      
      EntityBeanIntercept ebi = ((EntityBean)customer)._ebean_getIntercept();
      Assert.assertTrue(ebi.isDisableLazyLoad());
      Assert.assertNull(ebi.getPersistenceContext());
      
      // lazy loading disabled
      Assert.assertNull(customer.getId());
      Assert.assertNull(customer.getAnniversary());
    }
  }

  @Test
  public void test_onWhere() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .setUseCache(false)
        .where().setDistinct(true)
        .select("name");

    query.findList();

    String generatedSql = query.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains("select distinct t0.name c0 from o_customer t0"));
  }
  
  @Test
  public void testDistinctStatus() {
    
    ResetBasicData.reset();
    
    Query<Customer> query = Ebean.find(Customer.class)
      .setUseCache(false)
      .setDistinct(true)
      .select("status")
      .where().isNotNull("status").query();
    
    List<Customer> customers = query.findList();
    
    String generatedSql = query.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains("select distinct t0.status c0 from o_customer t0"));
    
    for (Customer customer : customers) {
      
      Assert.assertNotNull(customer.getStatus());
      
      // lazy loading disabled
      Assert.assertNull(customer.getId());
      Assert.assertNull(customer.getAnniversary());
    }
  }
  
}
