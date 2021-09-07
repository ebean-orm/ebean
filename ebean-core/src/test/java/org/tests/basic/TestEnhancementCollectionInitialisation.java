package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class TestEnhancementCollectionInitialisation extends BaseTestCase {

  @Test
  public void testListInitialisation() {
    Customer customer = new Customer();
    BeanState beanState = DB.getBeanState(customer);
    if (beanState != null) {
      List<Order> orders = customer.getOrders();
      assertNotNull(orders);
    }
  }
}
