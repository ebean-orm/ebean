package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;
import org.tests.model.basic.ResetBasicData;

public class TestEmptyManyLazyLoad extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c = DB.find(Customer.class).findList().get(0);

    Order o = new Order();
    o.setCustomer(c);
    o.setStatus(Status.NEW);

    DB.save(o);

    Order o2 = DB.find(Order.class, o.getId());
    o2.getDetails().size();

    // cleanup
    DB.delete(o2);

  }
}
