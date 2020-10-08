package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

public class TestThreeLevelQueryJoin extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Ebean.find(Customer.class).fetch("orders", new FetchConfig().query())
      .fetch("orders.details", new FetchConfig().query())
      .fetch("orders.shipments", new FetchConfig().query())
      .fetch("shippingAddress", new FetchConfig().query())
      .fetch("billingAddress", new FetchConfig().query()).findList();

  }

}
