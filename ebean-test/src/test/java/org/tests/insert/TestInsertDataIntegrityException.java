package org.tests.insert;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.DataIntegrityException;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestInsertDataIntegrityException extends BaseTestCase {


  @IgnorePlatform(Platform.NUODB)
  @Test
  public void insert_invalidForeignKey() {
    ResetBasicData.reset();

    // an invalid foreign key value
    Customer invalidCustomer = DB.reference(Customer.class, 900000);

    Order order = new Order();
    order.setStatus(Order.Status.NEW);
    order.setCustomer(invalidCustomer);
    assertThatThrownBy(() -> DB.save(order))
      .isInstanceOf(DataIntegrityException.class)
      .hasMessageContaining("insert into o_order")
      .hasMessageContaining(",900000");

  }
}
