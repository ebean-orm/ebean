package org.tests.model.basic.mapsuper;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import org.junit.Assert;
import org.junit.Test;

public class TestMapSuperEquals extends BaseTestCase {

  @Test
  public void testEquals() {

    MapSuperActual a = new MapSuperActual();

    if (a instanceof EntityBean) {
      // test on enhanced beans only

      MapSuperActual b = new MapSuperActual();
      b.setId(456l);

      MapSuperActual c = new MapSuperActual();
      c.setId(2l);

      a.setId(456l);

      Assert.assertTrue("equals By Id value on enhanced mapped super", a.equals(b));
      Assert.assertTrue(b.equals(a));
      Assert.assertTrue(!a.equals(c));
      Assert.assertTrue(!b.equals(c));

    } else {
      System.out.println("--- ok, not running TestMapSuperEquals test");
    }

  }

}
