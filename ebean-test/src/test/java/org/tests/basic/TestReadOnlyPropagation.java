package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.bean.BeanCollection;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestReadOnlyPropagation extends BaseTestCase {

  @Test
  public void testReadOnly() {

    ResetBasicData.reset();
    DB.cacheManager().clearAll();

    Order order = DB.find(Order.class)
      .setReadOnly(true)
      .setId(1)
      .findOne();

    assertTrue(DB.beanState(order).isReadOnly());


    Customer customer = order.getCustomer();
    assertTrue(DB.beanState(customer).isReadOnly());

    Address billingAddress = customer.getBillingAddress();
    assertNotNull(billingAddress);
    assertTrue(DB.beanState(billingAddress).isReadOnly());


    List<OrderDetail> details = order.getDetails();
    BeanCollection<?> bc = (BeanCollection<?>) details;

    assertTrue(bc.isReadOnly());
    assertTrue(!bc.isPopulated());

    bc.size();
    assertTrue(!bc.isEmpty());
    assertTrue(bc.isReadOnly());
    assertTrue(bc.isPopulated());
    try {
      details.add(new OrderDetail());
      assertTrue(false);
    } catch (IllegalStateException e) {
      assertTrue(true);
    }
    try {
      details.remove(0);
      assertTrue(false);
    } catch (IllegalStateException e) {
      assertTrue(true);
    }
    try {
      Iterator<OrderDetail> it = details.iterator();
      it.next();
      it.remove();
      assertTrue(false);
    } catch (IllegalStateException e) {
      assertTrue(true);
    }
    try {
      ListIterator<OrderDetail> it = details.listIterator();
      it.next();
      it.remove();
      assertTrue(false);
    } catch (IllegalStateException e) {
      assertTrue(true);
    }
    try {
      List<OrderDetail> subList = details.subList(0, 1);
      subList.remove(0);
      assertTrue(false);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }


  }

}
