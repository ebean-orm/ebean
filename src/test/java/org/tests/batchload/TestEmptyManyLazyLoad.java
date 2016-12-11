package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

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
