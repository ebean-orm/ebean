package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.composite.RCustomer;
import com.avaje.tests.model.composite.RCustomerKey;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestBeanDescriptorHasIdProperty extends BaseTestCase {

  SpiEbeanServer spiServer;
  
  public TestBeanDescriptorHasIdProperty() {
    EbeanServer server = Ebean.getServer(null);
    spiServer = (SpiEbeanServer)server;
  }
  
  @Test
  public void testHasId() {
  
    BeanDescriptor<Order> beanDescriptor = spiServer.getBeanDescriptor(Order.class);
    assertNotNull(beanDescriptor.getIdProperty());
    assertEquals("id", beanDescriptor.getIdProperty().getName());
    
    assertNotNull(beanDescriptor.getVersionProperty());
    assertEquals("updtime", beanDescriptor.getVersionProperty().getName());
    
    Order order = new Order();
    
    assertFalse(beanDescriptor.hasIdValue(entityBean(order)));
    assertFalse(beanDescriptor.hasVersionProperty(getIntercept(order)));
    
    order.setId(23);
    order.setUpdtime(new Timestamp(System.currentTimeMillis()));

    assertTrue(beanDescriptor.hasIdValue(entityBean(order)));
    assertTrue(beanDescriptor.hasVersionProperty(getIntercept(order)));
    
  }

  @Test
  public void testIsReference() {
    
    BeanDescriptor<Customer> beanDescriptor = spiServer.getBeanDescriptor(Customer.class);
    
    Customer order = new Customer();
    EntityBeanIntercept ebi = getIntercept(order);
    assertFalse(beanDescriptor.hasIdPropertyOnly(ebi));
    
    order.setId(23);
    assertTrue(beanDescriptor.hasIdPropertyOnly(ebi));

    order.setName("custName");
    assertFalse(beanDescriptor.hasIdPropertyOnly(ebi));
  }

  @Test
  public void test_getIdForJson() {

    BeanDescriptor<Order> orderDesc = spiServer.getBeanDescriptor(Order.class);

    Order order = new Order();
    order.setId(42);
    assertEquals(42, orderDesc.getIdForJson(order));

    assertEquals(42, orderDesc.convertIdFromJson(42));
    assertEquals(42, orderDesc.convertIdFromJson("42"));
    assertEquals(42, orderDesc.convertIdFromJson(42L));


    RCustomerKey key = new RCustomerKey();
    key.setCompany("comp");
    key.setName("fred");

    RCustomer rCustomer = new RCustomer();
    rCustomer.setKey(key);

    BeanDescriptor<RCustomer> rcustDesc = spiServer.getBeanDescriptor(RCustomer.class);

    Map<String,Object> idForJson = (Map<String,Object>)rcustDesc.getIdForJson(rCustomer);
    assertEquals("comp",idForJson.get("company"));
    assertEquals("fred",idForJson.get("name"));
    assertEquals(2, idForJson.size());

    RCustomerKey keyVal = (RCustomerKey)rcustDesc.convertIdFromJson(idForJson);
    assertEquals("comp",keyVal.getCompany());
    assertEquals("fred",keyVal.getName());

  }

  private EntityBean entityBean(Object bean) {
    return (EntityBean)bean;
  }

  private EntityBeanIntercept getIntercept(Object bean) {
    return ((EntityBean)bean)._ebean_getIntercept();
  }
  
}
