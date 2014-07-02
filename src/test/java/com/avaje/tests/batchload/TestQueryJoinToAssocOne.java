package com.avaje.tests.batchload;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryJoinToAssocOne extends BaseTestCase {

  @Test
  public void testLazyOnNonLoaded() {

    ResetBasicData.reset();

    // This will use 3 SQL queries to build this object graph
    List<Order> l0 = Ebean.find(Order.class).select("status, shipDate")
        .fetch("details", "orderQty, unitPrice", new FetchConfig().query())
        .fetch("details.product", "sku, name")

        // .join("customer", "name", new JoinConfig().query(10))
        // .join("customer.contacts","firstName, lastName, mobile")
        // .join("customer.shippingAddress","line1, city")
        .findList();

    Assert.assertTrue(l0.size() > 0);

  }
}
