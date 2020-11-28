package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.junit.Assert;
import org.junit.Test;

public class TestEqualsOnEnhanced extends BaseTestCase {

  @Test
  public void test() {

    Customer c = new Customer();
    if (c instanceof EntityBean) {
      Order o = new Order();
      if (o instanceof EntityBean) {

        c.setId(1);

        o.setId(1);

        Assert.assertFalse(c.equals(o));
        Assert.assertFalse(c.equals(null));
        Assert.assertTrue(c.equals(c));

        Customer c2 = new Customer();
        c2.setId(1);
        Assert.assertTrue(c.equals(c2));

        Customer c3 = new Customer();
        // c2.setId(1);
        Assert.assertFalse(c.equals(c3));

        Customer c4 = new Customer();
        c4.setId(2);
        Assert.assertFalse(c.equals(c4));

      }
    }

  }
}
