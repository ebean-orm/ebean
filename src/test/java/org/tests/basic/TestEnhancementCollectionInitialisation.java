package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;


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
