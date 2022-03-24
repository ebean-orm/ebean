package org.tests.unitinternal;

import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestEqualsOnEnhanced extends BaseTestCase {

  @Test
  public void test() {
    Customer c = new Customer();
    Order o = new Order();

    c.setId(1);
    o.setId(1);
    assertNotEquals(c, o);
    assertNotEquals(null, c);
    assertEquals(c, c);

    Customer c2 = new Customer();
    c2.setId(1);
    assertEquals(c, c2);

    Customer c3 = new Customer();
    assertNotEquals(c, c3);

    Customer c4 = new Customer();
    c4.setId(2);
    assertNotEquals(c, c4);
  }
}
