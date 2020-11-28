package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    assertEquals(1, loadedProps.size());
    assertTrue(beanState.isReference());

    // read the status invokes lazy loading
    order.getStatus();

    assertFalse(beanState.isReference());
    assertNotNull(order.getCustomer());
  }

}
