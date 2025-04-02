package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class TestReadOnlyPropagation extends BaseTestCase {

  @Test
  void testReadOnly() {

    ResetBasicData.reset();
    DB.cacheManager().clearAll();

    Order order = DB.find(Order.class)
      .setUnmodifiable(true)
      .fetch("customer")
      .fetch("customer.billingAddress")
      .fetch("details")
      .setId(1)
      .findOne();

    assertTrue(DB.beanState(order).isUnmodifiable());


    Customer customer = order.getCustomer();
    assertTrue(DB.beanState(customer).isUnmodifiable());

    Address billingAddress = customer.getBillingAddress();
    assertNotNull(billingAddress);
    assertTrue(DB.beanState(billingAddress).isUnmodifiable());


    List<OrderDetail> details = order.getDetails();
    assertThrows(UnsupportedOperationException.class, details::clear);


    assertThatThrownBy(() -> details.add(new OrderDetail())).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> details.remove(0)).isInstanceOf(UnsupportedOperationException.class);

    Iterator<OrderDetail> it = details.iterator();
    it.next();
    assertThatThrownBy(it::remove).isInstanceOf(UnsupportedOperationException.class);

    ListIterator<OrderDetail> it2 = details.listIterator();
    it2.next();
    assertThatThrownBy(it2::remove).isInstanceOf(UnsupportedOperationException.class);

    List<OrderDetail> subList = details.subList(0, 1);
    assertThatThrownBy(() -> subList.remove(0)).isInstanceOf(UnsupportedOperationException.class);
  }

}
