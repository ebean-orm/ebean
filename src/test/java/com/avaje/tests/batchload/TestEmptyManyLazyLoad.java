package com.avaje.tests.batchload;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.Order.Status;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestEmptyManyLazyLoad extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c = Ebean.find(Customer.class).findList().get(0);

    Order o = new Order();
    o.setCustomer(c);
    o.setStatus(Status.NEW);

    Ebean.save(o);

    Order o2 = Ebean.find(Order.class, o.getId());
    o2.getDetails().size();

  }
}
