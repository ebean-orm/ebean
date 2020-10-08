package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

public class TestQueryJoinBatchSize extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Order> list = Ebean.find(Order.class)
      .fetch("customer", new FetchConfig().queryFirst(3).lazy(2))
      //.fetch("orders.details", new FetchConfig().query())
      //.fetch("orders.shipments", new FetchConfig().query())
      .findList();

    for (Order order : list) {
      Customer customer = order.getCustomer();
      customer.getName();
    }

  }

//  @Test
//  public void test() {
//
//    ResetBasicData.reset();
//
//    Ebean.find(Customer.class)
//        .fetch("orders", new FetchConfig().query(3).lazy(2))
//        //.fetch("orders.details", new FetchConfig().query())
//        //.fetch("orders.shipments", new FetchConfig().query())
//        .findList();
//
//  }

}
