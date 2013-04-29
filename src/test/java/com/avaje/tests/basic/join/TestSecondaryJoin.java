package com.avaje.tests.basic.join;

import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.Order.Status;
import com.avaje.tests.model.basic.ResetBasicData;

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

    System.out.println("done");

  }

}
