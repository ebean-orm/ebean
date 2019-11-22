package org.tests.insert;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.DataIntegrityException;
import io.ebean.Ebean;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

public class TestInsertDataIntegrityException extends BaseTestCase {


  @IgnorePlatform(Platform.NUODB)
  @Test(expected = DataIntegrityException.class)
  public void insert_invalidForeignKey() {

    ResetBasicData.reset();

    // an invalid foreign key value
    Customer invalidCustomer = Ebean.getReference(Customer.class, 900000);

    Order order = new Order();
    order.setStatus(Order.Status.NEW);
    order.setCustomer(invalidCustomer);

    DB.save(order);
  }
}
