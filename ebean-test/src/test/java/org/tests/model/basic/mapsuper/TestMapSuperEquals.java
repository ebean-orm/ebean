package org.tests.model.basic.mapsuper;

import io.ebean.xtest.BaseTestCase;
import io.ebean.bean.EntityBean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

      assertEquals(a, b);
      assertEquals(b, a);
      assertNotEquals(a, c);
      assertNotEquals(b, c);

    } else {
      System.out.println("--- ok, not running TestMapSuperEquals test");
    }

  }

}
