package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.BeanCollection;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TestSharedInstancePropagation extends BaseTestCase {


  /**
   * Test that the sharedInstance status is propagated on lazy loading.
   */
  @Test
  public void testSharedListNavigate() {

    ResetBasicData.reset();

    Ebean.getServerCacheManager().clearAll();

    Order order = Ebean.find(Order.class)
      .setAutoTune(false)
      .setReadOnly(true)
      .setId(1)
      .findOne();


    assertNotNull(order);
    assertTrue(Ebean.getBeanState(order).isReadOnly());

    List<OrderDetail> details = order.getDetails();
    BeanCollection<?> bc = (BeanCollection<?>) details;
    assertTrue(bc.isReadOnly());
    assertFalse(bc.isPopulated());

    // lazy load
    bc.size();

    assertTrue(bc.isPopulated());
    assertTrue(!bc.isEmpty());
    OrderDetail detail = details.get(0);

    assertTrue(Ebean.getBeanState(detail).isReadOnly());
    assertFalse(Ebean.getBeanState(detail).isReference());

    Product product = detail.getProduct();

    assertTrue(Ebean.getBeanState(product).isReadOnly());

    // lazy load
    product.getName();
    assertFalse(Ebean.getBeanState(product).isReference());

  }
}
