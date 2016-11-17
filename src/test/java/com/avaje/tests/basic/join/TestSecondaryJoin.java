package com.avaje.tests.basic.join;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.Order.Status;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

public class TestSecondaryJoin extends BaseTestCase {

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
