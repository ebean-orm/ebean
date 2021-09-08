package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestReferenceWithConstructorProperties extends BaseTestCase {

  /**
   * Test when constructor sets some properties.
   */
  @Test
  public void test() {

    ResetBasicData.reset();
    Order order = DB.reference(Order.class, 1);

    BeanState beanState = DB.beanState(order);
    Set<String> loadedProps = beanState.loadedProps();

    assertEquals(1, loadedProps.size());
    assertTrue(beanState.isReference());

    // read the status invokes lazy loading
    order.getStatus();

    assertFalse(beanState.isReference());
    assertNotNull(order.getCustomer());
  }

}
