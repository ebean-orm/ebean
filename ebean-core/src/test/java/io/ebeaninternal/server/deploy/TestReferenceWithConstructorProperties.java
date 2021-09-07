package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestReferenceWithConstructorProperties extends BaseTestCase {

  /**
   * Test when constructor sets some properties.
   */
  @Test
  public void test() {

    ResetBasicData.reset();
    Order order = DB.getReference(Order.class, 1);

    BeanState beanState = DB.getBeanState(order);
    Set<String> loadedProps = beanState.getLoadedProps();

    assertEquals(1, loadedProps.size());
    assertTrue(beanState.isReference());

    // read the status invokes lazy loading
    order.getStatus();

    assertFalse(beanState.isReference());
    assertNotNull(order.getCustomer());
  }

}
