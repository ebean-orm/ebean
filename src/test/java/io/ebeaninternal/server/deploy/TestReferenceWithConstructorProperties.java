package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

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
    Assert.assertTrue(beanState.isReference());

    // read the status invokes lazy loading
    order.getStatus();

    Assert.assertFalse(beanState.isReference());

  }

}
