package com.avaje.ebeaninternal.server.deploy;

import java.sql.Timestamp;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;

public class TestBeanDescriptorHasIdProperty extends BaseTestCase {

  SpiEbeanServer spiServer;
  
  public TestBeanDescriptorHasIdProperty() {
    EbeanServer server = Ebean.getServer(null);
    spiServer = (SpiEbeanServer)server;
  }
  
  @Test
  public void testHasId() {
  
    BeanDescriptor<Order> beanDescriptor = spiServer.getBeanDescriptor(Order.class);
    Assert.assertNotNull(beanDescriptor.getIdProperty());
    Assert.assertEquals("id", beanDescriptor.getIdProperty().getName());
    
    Assert.assertNotNull(beanDescriptor.getVersionProperty());
    Assert.assertEquals("updtime", beanDescriptor.getVersionProperty().getName());
    
    Order order = new Order();
    
    Assert.assertFalse(beanDescriptor.hasIdProperty(getIntercept(order)));
    Assert.assertFalse(beanDescriptor.hasVersionProperty(getIntercept(order)));
    
    order.setId(23);
    order.setUpdtime(new Timestamp(System.currentTimeMillis()));

    Assert.assertTrue(beanDescriptor.hasIdProperty(getIntercept(order)));
    Assert.assertTrue(beanDescriptor.hasVersionProperty(getIntercept(order)));
    
  }

  @Test
  public void testIsReference() {
    
    BeanDescriptor<Customer> beanDescriptor = spiServer.getBeanDescriptor(Customer.class);
    
    Customer order = new Customer();
    EntityBeanIntercept ebi = getIntercept(order);
    Assert.assertFalse(beanDescriptor.hasIdPropertyOnly(ebi));
    
    order.setId(23);
    Assert.assertTrue(beanDescriptor.hasIdPropertyOnly(ebi));

    order.setName("custName");
    Assert.assertFalse(beanDescriptor.hasIdPropertyOnly(ebi));
  }
  
  private EntityBeanIntercept getIntercept(Object bean) {
    return ((EntityBean)bean)._ebean_getIntercept();
  }
  
}
