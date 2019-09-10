package org.tests.basic.join;

import io.ebean.Ebean;
import io.ebean.TransactionalTestCase;

import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

public class TestSecondaryJoin extends TransactionalTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Order> list = Ebean.find(Order.class)
      // .select("*")
      // .join("customer")
      .findList();

    Order o0 = list.get(0);
    o0.setCustomerName("Banan");
    o0.setStatus(Status.APPROVED);

    Ebean.save(o0);
  }

}
