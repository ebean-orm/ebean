package com.avaje.ebean;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestFilterWithEnum extends BaseTestCase {

  @Test
  public void test() {
    
    ResetBasicData.reset();
    
    List<Order> allOrders = Ebean.find(Order.class).findList();
    
    Filter<Order> filter = Ebean.filter(Order.class);
    List<Order> newOrders = filter.eq("status", Order.Status.NEW).filter(allOrders);
    
    Assert.assertNotNull(newOrders);
    
  }
  
}
