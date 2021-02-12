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

    Ebean.find(Customer.class).fetchQuery("orders")
      .fetchQuery("orders.details")
      .fetchQuery("orders.shipments")
      .fetchQuery("shippingAddress")
      .fetchQuery("billingAddress").findList();

  }

}
