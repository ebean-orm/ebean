package com.avaje.ebeaninternal.server.cache;

import java.sql.Timestamp;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Country;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Customer.Status;

public class TestCacheBeanData extends BaseTestCase {

  @Test
  public void testCacheBeanExtractAndLoad() {
    
    SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
    BeanDescriptor<Customer> desc = server.getBeanDescriptor(Customer.class);
    
    Customer c = new Customer();
    c.setId(98989);
    c.setName("Rob");
    c.setCretime(new Timestamp(System.currentTimeMillis()));
    c.setUpdtime(new Timestamp(System.currentTimeMillis()));
    c.setStatus(Status.ACTIVE);
    c.setSmallnote("somenote");
    
    Address billingAddress = new Address();
    billingAddress.setId((short)12);
    billingAddress.setCity("Auckland");
    billingAddress.setCountry(server.getReference(Country.class, "NZ"));
    billingAddress.setLine1("92 Someplace Else");
    c.setBillingAddress(billingAddress);
    
    ((EntityBean)c)._ebean_getIntercept().setNewBeanForUpdate();
    
    CachedBeanData cacheData = CachedBeanDataFromBean.extract(desc, (EntityBean)c);

    
    Assert.assertNotNull(cacheData);
    
    Customer newCustomer = new Customer();
    newCustomer.setId(c.getId());
    CachedBeanDataToBean.load(desc, (EntityBean)newCustomer, cacheData);
    
    Assert.assertEquals(c.getId(), newCustomer.getId());
    Assert.assertEquals(c.getName(), newCustomer.getName());
    Assert.assertEquals(c.getStatus(), newCustomer.getStatus());
    Assert.assertEquals(c.getSmallnote(), newCustomer.getSmallnote());
    Assert.assertEquals(c.getCretime(), newCustomer.getCretime());
    Assert.assertEquals(c.getUpdtime(), newCustomer.getUpdtime());
    Assert.assertEquals(c.getBillingAddress().getId(), newCustomer.getBillingAddress().getId());
    
    Assert.assertNotNull(newCustomer.getId());
    Assert.assertNotNull(newCustomer.getName());
    Assert.assertNotNull(newCustomer.getStatus());
    Assert.assertNotNull(newCustomer.getSmallnote());
    Assert.assertNotNull(newCustomer.getCretime());
    Assert.assertNotNull(newCustomer.getUpdtime());
    Assert.assertNotNull(newCustomer.getBillingAddress());
    Assert.assertNotNull(newCustomer.getBillingAddress().getId());
    
  }
}
