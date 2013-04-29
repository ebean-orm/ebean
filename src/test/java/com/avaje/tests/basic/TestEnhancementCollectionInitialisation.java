package com.avaje.tests.basic;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;


public class TestEnhancementCollectionInitialisation extends BaseTestCase {
  
  @Test
  public void testListInitialisation() {
    
    Customer customer = new Customer();
    BeanState beanState = Ebean.getBeanState(customer);
    if (beanState != null) {
      List<Order> orders = customer.getOrders();
      Assert.assertNotNull(orders);
    }
    
  }

}
