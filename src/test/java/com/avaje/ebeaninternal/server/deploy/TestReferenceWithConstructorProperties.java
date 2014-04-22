package com.avaje.ebeaninternal.server.deploy;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestReferenceWithConstructorProperties extends BaseTestCase {

  /**
   * Test when constructor sets some properties.
   */
  @Test
  public void test() {
    
    ResetBasicData.reset();
    Order order = Ebean.getReference(Order.class, 1);

    BeanState beanState = Ebean.getBeanState(order);
    Set<String> loadedProps = beanState.getLoadedProps();
    
    Assert.assertEquals(1, loadedProps.size());

    // read the status invokes lazy loading
    order.getStatus();
    
    BeanState beanState2 = Ebean.getBeanState(order);
    // fully loaded
    Assert.assertNull(beanState2.getLoadedProps());
    
    
  }
  
}
