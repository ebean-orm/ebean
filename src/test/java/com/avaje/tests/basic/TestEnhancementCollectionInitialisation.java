package com.avaje.tests.basic;

import java.util.List;

import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;

import junit.framework.TestCase;


public class TestEnhancementCollectionInitialisation extends TestCase {
  
  public void testListInitialisation() {
    
    Customer customer = new Customer();
    BeanState beanState = Ebean.getBeanState(customer);
    if (beanState != null) {
      List<Order> orders = customer.getOrders();
      assertNotNull(orders);
    }
    
  }

}
