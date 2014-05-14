package com.avaje.tests.query.other;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.Order.Status;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestFindPartialWithConstructorSetFields extends BaseTestCase {

  @Test
  public void test() {
    
    ResetBasicData.reset();
    
    
    List<Order> list = Ebean.find(Order.class)
      .select("shipDate")
      .findList();
    
    for (Order order : list) {
      BeanState beanState = Ebean.getBeanState(order);
      Set<String> loadedProps = beanState.getLoadedProps();
      Assert.assertTrue(loadedProps.contains("shipDate"));
      Assert.assertTrue(!loadedProps.contains("status"));
      Assert.assertTrue(!loadedProps.contains("orderDate"));
      
      Status status = order.getStatus();
      Date orderDate = order.getOrderDate();
      System.out.println("-- order - "+order.getId()+" status:"+status+" date:"+orderDate);
    }
    
  }
  
}
