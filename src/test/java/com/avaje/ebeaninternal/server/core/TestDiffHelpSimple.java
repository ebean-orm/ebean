package com.avaje.ebeaninternal.server.core;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.ValuePair;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.Order.Status;

public class TestDiffHelpSimple extends BaseTestCase {

  DiffHelp diffHelp = new DiffHelp();

  long firstTime = System.currentTimeMillis()-10000;
  long secondTime = System.currentTimeMillis();

  EbeanServer server;
  BeanDescriptor<Order> orderDesc;
  
  public TestDiffHelpSimple() {
    server = Ebean.getServer(null);
    SpiEbeanServer spiServer = (SpiEbeanServer)server;
    orderDesc = spiServer.getBeanDescriptor(Order.class);
  }
  
  private Order createBaseOrder(EbeanServer server) {
    Order order1 = new Order();
    order1.setId(12);
    order1.setCretime(new Timestamp(firstTime));
    order1.setCustomer(server.getReference(Customer.class, 1234));
    order1.setStatus(Status.NEW);
    order1.setShipDate(new Date(firstTime));
    order1.setOrderDate(new Date(firstTime));
    return order1;
  }
  
  @Test
  public void testBasicChanges() {
    
    
    Order order1 = createBaseOrder(server);
    
    Order order2 = new Order();
    order2.setId(14);
    order2.setCretime(new Timestamp(secondTime));
    order2.setCustomer(server.getReference(Customer.class, 2133));
    order2.setStatus(Status.COMPLETE);
    order2.setShipDate(new Date(secondTime));
    order2.setOrderDate(new Date(secondTime));
    
    Map<String, ValuePair> diff = diffHelp.diff(order1, order2, orderDesc);
    
    Assert.assertEquals(5, diff.size());
    
    Set<String> keySet = diff.keySet();
    Assert.assertTrue(keySet.contains("cretime"));
    Assert.assertTrue(keySet.contains("status"));
    Assert.assertTrue(keySet.contains("shipDate"));
    Assert.assertTrue(keySet.contains("orderDate"));
    Assert.assertTrue(keySet.contains("customer"));
  }


  @Test
  public void testIdIgnored() {
    
    Order order1 = createBaseOrder(server);
    Order order2 = createBaseOrder(server);
    order2.setId(14);
    
    Map<String, ValuePair> diff = diffHelp.diff(order1, order2, orderDesc);
    
    Assert.assertEquals(0, diff.size());    
  }
  
  @Test
  public void testSecondValueNull() {
    
    Order order1 = createBaseOrder(server);
    
    Order order2 = createBaseOrder(server);
    order2.setCustomer(server.getReference(Customer.class, 2133));
    order2.setStatus(Status.COMPLETE);
    order2.setShipDate(null);
    
    Map<String, ValuePair> diff = diffHelp.diff(order1, order2, orderDesc);
    
    Assert.assertEquals(3, diff.size());
    
    Set<String> keySet = diff.keySet();
    Assert.assertTrue(keySet.contains("status"));
    Assert.assertTrue(keySet.contains("customer"));
    Assert.assertTrue(keySet.contains("shipDate"));
    
    ValuePair shipDatePair = diff.get("shipDate");
    Assert.assertEquals(order1.getShipDate(),shipDatePair.getNewValue());
    Assert.assertEquals(order2.getShipDate(),shipDatePair.getOldValue());
    Assert.assertNull(shipDatePair.getOldValue());
  }
  
  
  @Test
  public void testFirstValueNull() {
    
    Order order1 = createBaseOrder(server);
    order1.setShipDate(null);
    
    Order order2 = createBaseOrder(server);    
    order2.setShipDate(new Date(secondTime));
    
    Map<String, ValuePair> diff = diffHelp.diff(order1, order2, orderDesc);
    
    Assert.assertEquals(1, diff.size());    
    Set<String> keySet = diff.keySet();
    Assert.assertTrue(keySet.contains("shipDate"));
    
    ValuePair shipDatePair = diff.get("shipDate");
    Assert.assertEquals(order1.getShipDate(),shipDatePair.getNewValue());
    Assert.assertEquals(order2.getShipDate(),shipDatePair.getOldValue());
    Assert.assertNull(shipDatePair.getNewValue());
  }
  
  @Test
  public void testBothNull() {
    
    Order order1 = createBaseOrder(server);
    order1.setShipDate(null);
    
    Order order2 = createBaseOrder(server);    
    order2.setShipDate(null);
    
    Map<String, ValuePair> diff = diffHelp.diff(order1, order2, orderDesc);
    
    Assert.assertEquals(0, diff.size());    
  }
}
