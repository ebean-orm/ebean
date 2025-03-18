package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestSharedInstancePropagation extends BaseTestCase {


  /**
   * Test that the sharedInstance status is propagated on lazy loading.
   */
  @Test
  public void testSharedListNavigate() {

    ResetBasicData.reset();

    DB.cacheManager().clearAll();

    Order order = DB.find(Order.class)
      .setAutoTune(false)
      .setUnmodifiable(true) // .setReadOnly(true)
      .fetch("details")
      .fetch("details.product", "name")
      .setId(1)
      .findOne();

    assertNotNull(order);
    assertTrue(DB.beanState(order).isUnmodifiable()); // .isReadOnly()

    List<OrderDetail> details = order.getDetails();
    assertThrows(UnsupportedOperationException.class, details::clear);


    OrderDetail detail = details.get(0);

    assertTrue(DB.beanState(detail).isUnmodifiable()); // .isReadOnly()
    assertFalse(DB.beanState(detail).isReference());

    Product product = detail.getProduct();

    assertTrue(DB.beanState(product).isUnmodifiable());

    // lazy load
    product.getName();
    assertFalse(DB.beanState(product).isReference());

  }
}
