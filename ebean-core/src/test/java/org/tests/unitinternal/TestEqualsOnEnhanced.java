package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEqualsOnEnhanced extends BaseTestCase {

  @Test
  public void test() {
    Customer c = new Customer();
    if (c instanceof EntityBean) {
      Order o = new Order();
      if (o instanceof EntityBean) {

        c.setId(1);

        o.setId(1);

        assertFalse(c.equals(o));
        assertFalse(c.equals(null));
        assertTrue(c.equals(c));

        Customer c2 = new Customer();
        c2.setId(1);
        assertTrue(c.equals(c2));

        Customer c3 = new Customer();
        // c2.setId(1);
        assertFalse(c.equals(c3));

        Customer c4 = new Customer();
        c4.setId(2);
        assertFalse(c.equals(c4));

      }
    }

  }
}
