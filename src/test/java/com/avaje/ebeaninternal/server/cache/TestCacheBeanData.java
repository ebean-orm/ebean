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
  public void test() {
    
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
    //BeanProperty idProperty = desc.getBeanProperty("id");
    //Assert.assertTrue(cacheData.isLoaded(idProperty.getPropertyIndex()));
    
    Assert.assertNotNull(cacheData);
    
    Customer newCustomer = new Customer();
    newCustomer.setId(c.getId());
    CachedBeanDataToBean.load(desc, (EntityBean)newCustomer, cacheData);
    
    Assert.assertEquals(c.getId(), newCustomer.getId());
    Assert.assertEquals(c.getName(), newCustomer.getName());
    Assert.assertEquals(c.getStatus(), newCustomer.getStatus());
    
  }
}
