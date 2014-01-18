package com.avaje.tests.query;

import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

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
