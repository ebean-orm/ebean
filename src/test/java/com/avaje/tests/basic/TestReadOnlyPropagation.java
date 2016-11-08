package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.tests.model.basic.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class TestReadOnlyPropagation extends BaseTestCase {

  @Test
  public void testReadOnly() {

    ResetBasicData.reset();

    Order order = Ebean.find(Order.class)
      .setAutoTune(false)
      .setUseCache(false)
      .setReadOnly(true)
      .setId(1)
      .findUnique();

    Assert.assertTrue(Ebean.getBeanState(order).isReadOnly());


    Customer customer = order.getCustomer();
    Assert.assertTrue(Ebean.getBeanState(customer).isReadOnly());

    Address billingAddress = customer.getBillingAddress();
    Assert.assertTrue(Ebean.getBeanState(billingAddress).isReadOnly());


    List<OrderDetail> details = order.getDetails();
    BeanCollection<?> bc = (BeanCollection<?>) details;

    Assert.assertTrue(bc.isReadOnly());
    Assert.assertTrue(!bc.isPopulated());

    bc.size();
    Assert.assertTrue(!bc.isEmpty());
    Assert.assertTrue(bc.isReadOnly());
    Assert.assertTrue(bc.isPopulated());
    try {
      details.add(new OrderDetail());
      Assert.assertTrue(false);
    } catch (IllegalStateException e) {
      Assert.assertTrue(true);
    }
    try {
      details.remove(0);
      Assert.assertTrue(false);
    } catch (IllegalStateException e) {
      Assert.assertTrue(true);
    }
    try {
      Iterator<OrderDetail> it = details.iterator();
      it.next();
      it.remove();
      Assert.assertTrue(false);
    } catch (IllegalStateException e) {
      Assert.assertTrue(true);
    }
    try {
      ListIterator<OrderDetail> it = details.listIterator();
      it.next();
      it.remove();
      Assert.assertTrue(false);
    } catch (IllegalStateException e) {
      Assert.assertTrue(true);
    }
    try {
      List<OrderDetail> subList = details.subList(0, 1);
      subList.remove(0);
      Assert.assertTrue(false);
    } catch (UnsupportedOperationException e) {
      Assert.assertTrue(true);
    }


  }

}
